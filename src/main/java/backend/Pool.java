package backend;


import java.util.*;
import backend.dbconnector.*;

/*
 * Creating a fighting game pool
 * Record wins and losses from user input
 * Create and maintain loser's bracket
 * Pass on qualifying players to the next pool
 * **TODO**
 */

public class Pool {
    public String poolName = "empty"; //will eventually need another way to identify the pool. Also customizable pool names?????? */
    public Bracket parentBracket;
    public ArrayList<Player> poolPlayers = new ArrayList<>();
    private Player winnersFinalist = null;
    private Player losersFinalist = null;
    private boolean prelimsFinished = false;
    private boolean winnersFinished = false;
    private boolean losersFinished = false;
    private Long poolID;
    private Match losersFinalMatch;
    private final PoolQueries poolQueries;
    private final NavigableMap<Integer, Match> winnersSide = new TreeMap<>(); //contains all winners matches, sorted by match position
    private final NavigableMap<Integer, Match> losersSide = new TreeMap<>(); //contains all losers matches, sorted by match position
    private final NavigableMap<Integer, Match> preliminaries = new TreeMap<>(); //contains all prelim matches, sorted by match position
    private final NavigableMap<Integer, Match> allActiveWinnersMatches = new TreeMap<>(); //contains all matches that have yet to be resolved, sorted by match position
    private final NavigableMap<Integer, Match> allActiveLosersMatches = new TreeMap<>(); //contains all losers matches yet to be resolved, sorted by match position
    private int initialPoolSize = 0;
    private final Integer poolStage;

    // this will need to call jdbcConnector to see if id exists.

    public Pool(String poolName, Bracket bracket, Integer stage, PoolQueries poolQueries) {
        this.poolName = poolName;
        this.parentBracket = bracket;
        this.poolStage = stage;
        this.poolQueries = poolQueries;
        this.poolQueries.insertPoolToDB(bracket.getParentTournament().getTournamentID(), bracket.getGetBracketID(), this);
    }


    public void addWinnersMatch(Match newWinnersMatch) {
        winnersSide.put(newWinnersMatch.matchPosition, newWinnersMatch);
        allActiveWinnersMatches.put(newWinnersMatch.getMatchPosition(), newWinnersMatch);

        //Add match histories to existing players in the match
        if(newWinnersMatch.getP1() != null && newWinnersMatch.getP2() != null) {
            newWinnersMatch.getP1().updateBracketMatchHistory(newWinnersMatch.getMatchGame(), newWinnersMatch);
            newWinnersMatch.getP2().updateBracketMatchHistory(newWinnersMatch.getMatchGame(), newWinnersMatch);
        }
        //if one of the player positions are currntly empty, just add to the other player position
        else if (newWinnersMatch.getP1() != null) newWinnersMatch.getP1().updateBracketMatchHistory(newWinnersMatch.getMatchGame(), newWinnersMatch);
        else if (newWinnersMatch.getP2() != null) newWinnersMatch.getP2().updateBracketMatchHistory(newWinnersMatch.getMatchGame(), newWinnersMatch);
        parentBracket.getAllMatchesByID().put(newWinnersMatch.getMatchId(), newWinnersMatch);
    }

    public void addLosersMatch(Match newLosersMatch) {
        losersSide.put(newLosersMatch.matchPosition, newLosersMatch);
        allActiveLosersMatches.put(newLosersMatch.getMatchPosition(), newLosersMatch);

        if(newLosersMatch.getP1() != null && newLosersMatch.getP2() != null) {
            newLosersMatch.getP1().updateBracketMatchHistory(newLosersMatch.getMatchGame(), newLosersMatch);
            newLosersMatch.getP2().updateBracketMatchHistory(newLosersMatch.getMatchGame(), newLosersMatch);
        }
        //if one of the player positions are currntly empty, just add to the other player position
        else if (newLosersMatch.getP1() != null) newLosersMatch.getP1().updateBracketMatchHistory(newLosersMatch.getMatchGame(), newLosersMatch);
        else if (newLosersMatch.getP2() != null) newLosersMatch.getP2().updateBracketMatchHistory(newLosersMatch.getMatchGame(), newLosersMatch);
        parentBracket.getAllMatchesByID().put(newLosersMatch.getMatchId(), newLosersMatch);

        //detect if the matcch added is the final losers match of the pool. If so, set it to its proper field.
        int winnersRounds = (int) (Math.log(initialPoolSize) / Math.log(2)) + 1;
        if (!preliminaries.isEmpty()) {
            if (newLosersMatch.getMatchPosition() / 100 == winnersRounds + 1) this.losersFinalMatch = newLosersMatch;
        }
        else if (newLosersMatch.getMatchPosition() / 100 == winnersRounds + 2) this.losersFinalMatch = newLosersMatch;
    }

