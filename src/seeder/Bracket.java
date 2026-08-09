package seeder;

import java.lang.Math;
import java.util.*;

public class Bracket {
    public String bracketName = "empty";
    public int bracketId;
    private int pointsAward;
    private int bracketAvgPoints = 0;
    private int totalBracketPoolCountMark = 0;
    private int totalLosersRoundsCount = 1;
    private Integer currentPoolStageMarker = 1;
    private Player bracketWinner = null;
    private NavigableMap<Integer, Integer> totalLosersRoundEachStage = new TreeMap<>();
    private NavigableMap<Integer, NavigableMap<Integer, Integer>> playersEliminatedCountMap = new TreeMap<>(); //organize and saves the number of eliminated players for each stage.
    private NavigableMap<Integer, NavigableMap<Integer, Pool>> bracketStagesMap = new TreeMap<>(); //contains each map for each pool stage
    private final int bracketTier = 2;
    private final NavigableMap<Integer, Match> allMatchesByID = new TreeMap<>();
    private final TreeMap<Player, Integer> orderedPlayerMap;
    private final NavigableMap<Integer, Pool> seededPoolMap = new TreeMap<>(); //ordered so higher seeds are at opposing ends of their bracket
    private final String gameName;
    private final Tournament parentTournament;
    private final NavigableMap<Integer, Pool> orderedPoolMap = new TreeMap<>(); //initial stage organized pool map. Used only for the first set of pools

    public Bracket(Tournament parentTournament, String bracketName, ArrayList<Player> entrants, int pool_size, String gameName) {
        if (entrants.size() < 16) throw new IllegalStateException("The bracket size: " + entrants.size() + "is too small.");
        this.bracketName = bracketName;
        this.parentTournament = parentTournament;
        this.gameName = gameName;
        orderedPlayerMap = new TreeMap<>(Comparator.<Player, Integer>comparing(p -> p.getPoints(gameName)).thenComparing(Player::getPlayerID)); //ensures that the players are correctly ordered by points without being overwritten
        int total_points = 0;
         for (Player player : entrants) {
             orderedPlayerMap.put(player, player.getPoints(gameName));
            total_points += player.getPoints(gameName);
        }

        bracketAvgPoints = total_points / orderedPlayerMap.size();


        createPools(pool_size);

    }

    // calculate log2 N indirectly for size weight calculation
    private static int log2(int N) {
        return (int)(Math.log(N) / Math.log(2));
    }

    /*FUTURE MAJOR ADJUSTMENTS NEEDED:
    * ALLOW THE USER TO REQUEST THEIR OWN NUMBER OF POOLS WILL NEED TO PLACE RESTRICTIONS AND ADJUST PLAYINS MOST LIKELY
    * IN THE CASES THAT THEY BECOME LARGER THAN 8 PLAY IN MATCHES PER POOL
    * EXECUTE NEEW PLAN IN NOTEBOOOK!!!!!
     */


