package seeder;

import java.lang.Math;
import java.util.*;
import java.util.Random;

/*NEEDS:
 * phone number from users to verify integrity of tournaments
 * security to protect users from cyberhacks
 */
public final class Player implements Comparable<Player> {
    public String playerNickname;
    public static HashMap<Integer, Player> globalPlayerMap = new HashMap<>(); // contains ALL THE PLAYERS ACROSS ALL GAMES
    private String realName = "empty";
    private int exp_factor;
    private int setsPlayed = 0;
    private int phone_number;
    private HashMap <String, Integer> tempPointsValues = new HashMap<>();
    public final HashMap<String, ArrayList<Match>> matchHistory = new HashMap<>(); //Need to change this so that I can filter and sort by, tournament,  opposing player tier, and points gained
    public final HashMap<String, Integer> playerPointsMap = new HashMap<>(); //contains the players points and tier for each game. The first index in a hash value is the points, the second is the tier.
    public final HashMap<String, Integer> playerTierMap = new HashMap<>();//holds the player tier for each game of the player.
    private final HashMap<String, Integer> playerPlacementMap = new HashMap<>();
    private final Random random = new Random();
    private final HashMap<String, Integer> tournamentMatchesSum = new HashMap<>(); //holds the sum of the player's points until the bracket is over
    private final Integer player_id; //maybe make a global field for player_id, so I can cast rand to it?


    private Integer generatePlayerId() {
        Integer id;
        //random number generator for matchID for a player.
        do { id = random.nextInt(999999999); }
        while (globalPlayerMap.containsKey(id));
        globalPlayerMap.put(id, this);
        return id;
    }

    // **IMPORTANT NOTE: this.playerPointsMap.get(game).get(0), is the player's points for a specific game. || this.playerPointsMap.get(game).get(1) is the player's Tier for a specific game
    public Player(String playerName, String playerNickname) {
        this.realName = playerName;
        this.playerNickname = playerNickname;
        this.playerPointsMap.put("Street Fighter 6", 800);
        this.playerTierMap.put("Street Fighter 6", 7);
        this.player_id = generatePlayerId();
        globalPlayerMap.put(player_id, this);
    }

    @Override
    public int compareTo(Player other) {
        // Sort ascending by points for the game
        int pointsCompare = Integer.compare(
                this.getPoints("Street Fighter 6"),
                other.getPoints("Street Fighter 6")
        );

        // Tiebreaker by nickname so no two players are ever considered equal
        if (pointsCompare == 0) return this.getNickname().compareTo(other.getNickname());
        return pointsCompare;
    }


    //Calculates a percentage modifier based on matches played
    private double experienceFactorCalculator(String gameName) {
        return Math.round(1 + (1.3 / (1+((8- this.playerTierMap.get(gameName)/1.2)*(Math.log(setsPlayed))))));
    }

    // Integer currPoints = this.playerPointsMap.get(game).get(0);
    //        this.playerPointsMap.get(game).set(0, currPoints += pointsChange);


    //adjusts tournament points after player is finished with tournament
    private int changePointsMatchHelper(String gameName, int pointsChange) { //change here to include matches
        if (pointsChange < -30) pointsChange = -30;
        else if (pointsChange > 30 ) pointsChange = 30;
        else if (pointsChange < 5 && pointsChange > 0) pointsChange = 5;
        return pointsChange;
    }

    //adjust points after bracket match
    public void pointsChangeMatch(String gameName, Player opponent, int resultVal) {
        Integer playerPoints = this.playerPointsMap.get(gameName);
        double expMultiplier = experienceFactorCalculator(gameName);

        //expected percentage result of the player
        double expectedResult = (1 / (1 + Math.pow(10, -1 * ((double) (playerPoints - opponent.getPoints(gameName)) / 500))));

        //actual elo diff calculator to determine points gained or lost from the match
        int pointsChange = (int) (40 * (resultVal - expectedResult) * expMultiplier);

        Integer matchPointsFinal = this.changePointsMatchHelper(gameName, pointsChange);
        tournamentMatchesSum.merge(gameName, matchPointsFinal, Integer::sum);

    }

    //adjust points after a Player's tournament run is over
    public void finalPointsChange(String gameName, int finalChange) {
        Integer matchesPointsSum = this.tournamentMatchesSum.get(gameName);
        this.tempPointsValues.merge(gameName, matchesPointsSum + finalChange, Integer::sum);
    }

