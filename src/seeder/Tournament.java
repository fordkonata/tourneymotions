package seeder;

import java.util.ArrayList;
import java.util.HashMap;


public class Tournament {

    public String tournamentName = "";
    public String tournamentLocation = "";
    public String tournament_type;
    public int totalEntrants = 0;
    private HashMap<String, ArrayList<Player>> tournamentEntrants = new HashMap<>(); //NEED TO ALTER THIS TO SUPPORT MULTIPLE GAMES
    private HashMap<String, Bracket> tournamentBrackets = new HashMap<>();
    private int tournament_id; //maybe make a global field for player_id, so I can cast rand to it?


    public Tournament(String tournamentName, String tournamentLocation) {
        this.tournamentName = tournamentName;
        this.tournamentLocation = tournamentLocation;
    }


    // Creates a bracket for this tournament with its desired name and entrants.
    public void createBracket(String bracketName, ArrayList<Player> bracketEntrants, String gameName) {
        Bracket tBracket = new Bracket(bracketName, bracketEntrants, bracketEntrants.size(), gameName);
        tournamentBrackets.put(bracketName, tBracket);
        totalEntrants += bracketEntrants.size();
    }

    //public void startPools(String bracketName) {}

    public void autoCompleteBracket(String bracketName) { tournamentBrackets.get(bracketName); }

    public Bracket findBracket(String bracketName) {
        return this.tournamentBrackets.get(bracketName);
    }
}