    public void addPlayInMatch(Match newPrelimsMatch) {
        preliminaries.put(newPrelimsMatch.matchPosition, newPrelimsMatch);
        allActiveWinnersMatches.put(newPrelimsMatch.getMatchPosition(), newPrelimsMatch);

        if(newPrelimsMatch.getP1() != null && newPrelimsMatch.getP2() != null) {
            newPrelimsMatch.getP1().updateBracketMatchHistory(newPrelimsMatch.getMatchGame(), newPrelimsMatch);
            newPrelimsMatch.getP2().updateBracketMatchHistory(newPrelimsMatch.getMatchGame(), newPrelimsMatch);
        }
        //if one of the player positions are currntly empty, just add to the other player position
        else if (newPrelimsMatch.getP1() != null) newPrelimsMatch.getP1().updateBracketMatchHistory(newPrelimsMatch.getMatchGame(), newPrelimsMatch);
        else if (newPrelimsMatch.getP2() != null) newPrelimsMatch.getP2().updateBracketMatchHistory(newPrelimsMatch.getMatchGame(), newPrelimsMatch);
        parentBracket.getAllMatchesByID().put(newPrelimsMatch.getMatchId(), newPrelimsMatch);
    }

    public boolean isFinished() { return winnersFinished && losersFinished; }

    public boolean isWinnersFinished() { return winnersFinished; }

    public boolean isLosersFinished() {return losersFinished; }

    public boolean isPrelimsFinished() {return prelimsFinished; }

    public void resolveMatch(Match match) {
        if (match.getMatchSide().equals("winners") || match.getMatchSide().equals("preliminaries")) allActiveWinnersMatches.remove(match.getMatchPosition());
        else allActiveLosersMatches.remove(match.getMatchPosition());
    }

    private Integer findRoundOneLosersPosition(Match match) {
        int prevMatchSubPosition = match.getMatchPosition() % 100;
        Integer losersMatchPosition = 101;

        switch (preliminaries.size()) {

            case 1:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1, 2 -> 204;
                    case 3, 4 -> 203;
                    case 5, 6 -> 202;
                    case 7 -> 201;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;

            case 2:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1, 2 -> 204;
                    case 3 -> 203;
                    case 4 -> 103;
                    case 5, 6 -> 202;
                    case 7 -> 201;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;

            case 3:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 204;
                    case 2 -> 104;
                    case 3 -> 203;
                    case 4 -> 103;
                    case 5, 6 -> 202;
                    case 7 -> 201;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;

            case 4:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 204;
                    case 2 -> 104;
                    case 3 -> 203;
                    case 4 -> 103;
                    case 5 -> 202;
                    case 6 -> 102;
                    case 7 -> 201;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;

            case 5:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 204;
                    case 2 -> 105;
                    case 3 -> 203;
                    case 4 -> 104;
                    case 5 -> 103;
                    case 6 -> 102;
                    case 7 -> 201;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;

            case 6:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 106;
                    case 2 -> 105;
                    case 3 -> 203;
                    case 4 -> 104;
                    case 5 -> 103;
                    case 6 -> 102;
                    case 7 -> 201;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;

            case 7:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 107;
                    case 2 -> 106;
                    case 3 -> 105;
                    case 4 -> 104;
                    case 5 -> 103;
                    case 6 -> 102;
                    case 7 -> 201;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;

            case 8:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 108;
                    case 2 -> 107;
                    case 3 -> 106;
                    case 4 -> 105;
                    case 5 -> 104;
                    case 6 -> 103;
                    case 7 -> 102;
                    case 8 -> 101;
                    default -> losersMatchPosition;
                };
                break;
            default:
                losersMatchPosition = 101;
                break;
        }

