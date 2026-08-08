package seeder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Random;


public class Tournament {

    public String tournamentName = "";
    public String tournamentLocation = "";
    public String tournament_type;
    public int totalEntrants = 0;
    private HashMap<String, NavigableMap<Integer, Player>> tournamentEntrants = new HashMap<>(); //NEED TO ALTER THIS TO SUPPORT MULTIPLE GAMES
    private HashMap<String, Bracket> tournamentBrackets = new HashMap<>();
    private int tournamentID; //maybe make a global field for player_id, so I can cast rand to it?
    public static HashMap<Integer, Tournament> allTournaments = new HashMap<>();


    private void generateTournamentId() {
        Integer id;
        Random random = new Random();
        //random number generator for matchID for a tournament
        do id = random.nextInt(999999999);
        while (allTournaments.containsKey(id));
        this.tournamentID = id;
        allTournaments.put(tournamentID, this);

    }

    public Tournament(String tournamentName, String tournamentLocation) {
        this.tournamentName = tournamentName;
        this.tournamentLocation = tournamentLocation;
    }


    // Creates a bracket for this tournament with its desired name and entrants.
    public void createBracket(String bracketName, ArrayList<Player> bracketEntrants, String gameName) {
        Bracket tBracket = new Bracket(this, bracketName, bracketEntrants, bracketEntrants.size(), gameName);
        tournamentBrackets.put(bracketName, tBracket);
        totalEntrants += bracketEntrants.size();
    }

    //public void startPools(String bracketName) {}

    public void autoCompleteBracket(String bracketName) { tournamentBrackets.get(bracketName); }

    public Integer getTournamentID() { return tournamentID; }

    public void deleteTournament() {}

    public Bracket findBracket(String bracketName) {
        return this.tournamentBrackets.get(bracketName);
    }
}