    // if remainder != 0 create playIns
    //playIn matches = orderedPlayerMap.size() / 2
    // for a count of playInMatches times, the top seeded players enter a match without an opponent.
    //playInMatches MUST be snaked like winners round 1.
    public void createPools(int poolSize) {
        // pool count is always total players / 16, rounded down
        double matchesNeeded = Math.ceil((double)  orderedPoolMap.size()/ 2);
        int prelimMatchCount = 0;
        int poolCount = orderedPlayerMap.size() / 16;
        if (poolCount < 1) poolCount = 1;
        else if (orderedPlayerMap.size() % 16 != 1) {
            poolCount--;
            prelimMatchCount = orderedPlayerMap.size() - poolCount * 16; // The number of preliminary matchers there is to be.
        }
        int singleSeededPlayers = prelimMatchCount;

        //creates empty pools to use later
        for (int i = 1; i < poolCount + 1; i++) {
            orderedPoolMap.put(i, new Pool("Pool " + i, this, 1));
            //probably should add pool id here too
            totalBracketPoolCountMark++;
        }
        if (poolCount == 1) {
            seededPoolMap.put(1, new Pool("Pool " + 1, this, 1));
        }
        else {
            //creates empty pools to use later
            int snakeLeft = 1; //need to change this to 1 and fix the error.
            int snakeRight = orderedPoolMap.size();
            boolean goingRight = true;
            while (snakeLeft <= snakeRight) {
                if (goingRight) {
                    seededPoolMap.put(snakeLeft, new Pool("Pool " + snakeLeft, this, 1));
                    seededPoolMap.put(snakeRight, new Pool("Pool " + snakeRight, this, 1));
                } else {
                    seededPoolMap.put(snakeRight, new Pool("Pool " + snakeRight, this, 1));
                    seededPoolMap.put(snakeLeft, new Pool("Pool " + snakeLeft, this, 1));
                }
                snakeRight--;
                snakeLeft++;
                goingRight = !goingRight;
            }
        }
        // Convert ordered map to a list, ascending by points (worst to best)
        ArrayList<Player> sortedPlayers = new ArrayList<>(orderedPlayerMap.keySet());

        //only need to approach this function if there are leftover players
        if (prelimMatchCount > 0) seedPrelimMatches(sortedPlayers, poolCount, prelimMatchCount);

        seedFirstRoundMatches(sortedPlayers, poolCount, singleSeededPlayers);
        bracketStagesMap.put(currentPoolStageMarker, seededPoolMap);

        //fill out all of the matches for both sides of the pool
        for (Pool createdPool : bracketStagesMap.get(currentPoolStageMarker).values())  {
            createWinnersSideMatches(createdPool);
            createLosersSideMatches(createdPool);
        }

        // Finds how many total losersRounds are created in the first stage and adds them to the tracker.
       Pool testPool =  bracketStagesMap.get(currentPoolStageMarker).firstEntry().getValue();
        int roundTracker = 201;
        boolean maxFound = false;
        while (!maxFound) {
            if (!testPool.getLosersSide().containsKey(roundTracker)) {
                totalLosersRoundsCount += (roundTracker - 100) / 100;
                totalLosersRoundsCount--; // to account for an extra round being counted in the increment process
                maxFound = true;
            }
            roundTracker += 100;
        }


        NavigableMap<Integer, Integer> nestedPlayersEliminatedMap = new TreeMap<>();
        playersEliminatedCountMap.put(currentPoolStageMarker, nestedPlayersEliminatedMap);
    }

    //this method starts from round 2 and creates the winners side matches
    private void createWinnersSideMatches(Pool pool) {
        int winnersRounds = (int)(Math.log(pool.getInitialPoolSize()) / Math.log(2)) + 1;
        Integer matchPositionMarker = 201;
        if (!pool.getPreliminaries().isEmpty()) matchPositionMarker = 301;
        int maxMatchesInRound = pool.getInitialPoolSize() / 2; // needs to be the initial size cut in half, because its starts at the second round

        //creates placeholder pools for each round, and resets when the last position of the round has a match
        for (int i = 0; i < winnersRounds; i++) {
            while (matchPositionMarker % 100 <= maxMatchesInRound) {
                Match newMatch = new Match(matchPositionMarker, pool, "winners", this);
                pool.addWinnersMatch(newMatch);
                matchPositionMarker++;
            }
            matchPositionMarker = ((matchPositionMarker / 100) + 1) * 100 + 1;
            maxMatchesInRound /= 2;
        }
    }

    //This method starts from losers round 1, if there are no prelims. Then round 2 if there are prelims. Then it creates future losers matches for the pool
    private int  createLosersSideMatches(Pool pool) {
        int losersRounds = 2 * ((int)(Math.log(pool.getInitialPoolSize()) / Math.log(2))) - 2;
        Integer matchPositionMarker = 101;
        int consolidationMarker = 1;
        //if the
        int maxMatchesInRound = pool.getInitialPoolSize() / 2; // needs to be the initial size cut in half, because its starts at the second round

        if (!pool.getPreliminaries().isEmpty()) {
            matchPositionMarker = 201;
            consolidationMarker = 0;
            losersRounds++;
        }

        //handle variance when pools are merged. Two additional losers rounds are needed, because the first round of the new pool is filled
        else if (currentPoolStageMarker != 1) {
            matchPositionMarker = 201;
            maxMatchesInRound = pool.getInitialPoolSize();
//            totalLosersRoundsCount++; // need to increment to account for the first losers round being filled.
            losersRounds += 2;
        }
        //creates placeholder pools for each round, and resets when the last position of the round has a match
        for (int i = 0; i < losersRounds + 1; i++) {
            while (matchPositionMarker % 100 <= maxMatchesInRound) {
                Match newMatch = new Match(matchPositionMarker, pool, "losers", this);
                newMatch.setActualMatchRound(i + 1 + totalLosersRoundsCount);
                pool.addLosersMatch(newMatch);
                matchPositionMarker++;
            }
            //if consolidationMarker is even, the next round will be a consolidation round. Therefore, we divide maxMatchesInRound by 2.
            consolidationMarker++;
            matchPositionMarker = ((matchPositionMarker / 100) + 1) * 100 + 1;
            if (consolidationMarker % 2 == 0) maxMatchesInRound /= 2;
        }
        return losersRounds + 1;
    }


