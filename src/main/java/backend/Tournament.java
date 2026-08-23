package backend;

import java.util.*;

import backend.dbconnector.*;


public class Tournament {

    public String tournamentName = "";
    public String tournament_type;
    private long tournamentID; //maybe make a global field for player_id, so I can cast rand to it?
    public int totalEntrants = 0;
    private TournamentQueries tournamentQueries;
    private NavigableMap<Integer, Player> tournamentEntrants = new TreeMap<>(); //NEED TO ALTER THIS TO SUPPORT MULTIPLE GAMES
    private HashMap<String, Bracket> tournamentBrackets = new HashMap<>();
//    public static HashMap<Integer, Tournament> allTournaments = new HashMap<>();



    public Tournament(String tournamentName, TournamentQueries tournamentQueries) {
        this.tournamentName = tournamentName;
//        this.tournamentLocation = tournamentLocation;
        this.tournamentQueries = tournamentQueries;
    }


    // Creates a bracket for this tournament with its desired name and entrants.
    public void createBracket(String bracketName, ArrayList<Player> bracketEntrants, String gameName, BracketQueries bracketQueries) {
        Bracket tBracket = new Bracket(this, bracketName, bracketEntrants, bracketEntrants.size(), gameName, bracketQueries);
        tournamentBrackets.put(bracketName, tBracket);
        totalEntrants += bracketEntrants.size();
    }

    //public void startPools(String bracketName) {}

    public void autoCompleteBracket(String bracketName) { tournamentBrackets.get(bracketName); }

    public Long getTournamentID() { return tournamentID; }

    public String getTournamentName() { return tournamentName; }

    public HashMap<String, Bracket> getTournamentBrackets() { return tournamentBrackets; }

    public void deleteTournament() {}

    public Bracket findBracket(String bracketName) {
        return this.tournamentBrackets.get(bracketName);
    }

    public void setTournamentID(int tournamentID) { this.tournamentID = tournamentID; }
}