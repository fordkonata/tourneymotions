package backend.dbconnector;

import backend.Bracket;
import backend.Pool;
import backend.Tournament;

import java.sql.*;
import java.util.NavigableMap;
import java.util.TreeMap;

public class BracketQueries {
    private final Connection connection;
    private final TournamentQueries tournamentQueries;
    private final PlayerQueries playerQueries;

    public BracketQueries(Connection connection, TournamentQueries tournamentQueries, PlayerQueries playerQueries) {
        this.connection = connection;
        this.tournamentQueries = tournamentQueries;
        this.playerQueries = playerQueries;
    }

    public boolean checkIfBracketInDB(long bracketID) {
        String select = "SELECT EXISTS(SELECT 1 FROM brackets WHERE bracket_id = ?)";
        try (PreparedStatement state = connection.prepareStatement(select)) {
            state.setLong(1, bracketID);
            ResultSet res = state.executeQuery();
            if (res.next()) { return res.getBoolean(1); }
            return false;
        }
        catch (SQLException e) { return false; }
    }

    public String getOrCreateGame(String gameName) {
        String select = "SELECT game_name, game_id FROM games WHERE game_name = ?";

        try (PreparedStatement selectState = connection.prepareStatement(select)) {
            selectState.setString(1, gameName);
            ResultSet res = selectState.executeQuery();

            if (res.next()) {
                System.out.println("found");
                return res.getString("game_name"); // already exists, nothing to insert
            }
        }
        catch (SQLException e) {
            System.err.println("Failed to check game: " + e.getMessage());
            return null;
        }
        // doesn't exist yet, insert fresh
        String insert = "INSERT INTO games (game_name) VALUES (?)";
        try (PreparedStatement insertState = connection.prepareStatement(insert)) {
            insertState.setString(1, gameName);
            insertState.executeUpdate();
            System.out.println("inserted");
            return gameName;
        }
        catch (SQLException e) {
            System.err.println("Failed to insert game: " + e.getMessage());
            return null;
        }
    }

    public Bracket retrieveBracket(long bracketID, PoolQueries poolQueries) {
        String selectBracket = "SELECT bracket_id, tournament_id, game_name, bracket_tier FROM brackets WHERE bracket_id = ?";

        try (PreparedStatement bState = connection.prepareStatement(selectBracket)) {
            bState.setLong(1, bracketID);
            ResultSet res = bState.executeQuery();

            if (res.next()) {
                Tournament parentTournament = tournamentQueries.retrieveTournament(res.getLong("tournament_id"));
                return new Bracket(res.getLong("bracket_id"), parentTournament, res.getString("game_name"), res.getInt("bracket_tier"),
                        this, poolQueries);
            }
            else {
                System.err.println("No bracket found with id " + bracketID);
                return null;
            }
        }
        catch (SQLException e) {
            System.err.println("Failed to retrieve bracket: " + e.getMessage());
            return null;
        }
    }

    public NavigableMap<Integer, NavigableMap<Integer, Pool>> retrieveBracketStages(long bracketID, PoolQueries poolQueries) {
        String select = "SELECT pool_id, stage_number, pool_position FROM pools WHERE bracket_id = ? ORDER BY stage_number, pool_position";
        NavigableMap<Integer, NavigableMap<Integer, Pool>> bracketStagesMap = new TreeMap<>();

        try (PreparedStatement state = connection.prepareStatement(select)) {
            state.setLong(1, bracketID);
            ResultSet res = state.executeQuery();

            while (res.next()) {
                int stageNumber = res.getInt("stage_number");
                int poolPosition = res.getInt("pool_position");
                long poolID = res.getLong("pool_id");

                Pool pool = poolQueries.retrievePool(poolID); // reuse existing method rather than duplicating logic

                bracketStagesMap.computeIfAbsent(stageNumber, k -> new TreeMap<>()).put(poolPosition, pool);
            }
        }
        catch (SQLException e) {
            System.err.println("Failed to retrieve bracket stages: " + e.getMessage());
        }

        return bracketStagesMap;
    }


    public boolean insertBracketToDB(long tournamentID, Bracket bracket) {

        String bString = "insert into brackets (tournament_id, game_name, bracket_tier) VALUES (?,?,?) RETURNING bracket_id";
        try (PreparedStatement bracketState = connection.prepareStatement(bString)) {
            bracketState.setLong(1, tournamentID);
            bracketState.setString(2, bracket.getGameName());
            bracketState.setInt(3, bracket.getBracketTier());
            ResultSet res = bracketState.executeQuery();
            if (res.next()) bracket.setBracketID(res.getLong("bracket_id"));

            return true;
        }
        catch (SQLException e) {
            System.err.println("Failed to insert bracket: " + e.getMessage());
            return false;
        }
    }
}