    // Generates the correct order for assigning players to their correct pools
    // This is to keep the tournament well balanced, ensuring the strongest players don't meet until the latest possible time
    private int[] generatePoolOrder(int poolCount) {
        if (poolCount == 1) return new int[]{1};
        int[] prev = generatePoolOrder(poolCount / 2);
        int[] result = new int[poolCount];
        for (int i = 0; i < prev.length; i++) {
            result[i * 2] = prev[i];
            result[i * 2 + 1] = poolCount + 1 - prev[i];
        }
        return result;
    }

    //FINISHED
    private void seedPrelimMatches(ArrayList<Player> sortedPlayers, int poolCount, int prelimCount) {
        int[] subOrder = {1, 5, 7, 3, 4, 6, 8, 2};
        int[] poolOrder = generatePoolOrder(poolCount);
        int subOrderCount = 0;
        int poolOrderCount = 0;

        for (int i = 0; i < prelimCount * 2; i += 2) {
            int currentPoolMark = poolOrder[poolOrderCount];

            if (!seededPoolMap.containsKey(currentPoolMark)) {
                seededPoolMap.put(currentPoolMark, new Pool("Pool " + currentPoolMark, this, 1));
            }

            int matchPosition = 100 + subOrder[subOrderCount];

            Match prelimMatch = new Match(sortedPlayers.get(i + 1), sortedPlayers.get(i), matchPosition, seededPoolMap.get(currentPoolMark), "preliminaries", this);
            seededPoolMap.get(currentPoolMark).addPlayInMatch(prelimMatch);

            poolOrderCount++;
            if (poolOrderCount >= poolCount) {
                poolOrderCount = 0;
                subOrderCount++;
            }
        }
    }


    //FINISHED
    private void seedFirstRoundMatches(ArrayList<Player> sortedPlayers, int numberOfPools, int singleSeededPlayers) {
        int playerLeft = singleSeededPlayers * 2;
        int playerRight = sortedPlayers.size() - 1;
        int currentPoolMark = 1;
        int[] subOrder = {1, 5, 7, 3, 4, 6, 8, 2};
        int[] poolOrder = generatePoolOrder(numberOfPools);
        int subOrderCount = 0;
        int poolOrderCount = 0;
        int roundVal = 100;

        while (playerLeft < playerRight) {
            currentPoolMark = poolOrder[poolOrderCount];

            if (!seededPoolMap.containsKey(currentPoolMark))
                seededPoolMap.put(currentPoolMark, new Pool("Pool " + currentPoolMark, this, 1));

            if (!seededPoolMap.get(currentPoolMark).getPreliminaries().isEmpty()) roundVal = 200;
            else roundVal = 100;

            int matchPosition = roundVal + subOrder[subOrderCount];

            Match newMatch;
            if (singleSeededPlayers > 0) {
                newMatch = new Match(sortedPlayers.get(playerRight), matchPosition, 1, seededPoolMap.get(currentPoolMark), "winners", this);
                singleSeededPlayers--;
            } else {
                newMatch = new Match(sortedPlayers.get(playerRight), sortedPlayers.get(playerLeft), matchPosition, seededPoolMap.get(currentPoolMark), "winners", this);
                playerLeft++;
            }
            seededPoolMap.get(currentPoolMark).addWinnersMatch(newMatch);
            seededPoolMap.get(currentPoolMark).incrementInitialPoolSize();

            playerRight--;
            poolOrderCount++;
            if (poolOrderCount >= numberOfPools) {
                poolOrderCount = 0;
                subOrderCount++;
            }
        }
    }