    public void applyFinalPoints(String gameName) { this.playerPointsMap.put(gameName, tempPointsValues.get(gameName));}




    public void setPlayerTier(String gameName) {
        List<Integer> pointThresholds = List.of(1000, 1200, 1400, 1600, 1800, 2000);
        Integer count = 7;
        Integer playerPoints = playerPointsMap.get(gameName);

        for (Integer threshold : pointThresholds) {
            if (playerPoints < threshold) {
                playerTierMap.put(gameName, count);
                break;
            }
            count--;
        }
        // player passed all thresholds, they are tier 1
        if (count == 1) playerTierMap.put(gameName, 1);
    }

    public Integer getPoints(String gameName) { return playerPointsMap.get(gameName); }


    //public int getPointsPreTournament() { return pointsPreTournament; }

    public String getNickname() { return playerNickname; }

    public Integer getPlayerPlacement(String gameName) {return this.playerPlacementMap.get(gameName); }

    public int getTier(String gameName) { return playerPointsMap.get(gameName); }

    public ArrayList<Match> getGameMatchHistory(String gameName) { return this.matchHistory.get(gameName); }

    //If the match history list doesn't exist, make a new one and add it. Otherwise, add it to the existing list.
    public void updateMatchHistory(String gameName, Match addingMatch) { matchHistory.computeIfAbsent(gameName, p -> new ArrayList<>()).add(addingMatch); }


    //public static void setRealName(String real_name) { Player.realName = real_name; }

    //Actually sets players points and replaces the previous value.
    public void setPlayerPoints( String gameName, Integer newPoints) {
        this.tempPointsValues.put(gameName, newPoints);
        this.setPlayerTier(gameName);
    }

    public void setPlayerPlacement(String gameName, Integer position) { this.tempPointsValues.put(gameName, position); }

    @Override
    public String toString() {
        ArrayList<String> printArray = new ArrayList<>();
        for (Map.Entry<String, Integer> pointsEntry : playerPointsMap.entrySet()) {
            for(Map.Entry<String, Integer> tierEntry : playerTierMap.entrySet()) {

                //Prints out all of the player's entry in the map
                printArray.add("Game: " + pointsEntry.getKey() + " | Points: " + pointsEntry.getValue() + " | Tier: " + tierEntry.getValue());
            }
        }
        return "Player: " + this.playerNickname + " " + printArray.toString();
    }

}