        return losersMatchPosition;
    }

    //need to change later for scalability of pools greater than base size of 16 players.
    public Integer findPrelimLosersPosition(Match match) {
        int prevMatchSubPosition = match.getMatchPosition() % 100;
        Integer losersMatchPosition = 101;

        switch (getPreliminaries().size()) {
            case 2:
                if (prevMatchSubPosition == 1) losersMatchPosition = 101;
                else losersMatchPosition = 103;
                break;

            case 3:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 101;
                    case 5 -> 103;
                    case 7 -> 104;
                    default -> losersMatchPosition;
                };
                break;

            case 4:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 101;
                    case 3 -> 102;
                    case 5 -> 103;
                    case 7 -> 104;
                    default -> losersMatchPosition;
                };
                break;

            case 5:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 101;
                    case 3 -> 102;
                    case 4 -> 103;
                    case 5 -> 104;
                    case 7 -> 105;
                    default -> losersMatchPosition;
                };
                break;

            case 6:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 101;
                    case 3 -> 102;
                    case 4 -> 103;
                    case 5 -> 104;
                    case 6 -> 105;
                    case 7 -> 106;
                    default -> losersMatchPosition;
                };
                break;

            case 7:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 101;
                    case 3 -> 102;
                    case 4 -> 103;
                    case 5 -> 104;
                    case 6 -> 105;
                    case 7 -> 106;
                    case 8 -> 107;
                    default -> losersMatchPosition;
                };
                break;

            case 8:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 101;
                    case 2 -> 102;
                    case 3 -> 103;
                    case 4 -> 104;
                    case 5 -> 105;
                    case 6 -> 106;
                    case 7 -> 107;
                    case 8 -> 108;
                    default -> losersMatchPosition;
                };
                break;
        }
        return losersMatchPosition;
    }



    public Integer findLosersConsolidationPosition(Match match) {
        int prevMatchSubPosition = match.getMatchPosition() % 100;
        Integer losersMatchPosition = 101;

        switch (getPreliminaries().size()) {
            case 2:
                if (prevMatchSubPosition == 1)
                    losersMatchPosition = 201;
                else
                    losersMatchPosition = 203;
                break;

            case 3:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 201;
                    case 3 -> 203;
                    case 4 -> 204;
                    default -> losersMatchPosition;
                };
                break;

            case 4:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 201;
                    case 2 -> 202;
                    case 3 -> 203;
                    case 4 -> 204;
                    default -> losersMatchPosition;
                };
                break;

            case 5:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 201;
                    case 2, 3 -> 202;
                    case 4 -> 203;
                    case 5 -> 204;
                    default -> losersMatchPosition;
                };
                break;

            case 6:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 201;
                    case 2, 3 -> 202;
                    case 4, 5 -> 203;
                    case 6 -> 204;
                    default -> losersMatchPosition;
                };
                break;

            case 7:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1 -> 201;
                    case 2, 3 -> 202;
                    case 4, 5 -> 203;
                    case 6, 7 -> 204;
                    default -> losersMatchPosition;
                };
                break;

            case 8:
                losersMatchPosition = switch (prevMatchSubPosition) {
                    case 1, 2 -> 201;
                    case 3, 4 -> 202;
                    case 5, 6 -> 203;
                    case 7, 8 -> 204;
                    default -> losersMatchPosition;
                };
                break;
        }
        return losersMatchPosition;
    }


    public void matchUpdateWinners(Match prevMatch, MatchQueries matchQueries) {
        resolveMatch(prevMatch);
        String gameName = parentBracket.getGameName();
        Player winner = prevMatch.getWinner();
        Player loser = prevMatch.getLoser();
        //always remove the previous match from active matches

        //Adjust the points of the players of the match here
        winner.pointsChangeMatch(gameName, loser, 1);
        loser.pointsChangeMatch(gameName, winner, 0);

        matchQueries.setMatchWinnerInDB(prevMatch.getMatchId(), winner.getPlayerID());
        matchQueries.setMatchLoserInDB(prevMatch.getMatchId(), loser.getPlayerID());

        // preliminary winner routes directly to round 1 at the same sub-position
        if (prevMatch.getMatchSide().equals("preliminaries")) {

            int prevMatchSubPosition = prevMatch.getMatchPosition();
            Integer newMatchPosition = 100 + prevMatchSubPosition;
            if (winnersSide.containsKey(newMatchPosition)) {
                Match pendingMatch = winnersSide.get(newMatchPosition);
                pendingMatch.setPlayer2(winner);
            }
            //segment to check and indicate if prelims are done or not. One active match makes this false.
            boolean prelimsDone = true;
            for (Match prelimMatch : preliminaries.values()) {
                if (prelimMatch.getWinner() == null) {
                    prelimsDone = false;
                    break;
                }
            }
            prelimsFinished = prelimsDone;

            return;
        }

        int prevMatchRound = prevMatch.getMatchPosition() / 100;

        if (allActiveWinnersMatches.isEmpty()) {
            winnersFinalist = winner;
            winnersFinished = true;
            return;
        }

        else {
            //grabs the  prevMatchSubPosition
            Integer prevMatchSubPosition = prevMatch.getMatchPosition() % 100;
            //gets the round for the match argument


            boolean subPositionIsEven = prevMatchSubPosition % 2 == 0;

            //every match has a sub-position. the whole number is the "round" which gets added to the decimal of the sub position
            if (!subPositionIsEven) prevMatchSubPosition += 1;
            prevMatchSubPosition /= 2;
            int newMatchRound = (prevMatchRound * 100) + 100;

            int newMatchPosition = prevMatchSubPosition + newMatchRound; //marked

            //if loser is coming from higher in the bracket, set winner to player 1.
            //if loser is coming from lower in the bracket, set winner to player 2.

            Match newMatch = winnersSide.get(newMatchPosition);

            if (subPositionIsEven) newMatch.setPlayer2(winner);
            else newMatch.setPlayer1(winner);
        }
    }

    public void matchUpdateLosers(Match prevMatch, MatchQueries matchQueries) {
        resolveMatch(prevMatch);
        String gameName = parentBracket.getGameName();
        Player winner = prevMatch.getWinner();
        Player loser = prevMatch.getLoser();
        int prevMatchSubPosition = prevMatch.getMatchPosition() % 100;
        int prevRound = prevMatch.getMatchPosition() / 100;
        Integer losersMatchPosition;
        boolean prevPositionIsEven = prevMatchSubPosition % 2 == 0;

        // losers survivor advancing — uses its own position calculation
        prevMatch.getWinner().pointsChangeMatch(gameName, loser, 1);
        prevMatch.getLoser().pointsChangeMatch(gameName, winner, 0);

        matchQueries.setMatchWinnerInDB(prevMatch.getMatchId(), winner.getPlayerID());
        matchQueries.setMatchLoserInDB(prevMatch.getMatchId(), loser.getPlayerID());

        // preliminaries drop to LR1
        if (prevMatch.getMatchSide().equals("preliminaries")) {
            losersMatchPosition = findPrelimLosersPosition(prevMatch);
            Match newLosersMatch = new Match(loser, losersMatchPosition, 2, this, "losers", parentBracket, matchQueries);
            newLosersMatch.setActualMatchRound(1);
            addLosersMatch(newLosersMatch);
            return;
        }

        // calculate the amount of rounds in the pool to detect the losers finalist
        int winnersRounds = (int) (Math.log(this.initialPoolSize) / Math.log(2)) + 1;

        //**TODO Need to handle this case as well. unless it is a grand fnals match, this losers finalist proceeds to next pool

        if (allActiveLosersMatches.isEmpty()) {
            losersFinalist = winner;
            losersFinished = true;
            return;
        }

        //processes losers dropping from winners
        else if (prevMatch.getMatchSide().equals("winners")) {

            // Special routing for W1 when prelim count is odd.
            if (prevRound == 2 && poolStage == 1 && !preliminaries.isEmpty()) losersMatchPosition = findRoundOneLosersPosition(prevMatch);

            else if (prevRound == 1 && poolStage == 1 && preliminaries.isEmpty()) losersMatchPosition = 100 + (initialPoolSize / 2) - ((prevMatchSubPosition - 1) / 2);

            else if (allActiveLosersMatches.size() == 1) {
                Match existingLosersMatch = allActiveLosersMatches.get(allActiveLosersMatches.firstKey());
                if (existingLosersMatch.getP1() == null) existingLosersMatch.setPlayer1(winner);
                else existingLosersMatch.setPlayer2(winner);
                return;
            }

            // Winners round drop ins after the first actual winners rounds
            else {
                int currentRoundMatchCount;

                int exponent;
                // Winners round drop routing
                if(poolStage == 1) {
                    exponent = Math.max(1, prevRound - 2);
                    currentRoundMatchCount = initialPoolSize >> exponent;
                    if (!preliminaries.isEmpty()) {
                        losersMatchPosition = switch (prevRound) {
                            case 3 -> 300 + (currentRoundMatchCount - prevMatchSubPosition + 1);
                            case 4 -> 500 + (currentRoundMatchCount - prevMatchSubPosition + 1);
                            case 5 -> 700 + (currentRoundMatchCount - prevMatchSubPosition + 1);
                            default -> 100 * (prevRound + 1) + (currentRoundMatchCount - prevMatchSubPosition + 1);
                        };
                    }
                    else losersMatchPosition = 100 * (prevRound + 1) + (currentRoundMatchCount - prevMatchSubPosition + 1);
                }
//                losersMatchPosition = 100 * (prevRound + 2) + (currentRoundMatchCount - prevMatchSubPosition + 1);
                //Stage is greater than 2 so need to compensate for pre-populated losers rounds.
                else {
                    exponent = Math.max(1, prevRound - 1);
                    currentRoundMatchCount = (this.initialPoolSize >> exponent);
                    if (prevRound == 1) currentRoundMatchCount *= 2;
                    losersMatchPosition = 100 * (prevRound + prevRound) + (currentRoundMatchCount - prevMatchSubPosition + 1);
                }
            }

            Match existingLosersMatch = this.losersSide.get(losersMatchPosition);

            this.losersSide.get(losersMatchPosition - 1);


            if (!this.preliminaries.isEmpty() && losersMatchPosition / 100 == 1) existingLosersMatch.setPlayer1(loser);

            else {
                //if there is no previous losers match feeding into losers round 2, perform special case
                if (prevRound == 1 && !this.preliminaries.isEmpty() && this.losersSide.get(losersMatchPosition - 100) == null) {
                    if (prevPositionIsEven) existingLosersMatch.setPlayer2(loser);

                    else existingLosersMatch.setPlayer1(loser);
                }


                if (existingLosersMatch.getP1() == null) existingLosersMatch.setPlayer1(loser);

                else existingLosersMatch.setPlayer2(loser);

            }
        }

        // Losers consolidation rounds
        else {
            //If its prelims then we have to do special cases.

            int poolExponent = Integer.numberOfTrailingZeros(this.initialPoolSize);
            if (poolStage == 1) {
                if (!this.preliminaries.isEmpty() && prevRound == 1) {
                    losersMatchPosition = findLosersConsolidationPosition(prevMatch);
                }
                else {
                    if (!this.preliminaries.isEmpty()) {
                        if (prevRound % 2 != 0) {
                            if (!prevPositionIsEven) prevMatchSubPosition += 1;
                            prevMatchSubPosition = prevMatchSubPosition / 2;
                        }
                    } else {
                        // Whether this round pairs two prior matches into one depends on
                        // initialPoolSize, not just prevRound's raw parity — smaller pools
                        // hit the consolidation step a round earlier.
                        if ((prevRound + poolExponent) % 2 != 0) {
                            if (!prevPositionIsEven) prevMatchSubPosition += 1;
                            prevMatchSubPosition = prevMatchSubPosition / 2;
                        }
                    }
                    losersMatchPosition = 100 * (prevRound + 1) + prevMatchSubPosition;
                }
            }
            else {
                if (prevRound % 2 == 0) {
                    if (!prevPositionIsEven) prevMatchSubPosition += 1;
                    prevMatchSubPosition = prevMatchSubPosition / 2;
                }
                losersMatchPosition = 100 * (prevRound + 1) + prevMatchSubPosition;
            }


            Match existingLosersMatch = this.losersSide.get(losersMatchPosition);

            if (existingLosersMatch.getP2() == null) existingLosersMatch.setPlayer2(winner);

            else existingLosersMatch.setPlayer1(winner);

            //handles background work such as adding the match to the player history, updating active matches, and adding the match to the parent bracket.
        }
    }





    public void incrementInitialPoolSize() { initialPoolSize++; }

    public Player getWinnersSideFinalist() { return winnersFinalist; }

    public Player getLosersSideFinalist() { return losersFinalist; }

    public Integer getPoolStage() {return poolStage; }

    public NavigableMap<Integer, Match> getWinnersSide() { return winnersSide; }

    public NavigableMap<Integer, Match> getLosersSide() { return losersSide; }

    public NavigableMap<Integer, Match> getPreliminaries() { return preliminaries; }

    public int getInitialPoolSize() { return initialPoolSize; }

    public Bracket getParentBracket() { return parentBracket; }

    public Integer getPoolNumber() { return Integer.parseInt(poolName.split(" ")[1]); }

    public Long getPoolID() { return poolID; }

    @Override
    public String toString() {
        StringBuilder poolString = new StringBuilder();
        poolString.append("\n\n*****" + poolName + " | Stage " + poolStage + "*****");
        if (!preliminaries.isEmpty()) {
            poolString.append("\n\n***Preliminaries*** \n\n");
            for (Map.Entry playInsMatch : preliminaries.entrySet()) poolString.append(playInsMatch.getValue().toString());
        }
        poolString.append("\n\n***Winner's Side*** \n\n");
        for (Map.Entry winnersMatch : winnersSide.entrySet()) poolString.append(winnersMatch.getValue().toString());


        if (!losersSide.isEmpty()) {
            poolString.append("\n\n***Loser's Side*** \n\n");
            for (Map.Entry losersMatch : losersSide.entrySet()) poolString.append(losersMatch.getValue().toString());
        }
        return poolString.toString();
    }

}