    /*gonna merge winners pools here. Each winner's pool should be 3 rounds, then the winners from the previous pool
    should face each other first in the next pool*/
    public void mergePools(NavigableMap<Integer, Pool> activePools) {
        currentPoolStageMarker++; //tournament enters a new stage of pools\
        totalBracketPoolCountMark++;
        totalLosersRoundsCount++;
        NavigableMap<Integer, Pool> newPoolMap = new TreeMap<>();
        int newPoolKey = 1;
        int poolMatchMaxCount = 1;
        boolean playerParity = false;

        Pool newPool = new Pool("Pool " + totalBracketPoolCountMark, this, currentPoolStageMarker);
        Integer subRoundMarker = 101;
        for (Pool pool : activePools.values()) {
            //If the new pool size goes above the maximum match count, create another pool
            //Needs to be adjusted for in the future to allow pools greater than a size of 8.
            if (poolMatchMaxCount >= 8) {
                totalBracketPoolCountMark++;
                subRoundMarker = 101;
                poolMatchMaxCount = 1;
                newPoolKey++;
                newPool = new Pool("Pool " + totalBracketPoolCountMark, this, currentPoolStageMarker);
                newPoolMap.put(newPoolKey, newPool); //***TODO MAKE SURE THIS BELONGS HERE
            }

            Player poolWinner = pool.getWinnersSideFinalist();
            Player poolLoser = pool.getLosersSideFinalist();

            //THERE SHOULD NEVER BE A CASE WHERE POOLS ARE ODD AT CREATION
            if (!playerParity) {
                Match newWinnersMatch = new Match(poolWinner, subRoundMarker, 1, newPool, "winners", this);
                newPool.addWinnersMatch(newWinnersMatch);
                Match newLosersMatch = new Match(poolLoser, subRoundMarker, 1, newPool, "losers", this);
                newPool.addLosersMatch(newLosersMatch);
                playerParity = true;
                newPool.incrementInitialPoolSize();
                newLosersMatch.setActualMatchRound(totalLosersRoundsCount);
            }
            else {
                Match existingWinnersMatch = newPool.getWinnersSide().get(subRoundMarker);
                existingWinnersMatch.setPlayer2(poolWinner);
                Match existingLosersMatch = newPool.getLosersSide().get(subRoundMarker);
                existingLosersMatch.setPlayer2(poolLoser);
//                poolWinner.updateBracketMatchHistory(existingWinnersMatch.getMatchGame(), existingWinnersMatch);
//                poolLoser.updateBracketMatchHistory(existingLosersMatch.getMatchGame(), existingLosersMatch);
                playerParity = false;
                subRoundMarker++;
                poolMatchMaxCount++;
            }
        }
        newPoolMap.put(totalBracketPoolCountMark, newPool);
        bracketStagesMap.put(currentPoolStageMarker, newPoolMap);
        int roundsThisStage = 0;
        for (Pool mergedNewPool : bracketStagesMap.get(currentPoolStageMarker).values()) {
            createWinnersSideMatches(mergedNewPool);
            roundsThisStage = createLosersSideMatches(mergedNewPool); // 2nd call, same pools
        }
        totalLosersRoundsCount += roundsThisStage;

        NavigableMap<Integer, Integer> nestedPlayersEliminatedMap = new TreeMap<>();
        playersEliminatedCountMap.put(currentPoolStageMarker,nestedPlayersEliminatedMap);

    }

    //***TODO fix this function based on claude's recommendations
    public int calculatePlacement(Match match) {

        int eliminatedRound = match.getActualMatchRound();

        int eliminatedRoundDiff = totalLosersRoundsCount - eliminatedRound;

//        // Losers Finals loser
//        if (eliminatedRoundDiff == 1) return 3;
//
//        // Losers Semis loser
//        if (eliminatedRoundDiff == 2) return 4;

        int k = eliminatedRoundDiff / 2;

        int placement;
        if ((eliminatedRoundDiff & 1) == 0) placement = (1 << (k + 1)) + 1;
        else placement = 3 * (1 << k) + 1;

//        System.out.printf(
//                "Placement=%d  ActualRound=%d  Diff=%d%n",
//                placement,
//                match.getActualMatchRound(),
//                eliminatedRoundDiff
//        );

        return placement;
    }