/* Players to enter for SF6
    * Nickname: Punk | Tier: 1 | Points 2000
    * Nickname: Leshar | Tier: 1 | Points 2000
    * Nickname: MenaRD | Tier: 1 | Points 2000
    * Nickname: Xiaohai | Tier: 1 | Points: 2000
    * Nickname: Fuudo | Tier: 1 | Points 2000
    * Nickname: Kilzyou | Tier 1 | Points 2000
    * Nickname: Blaz | Tier: 1 | Points 2000
    * Nickname: Higuchi | Tier 1 | Points 2000
    * Nickname: Yamaguchi | Tier: 1 | Points 2000
    * Nickname: Kobayan | Tier: 1 | Points 2000
    * Nickname: Dual Kevin | Tier: 1 | Points 2000
    * Nickname: BigBird | Tier: 1 | Points 2000 (possibly tier 2?)
    * Nickname: Sahara | Tier: 1 | Points 2000 (nearly in tier 2)
    * Nickname: AngryBird | Tier: 1 | Points 2000
    * Nickname: Problem X | Tier: 2 | Points 1800
    * Nickname: Vxbao | Tier: 2 | Points 1800
    * Nickname: Phenom | Tier: 2 | Points 1800 (barely outside of tier 2 imo)
    * Nickname: Mister Crimson | Tier: 2 | Points 1800
    * Nickname: EndingWalker | Tier: 2 | Points 1800
    * Nickname: Xian | Tier: 2 | Points 1800
    * Nickname: Kawano | Tier: 2 | Points 1800
    * Nickname: Itabashi Zangief | Tier: 2 | Points 1800
    * Nickname: Zhen | Tier: 2 | Points 1800
    * Nickname: Moke | Tier: 2 | Points 1800
    * Nickname: Go1 | Tier: 2 | Points 1800
    * Nickname: NL | Tier: 2 | Points 1800
    * Nickname: NoahTheProdigy | Tier: 2 | Points 1800
    * Nickname: Chris Wong | Tier: 2 | Points 1800
    * Nickname: Bonchan | Tier 2 | Points 1800
    * Nickname: Shuto | Tier: 2 | Points 1800
    * Nickname: Kakeru | Tier: 2 | Points 1800 (tier 2 demotion due to inactivity)
    * Nickname: DCQ | Tier: 2  | Points 1800
    * Nickname: Micky | Tier: 2 | Points 1800
    * Nickname: Momochi | Tier: 2 | Points 1800
    * Nickname: Kusanagi | Tier 2 | Points 1800
    * Nickname: Caba | Tier 2 | Points 1800
    * Nickname: Nephew | Tier 2 | Points 1800
    * Nickname: Nuckledu | Tier 2 | Points 1800
    * Nickname: Ryukichi | Tier: 2 | Points 1800
    * Nickname: Tokido | Tier 2 | Points 1800
    * Nickname: Matsu56 | Tier: 2 | Points 1800
    * Nickname: Oil King | Tier: 2 | Points 1800
    * Nickname: ChrisT | Tier: 2 | Points 1800
    * Nickname: Pugera | Tier: 2 | Points 1800
    * Nickname: Shine | Tier: 2 | Points 1800
    * Nickname: Bravery | Tier:  3 | Points 1600 (possibly tier 2?
    * Nickname: BNBBN | Tier: 3 | Points 1600
    * Nickname: Hotdog29 | Tier: 3 | Points 1600 (quite possibly tier 2?)
    * Nickname: Takamura | Tier: 3 | Points 1600
    * Nickname: Hikaru | Tier: 3 | Points 1600
    * Nickname: Mochi | Tier: 3 | Points 1600
    * Nickname: Mago | Tier: 3 | Points 1600
    * Nickname: John Takeuchi | Tier: 3 | Points 1600
    * Nickname: Pugera | Tier: 3 | Points 1600
    * Nickname: Haitani | Tier: 3 | Points 1600
    * Nickname: Craime | Tier: 3 | Points 1600
    * Nickname: Torimeshi | Tier: 3 | Points 1600
    * Nickname: Tachikawa | Tier: 3 | Points 1600
    * Nickname: JuicyJoe | Tier: 3 | Points 1600
    * Nickname: Daigo | Tier: 3 | Points 1800
    * Nickname: Idom | Tier: 3 | Points 1600
    * Nickname: Salvatore | Tier 3 | Points 1600
    * NIckname: Yanai | Tier: 3 | Points 1600
    * Nickname: ElChokotay | Tier: 3 | Points 1600
    * Nickname: JAK | Tier: 3 | Points 1600
    * Nickname: ChrisCCH | Tier: 3 | Points 1600
    * NIckname: Hurricane | Tier: 3 | Points 1600
    * Nickname: Acqua | Tier: 3 | Points 1600
    * Nickname: DakCorgi | Tier: 3 | Points 1600
    * Nickname: Armperor | Tier: 3 | Points 1600
    * Nickname: Lexx | Tier: 3 | Points 1600
    * Nickname: Cosa | Tier: 3 | Points 1600
    * Nickname: Fentritti | Tier: 3 | Points 1600
    * Nickname: EnzoTheHokage | Tier: 3 | Points 1600
    * Nickname: Sayff | Tier: 3 | Points 1600
    * Nickname: Haitani | Tier 3 | 1600
    * Nickname: Xerna | Tier: 3 | 1600
    * Nickname: Psycho | Tier: 3 | Points 1600
    * Nickname: Kazunoko | Tier 3 | Points 1600
    * Nickname: Broski | Tier: 3 | Points 1600
    * Nickname: NotPedro | Tier 3 | Points 1600
    * Nickname: Riddles | Tier: 3 | Points 1600
    * Nickname: Hibiki | Tier: 3 | Points 1600
    * Nickname: Booce | Tier 3 | Points 1600
    * Nickname: Nemo | Tier: 3 | Points 1600
    * Nickname: KojiKog | Tier: 3 | Points 1600
    * Nickname: NYChrisG | Tier: 3| Points 1600
    * Nickname: Akira | Tier: 3 | Points 1600
    * Nickname: JB | Tier: 3 | Points 1600
    * Nickname: Joe Umerogan | Tier: 3 | Points 1600
    * Nickname: Zangief Bolado | Tier 3 | Points 1600
    * Nicname: Akainu | Tier: 3 | Points 1600
    * Nickname: Stealth | Tier 3 | Points 1600
    * Nickname: Kincho | Tier: 3 | Points 1600
    * Nickname: Joey | Tier: 3 | Points 1600
    * Nickname: Dogura | Tier: 3 | Points 1600
    * Nickname: Kingsvega | Tier: 3 | Points 1600
    * Nickname: UrielVelorio | Tier: 3 | Points 1600
    * Nickname: Squall | Tier: 3 | Points 1600
    * Nickname: Mizuha | Tier: 3 | points 1600
    * Nickname: Inaba | Tier: 3 | Points 1600
    * Nickname: CrossoverRD | Tier: 3 | Points 1600
    * Nickname Valmaster | Tier: 3 | Points 1600
    * Nickname: Otani | Tier: 3 | Points 1600
    * Nickname: Bloo | Tier: 3 | Points 1600
    * Nickname: Mono | Tier: 4 | Points 1400
    * Nickname: PipoKun | Tier: 4 | Points 1400
    * Nickname: Nyanpi | Tier: 4 | Points 1400
    * Nickname: Ryusei | Tier: 4 | Points 1400
    * Nickname: Nishikin | Tier: 4 | Points 1400
    * Nickname: GuyGuy | Tier: 4 | Points 1400
    * Nickname: Luffy | Tier: 4 | Points 1400
    * Nickname: Jabhim | Tier: 4 | Points 1400
    * Nickname: Seiya | Tier: 4 | Points 1400
    * Nickname: Brian F | Tier: 4 | Points 1400
    * Nickname: Motchan | Tier: 4 | Points 1400
    * Nickname: Space Boy | Tier: 4 | Points 1400
    * Nickname: GuyGuy | Tier: 4 | Points 1400
    * Nickname: Kayne | Tier: 4 | Points 1400
    * Nickname: Akutagawa | Tier: 4 | Points 1400
    * Nickname: CamPaine | Tier: 4 | Points 1400
    * Nickname: Takagi | Tier: 4 | Ponts 1400
    * Nickname: S4ltyKid | Tier: 4 | Points 1400
    * Nickname: Kharsonist | Tier: 4 | Points 1400
    * Nickname: Adelie | Tier: 4 | Points 1400
    * Nickname: Ryan Hart | Tier 4 | Points 1400
    * Nickname: Diaphone | Tier: 4 | Points: 1400
    * Nickname: Kami | Tier: 4 | Points 1400
    * Nickname: Harumi | Tier: 4 | Points: 1400
    * Nickname: 801 Strider | Tier:  4 | Points 1400
    * Nickname: Zabeth | Tier: 4 | Points 1400
    * Nickname: Eguto | Tier: 4 | Points 1400
    * Nickname: YangMian | Tier: 4 | Points 1400
    * Nickname: SnakeEyez | Tier: 4 | Points 1400
    * Nickname: Fierce | Tier: 4 | Points 1400
    * Nickname: 2BASSA | Tier: 4  | Points 1400
    * Nickname: ScrawtVermillion | Tier: 4 | Points 1400
    * Nickname: Alphen | Tier 4 | Points 1400
    * Nickname: Jaccy | Tier: 5 | Points 1200
    * Nickname: SonicFox | Tier: 5 | Points 1200
    * Nickname: DomaXD | Tier: 5 | Points 1200
    * Nickname: Taisei | Tier: 5 | Points 1200
    * Nickname: Jackie Tan | Tier: 5 | Points 1200
    * Nickname: Hamood | Tier: 5 | Points 1200
    * Nickname: Kira | Tier: 5 | Points 1200
    * Nickname: Abood bboy | Tier 5 | Points 1200
    * Nickname: Tako | Tier: 5 | Points 1200
    * Nickname: Dubz | Tier: 6 | Points 1000
    * Nickname: Malton NEO! | Tier: 7 | Points 800
    * Nickname" MetalOrca | Tier: 7 | Points 800
 */