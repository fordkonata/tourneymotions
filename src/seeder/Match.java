package seeder;
import java.util.Random;

/***TODO
* Needs to handle point acquisition and reduction with a method
 */
public final class Match {
    public Integer matchPosition;
    private Player player2 = null;
    private Player player1 = null;
    private Player winner = null;
    private Player loser;
    private Integer matchId;
    private int actualMatchRound = 1;
    private final String matchGame;
    private final String matchSide;
    private final Pool parentPool; // need to organize at set a reference for parent pool probably need to shift some things
    private final Random random = new Random();
    private final Bracket parentBracket;
    public static int total_matches_globally = 0;

    private void generateMatchId() {
        Integer id;
        //random number generator for matchID for a bracket.
        do { id = random.nextInt(99999); }
        while (this.parentBracket.getAllMatchesByID().containsKey(id));
        this.parentBracket.getAllMatchesByID().put(id, this);
        this.matchId =id;
    }

    //constructor for placeholder matches
    public Match(Integer matchPosition, Pool pool, String side, Bracket bracket) {
        this.parentPool = pool;
        this.matchSide = side;
        this.matchPosition = matchPosition;
        this.parentBracket = bracket;
        this.matchGame = parentPool.parentbracket.getGameName();
        this.generateMatchId();
        total_matches_globally++;
    }

    //Match constructor for 2 players at the same time
    public Match(Player p1, Player p2, Integer matchPosition, Pool pool, String side, Bracket bracket) {
        this.player1 = p1;
        this.player2 = p2;
        this.matchPosition = matchPosition;;
        this.parentPool = pool;
        this.matchSide = side;
        this.parentBracket = bracket;
        this.matchGame = parentPool.parentbracket.getGameName();
        this.generateMatchId();
        total_matches_globally++;
    }

    //Match constructor for 1 player at a time if matches are asynchronized
    public Match(Player player, Integer matchPosition, Integer playerPos, Pool pool, String side, Bracket bracket) {
        if (playerPos ==  1) this.player1 = player;
        else this.player2 = player;
        this.parentPool = pool;
        this.matchSide = side;
        this.matchPosition = matchPosition;
        this.parentBracket = bracket;
        this.matchGame = parentPool.parentbracket.getGameName();
        this.generateMatchId();
        total_matches_globally++;
    }


    public Player getP1() { return this.player1; }

    public Player getP2() { return this.player2; }

    public Player getWinner() { return this.winner; }

    public Player getLoser() { return this.loser; }

    public Integer getMatchId() { return this.matchId; }

    public String getMatchSide() { return this.matchSide; }

    public Integer getMatchPosition() { return this.matchPosition; }

    public Pool getParentPool() { return this.parentPool; }

    public String getMatchGame() {return this.matchGame; }

    public Integer getMatchRound() {return this.matchPosition / 100; }

    public void setPlayer1(Player p1) { this.player1 = p1; }

    public void setPlayer2(Player p2) { this.player2 = p2; }



    public void setWinner(Player winner) {
        if (winner == player1) {
            this.winner = player1;
            this.loser = player2;
            return;
        }
        else if (winner == player2) {
            this.winner = player2;
            this.loser = player1;
            return;
        }
        System.out.println("Player not in  match");
    }

    public void setActualMatchRound(int round) { this.actualMatchRound = round; }

    public int getActualMatchRound() { return this.actualMatchRound; }

    @Override
    public String toString() {
        return "|| Stage: " + this.parentPool.getPoolStage() + " | " + this.parentPool.poolName + " | " + matchSide + " | " + "Match Position: " + matchPosition + " ||" +
                "\n|| " + (player1 != null ? player1.getNickname() : "TBD") + " vs. " + (player2 != null ? player2.getNickname() : "TBD") + " ||\n";
    }

}