    //After player completes their run in the tournament
    //FINISHED
    public void finishPlayer(Player player, Match lastMatch) {

        int placement;

        //handles grand finals winner and loser
        if (lastMatch.getMatchPosition() == 0) {
            if (player == lastMatch.getWinner()) placement = 1;
            else placement = 2;
        }
        // calculate placement once and reuse if the player's last match was not grand finals

        else placement = calculatePlacement(lastMatch);


        //Tournament win probability vs avg
        double winProbVsAvg = 1 / (1 + Math.pow(10, -1 * ((double) (player.getPoints(gameName) - this.bracketAvgPoints) / 500)));

        // Tournament expected placement
        int playerExpectedPlacement = (int) (1 + (orderedPlayerMap.size() - 1) * (1 - winProbVsAvg));

        //the expected modifier for the deltaR component of the final point calculation
        double expectedPlacementMod = Math.log((double) orderedPlayerMap.size() / playerExpectedPlacement) / Math.log(orderedPlayerMap.size());

        double actualPlacementMod = Math.log((double) orderedPlayerMap.size() / placement) / Math.log(orderedPlayerMap.size());

        //Tournament difficulty modifier also denoted as "A"
        double tournamentDifficultyMod = Math.max(0.60, Math.sqrt((double) bracketAvgPoints / player.getPoints(gameName)));

        //Tournament prize formula, then award player
        int baseAward = switch (bracketTier) {
            case 1 -> 180;
            case 2 -> 120;
            case 3 -> 80;
            case 4 -> 50;
            case 5 -> 30;
            default -> throw new IllegalStateException("Unexpected value: " + bracketTier + "Tournament tier must be 1-5");
        };

        //Final tournament awards formula
        int finalPointsChange = (int) Math.round(baseAward * 1.5 * Math.max(-1.0, Math.min(1.0, actualPlacementMod - expectedPlacementMod) * tournamentDifficultyMod));

        player.setPlayerPlacement(parentTournament.getTournamentID(), gameName, placement);
        player.finalPointsChange(gameName, finalPointsChange, parentTournament);
        player.setPlacementFinalized(true);
    }

    //Finished
    //*Note in the future should maybe change to only contain the match and the victor
    public void finishMatch(Integer stage, Integer poolNumber, Integer matchPosition, String side, Player victor) {
        Pool targetPool = this.bracketStagesMap.get(stage).get(poolNumber);
        Match targetMatch = null;

        switch (side) {
            case "winners" -> targetMatch = targetPool.getWinnersSide().get(matchPosition);
            case "losers" -> targetMatch = targetPool.getLosersSide().get(matchPosition);
            case "preliminaries" -> targetMatch = targetPool.getPreliminaries().get(matchPosition);
            default -> {
                System.out.println("Please enter valid entry side.");
                return;
            }
        }
        targetMatch.setWinner(victor);

        // update bracket routing
        if (side.equals("winners") || side.equals("preliminaries")) {
            targetPool.matchUpdateWinners(targetMatch);
            targetPool.matchUpdateLosers(targetMatch);
        }

        else targetPool.matchUpdateLosers(targetMatch);


        // check if ALL pools in this stage are finished
        boolean allFinished = this.bracketStagesMap.get(stage).values().stream().allMatch(Pool::isFinished);
        if (allFinished) {
            //if this is the final pool of the tournament, finish the two remaining players and complete the tournament.
            if(bracketStagesMap.get(currentPoolStageMarker).size() <= 1) {
                Match grandFinals = new Match(targetPool.getWinnersSideFinalist(), targetPool.getLosersSideFinalist(), 0, targetPool, "winners", this);
                allMatchesByID.put(grandFinals.getMatchId(), grandFinals);

                double expectedResult = (1 / (1 + Math.pow(10, -1 * ((double) (grandFinals.getP1().getPoints(gameName) - grandFinals.getP2().getPoints(gameName)) / 500))));
                double random = Math.random();

                //Randomly determines which player wins by win probability
                if (random < expectedResult) {
                    grandFinals.setWinner(grandFinals.getP1());
                }
                else {
                    grandFinals.setWinner(grandFinals.getP2());
                }

                //Determine bracket winner and complete the bracket.
                this.finishPlayer(grandFinals.getWinner(), grandFinals);
                this.finishPlayer(grandFinals.getLoser(),grandFinals);

                grandFinals.getWinner().updatePlayerTotalMatchHistory(parentTournament.getTournamentID(), gameName);
                grandFinals.getLoser().updatePlayerTotalMatchHistory(parentTournament.getTournamentID(), gameName);
                grandFinals.getWinner().applyFinalPoints(gameName);
                grandFinals.getLoser().applyFinalPoints(gameName);
                bracketWinner = grandFinals.getWinner();
            }
            else {
//                totalLosersRoundsCount++;
                mergePools(this.bracketStagesMap.get(stage));
            }
        }
    }

    //Used for automatic simulations. Pass matches in one at a time, and the rest will be handled by the class
    private void autoDeclareWinner(Match targetMatch) {
        Pool matchPool = targetMatch.getParentPool();

        Player winningPlayer;

        double expectedResult = (1 / (1 + Math.pow(10, -1 * ((double) (targetMatch.getP1().getPoints(gameName) - targetMatch.getP2().getPoints(gameName)) / 500))));
        double random = Math.random();

        //Randomly determines which player wins by win probability
        if (random < expectedResult) winningPlayer = targetMatch.getP1();
        else  winningPlayer = targetMatch.getP2();

        finishMatch(matchPool.getPoolStage(), matchPool.getPoolNumber(), targetMatch.getMatchPosition(), targetMatch.getMatchSide(), winningPlayer);
    }


