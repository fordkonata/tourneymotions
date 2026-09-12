package backend;

import backend.dbconnector.MatchQueries;

/***TODO
* Needs to handle point acquisition and reduction with a method
 */
public final class Match {
    public Integer matchPosition;
    private Player player2 = null;
    private Player player1 = null;
    private Player winner = null;
    private Player loser;
    private Long player1FromDBID = null;
    private Long player2FromDBID = null;
    private Long winnerFromDBID = null;
    private Long loserFromDBID = null;
    private Long matchID;
    private int actualMatchRound = 1;
    private String matchGame;
    private String matchSide;
    private Pool parentPool; // need to organize at set a reference for parent pool probably need to shift some things
    private final MatchQueries matchQueries;
    private Bracket parentBracket;


    //match constructor to retrieve a match from the DB
    public Match(long matchId, int matchPosition, String matchSide, MatchQueries matchQueries) { // constructor use exclusively when reconstructing an existing match from a DB
        this.matchID = matchId;
        this.matchPosition = matchPosition;
        this.matchSide = matchSide;
        this.matchQueries = matchQueries;
    }
    //constructor for placeholder matches
    public Match(Integer matchPosition, Pool pool, String side, Bracket bracket, MatchQueries matchQueries) {
        this.parentPool = pool;
        this.matchSide = side;
        this.matchPosition = matchPosition;
        this.parentBracket = bracket;
        this.matchGame = parentPool.parentBracket.getGameName();
        this.matchQueries = matchQueries;
        this.matchQueries.insertMatch(pool.getPoolID(), this);
    }

    //Match constructor for 2 players at the same time
    public Match(Player p1, Player p2, Integer matchPosition, Pool pool, String side, Bracket bracket, MatchQueries matchQueries) {
        this.player1 = p1;
        this.player2 = p2;
        this.matchPosition = matchPosition;
        this.parentPool = pool;
        this.matchSide = side;
        this.parentBracket = bracket;
        this.matchGame = parentPool.parentBracket.getGameName();
        this.matchQueries = matchQueries;
        this.matchQueries.insertMatch(pool.getPoolID(), this);
    }

    //Match constructor for 1 player at a time if matches are asynchronized
    public Match(Player player, Integer matchPosition, Integer playerPos, Pool pool, String side, Bracket bracket, MatchQueries matchQueries) {
        if (playerPos ==  1) this.player1 = player;
        else this.player2 = player;
        this.parentPool = pool;
        this.matchSide = side;
        this.matchPosition = matchPosition;
        this.parentBracket = bracket;
        this.matchGame = parentPool.parentBracket.getGameName();
        this.matchQueries = matchQueries;
        this.matchQueries.insertMatch(pool.getPoolID(), this);
    }


    public Player getP1() { return this.player1; }

    public Player getP2() { return this.player2; }

    public Player getWinner() { return this.winner; }

    public Player getLoser() { return this.loser; }

    public Long getMatchID() { return this.matchID; }

    public String getMatchSide() { return this.matchSide; }

    public Integer getMatchPosition() { return this.matchPosition; }

    public Pool getParentPool() { return this.parentPool; }

    public String getMatchGame() {return this.matchGame; }

    public int getMatchPoolStage() { return this.parentPool.getPoolStage(); }

    public Integer getMatchRound() {return this.matchPosition / 100; }

    public int getActualMatchRound() { return this.actualMatchRound; }

    public void setPlayer1(Player p1) {
        this.player1 = p1;
        p1.updateBracketMatchHistory(matchGame, this);
        matchQueries.setMatchPlayer(matchID, 1, p1.getPlayerID());
    }


    public void setPlayer2(Player p2) {
        this.player2 = p2;
        player2.updateBracketMatchHistory(matchGame, this);
        matchQueries.setMatchPlayer(matchID, 2, p2.getPlayerID());
    }



    public void setWinnerFromDB(long winner) {winnerFromDBID = winner; }

    public void setLoserFromDB(long loser) { loserFromDBID = loser; }

    public void setPlayer1FromDB(long playerID) { player1FromDBID = playerID; }


    public void setPlayer2FromDB(long playerID) { player2FromDBID = playerID; }


    public void setWinnerAndLoser(Player winner) {
        if (winner == player1) {
            this.winner = player1;
            this.loser = player2;
            matchQueries.setMatchWinnerInDB(matchID, player1.getPlayerID());
            matchQueries.setMatchLoserInDB(matchID, player2.getPlayerID());
            return;
        }
        else if (winner == player2) {
            this.winner = player2;
            this.loser = player1;
            matchQueries.setMatchWinnerInDB(matchID, player2.getPlayerID());
            matchQueries.setMatchLoserInDB(matchID, player1.getPlayerID());
            return;
        }
        System.out.println("Player not in  match");
    }

    public void setActualMatchRound(int round) { this.actualMatchRound = round; }


    public void setMatchSide(String matchSide) {this.matchSide = matchSide; }

    @Override
    public String toString() {
        return "|| Stage: " + this.parentPool.getPoolStage() + " | " + this.parentPool.getPoolName() + " | " + matchSide + " | " + "Match Position: " + matchPosition + " ||" +
                "\n|| " + (player1 != null ? player1.getNickname() : "TBD") + " vs. " + (player2 != null ? player2.getNickname() : "TBD") + " ||\n";
    }

//    public void setMatchID(long matchId) { this.matchId = matchId; }

    public void setMatchPosition(int matchPosition) { this.matchPosition = matchPosition; }

    public void setMatchID(long matchID) { this.matchID = matchID;
    }
}