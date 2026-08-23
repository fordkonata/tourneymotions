package backend.dbconnector;

import backend.Bracket;

import java.sql.*;

public class BracketQueries {
    private final Connection connection;

    public BracketQueries(Connection connection) {
        this.connection = connection;
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
