package backend.dbconnector;

import backend.Pool;

import java.sql.*;

public class PoolQueries {
    private final Connection connection;

    public PoolQueries(Connection connection) {
        this.connection = connection;
    }


    //Returns true if the poolID exists, and false if it does not.
    public boolean checkIfIDInDB(long poolID) {
        String select = "SELECT EXISTS(SELECT 1 FROM pools WHERE pool_id = ?)";
        try (PreparedStatement state = connection.prepareStatement(select)) {
            state.setLong(1, poolID);
            ResultSet res = state.executeQuery();
            if (res.next()) { return res.getBoolean(1); }
            return false;
        }
        catch (SQLException e) { return false; }
    }


    public boolean insertPoolToDB(long tournamentID, long bracketID, Pool pool)  {
        try {
            String pString = "insert into brackets (tournament_id, bracket_id, pool_id,) VALUES (?,?,?)";
            PreparedStatement poolState = connection.prepareStatement(pString);
            poolState.setLong(1, tournamentID);
            poolState.setLong(2, bracketID);
            poolState.setLong(3, pool.getPoolID());
            poolState.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            System.err.println("That pool is already in the database.");
            return false;
        }
    }
}