    public boolean autoCompleteBracket(NavigableMap<Integer, Pool> simulatedPoolMap) {
        for (Pool simulatedPool : simulatedPoolMap.values()) {
            if (!simulatedPool.getPreliminaries().isEmpty())
                while (!simulatedPool.isPrelimsFinished()) for (Match prelimMatch : simulatedPool.getPreliminaries().values()) autoDeclareWinner(prelimMatch);

            while (!simulatedPool.isWinnersFinished()) for (Match winnersMatch : simulatedPool.getWinnersSide().values()) autoDeclareWinner(winnersMatch);

            while (!simulatedPool.isLosersFinished()) for (Match losersMatch : simulatedPool.getLosersSide().values()) autoDeclareWinner(losersMatch);
        }
        if (bracketWinner != null) {
            Map<Integer, Integer> matchesPerRound = new TreeMap<>();

            for (Match m : allMatchesByID.values()) {
                if (m.getMatchSide().equals("losers")) {
                    matchesPerRound.merge(m.getActualMatchRound(), 1, Integer::sum);
                }
            }

//            System.out.println(matchesPerRound);
            finalizeAllPlacements();
            return true;
        }
        return false;
    }

    //finishes off each player and calculates their bracket placement
    public void finalizeAllPlacements() {
        for (Player player : orderedPlayerMap.keySet()) {
            if (!player.isPlacementFinalized()) {
                finishPlayer(player, player.getPlayerBracketMatchHistory(gameName).get(player.getPlayerBracketMatchHistory(gameName).size() - 1));
                player.applyFinalPoints(gameName);
                player.updatePlayerTotalMatchHistory(this.parentTournament.getTournamentID(), gameName);
            }
        }
    }


    public Pool findPool(Integer stage, Integer poolNumber) { return this.bracketStagesMap.get(stage).get(poolNumber); }

    //organizes and outputs all entrant placements into a printable string.
    public String getFinalPlacements() {
        StringBuilder placementString = new StringBuilder();
        NavigableMap<Integer, ArrayList<Player>> printingMap = new TreeMap<>();

        // fill the entrants into an ordered printable list from highest placing player to lowest placing player.
        orderedPlayerMap.forEach((player, value) -> printingMap.computeIfAbsent(player.getPlayerPlacement(parentTournament.getTournamentID(), gameName), placement -> new ArrayList<>()).add(player));

        for (ArrayList<Player> position : printingMap.values())
            //the original elo rating calculation here isn't working right for 1st and second place. more investigation is required.
            for (Player finishedPlayer : position) {
                Integer placement = finishedPlayer.getPlayerPlacement(parentTournament.getTournamentID(), gameName);
                int pointsDifferenceAfterBracket = finishedPlayer.getPointsChangeAfterBracket(parentTournament, gameName);
                int originalEloCalc = finishedPlayer.getPlayerPoints(gameName) - pointsDifferenceAfterBracket;

                placementString.append("|| " + finishedPlayer.getNickname() + " | Placement: " + placement + " | Points gained/lost: " + pointsDifferenceAfterBracket + " " +
                        finishedPlayer.getBracketWinLossRecord(parentTournament.getTournamentID(), gameName) + " Original Elo Rating: " + originalEloCalc + " |  New Elo Rating: " + finishedPlayer.getPlayerPoints(gameName) + " ||\n\n");
            }
        return placementString.toString();
    }

    public String getGameName() { return this.gameName; }

    public Player getBracketWinner() { return this.bracketWinner; }

    public NavigableMap<Integer, NavigableMap<Integer, Pool>> getBracketStagesMap() { return bracketStagesMap; }

    public Tournament getParentTournament() {return parentTournament;}

    public NavigableMap<Integer, Match> getAllMatchesByID() { return this.allMatchesByID; }



    @Override
    public String toString() {
        StringBuilder bracketString = new StringBuilder();
        bracketString.append("*******" + gameName + "****** \n\n\n");
        for (Map.Entry<Integer, Pool> entry : seededPoolMap.entrySet()) {
            bracketString.append(entry.getValue().toString());
        }
        return bracketString.toString();
    }


}