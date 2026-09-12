package backend;

import java.util.*;

import backend.dbconnector.*;


public class Tournament {

    public String tournamentName = "";
    public String tournament_type;
    private long tournamentID; //maybe make a global field for player_id, so I can cast rand to it?
    public int totalEntrants = 0;
    public ArrayList<Player> enteredPlayersList = new ArrayList<>();
    private TournamentQueries tournamentQueries;
    private NavigableMap<Integer, Player> tournamentEntrants = new TreeMap<>(); //NEED TO ALTER THIS TO SUPPORT MULTIPLE GAMES
    private HashMap<String, Bracket> tournamentBrackets = new HashMap<>();


    //tournament constructor that is only used from jdbc connector. Reconstructs tournament from DB when retrieveTournament is called
    public Tournament(String tournamentName, TournamentQueries tournamentQueries, long tournamentID, int totalEntrants){
        this.tournamentID = tournamentID;
        this.tournamentName = tournamentName;
        this.tournamentQueries = tournamentQueries;
        this.totalEntrants = totalEntrants;
    }

    public Tournament(String tournamentName, TournamentQueries tournamentQueries) {
        this.tournamentName = tournamentName;
        this.tournamentQueries = tournamentQueries;
        this.tournamentQueries.insertTournamentToDB(this);
    }



    // Creates a bracket for this tournament with its desired name and entrants.
    public void createBracket(String bracketName, ArrayList<Player> bracketEntrants, String gameName, BracketQueries bracketQueries,
                              PoolQueries poolQueries, MatchQueries matchQueries) {
        Bracket tBracket = new Bracket(this, bracketName, bracketEntrants, 16, 5, gameName,
                bracketQueries, poolQueries, matchQueries);
        tournamentBrackets.put(bracketName, tBracket);

        //ensures that we don't add players to the total count that are already in the tournament
        int newEntrantCount = (int) bracketEntrants.stream().filter(newEntrant -> !enteredPlayersList.contains(newEntrant)).count();
        enteredPlayersList.addAll(bracketEntrants);
        totalEntrants += newEntrantCount; // must change since players can enter more than one tournament
    }

    //public void startPools(String bracketName) {}

    public void autoCompleteBracket(String bracketName) { tournamentBrackets.get(bracketName); }

    public Long getTournamentID() { return tournamentID; }

    public String getTournamentName() { return tournamentName; }

    public HashMap<String, Bracket> getTournamentBrackets() { return tournamentBrackets; }

    public NavigableMap<Integer, Player> getTournamentEntrants() { return tournamentEntrants; }

    //must create a query function for this in pre production build
//    public void deleteTournament() {}

    public Bracket findBracket(String bracketName) {
        System.out.println("Number of brackets: " + tournamentBrackets.size());
        return this.tournamentBrackets.get(bracketName);
    }

    public void setTournamentID(long tournamentID) { this.tournamentID = tournamentID; }


    public void setTournamentEntrants(NavigableMap<Integer, Player> tournamentEntrants) { this.tournamentEntrants = tournamentEntrants; }
}