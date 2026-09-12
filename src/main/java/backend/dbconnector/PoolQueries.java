package backend.dbconnector;

import backend.Bracket;
import backend.Pool;

import java.sql.*;

public class PoolQueries {
    private final Connection connection;
    private final BracketQueries bracketQueries;
    private final PlayerQueries playerQueries;

    public PoolQueries(Connection connection, BracketQueries bracketQueries, PlayerQueries playerQueries) {
        this.connection = connection;
        this.playerQueries = playerQueries;
        this.bracketQueries = bracketQueries;
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

    //Needs to be updated to retrieve matches as well from the DB
    public Pool retrievePool(long poolID) {
        String selectPool = "SELECT pool_id, bracket_id, stage_number, pool_name, pool_position FROM pools WHERE pool_id = ?";

        try (PreparedStatement poolState = connection.prepareStatement(selectPool)) {
            poolState.setLong(1, poolID);
            ResultSet res = poolState.executeQuery();

            if (res.next()) {
                Bracket parentBracket = bracketQueries.retrieveBracket(res.getLong("bracket_id"), this);
                return new Pool(res.getLong("pool_id"), res.getString("pool_name"), parentBracket, res.getInt("stage_number"), this);
            }
            else {
                System.err.println("No pool found with id " + poolID);
                return null;
            }
        }
        catch (SQLException e) {
            System.err.println("Failed to retrieve pool: " + e.getMessage());
            return null;
        }
    }

    public boolean insertPoolToDB(long bracketID, Pool pool) {
        String pString = "insert into pools (bracket_id, stage_number, pool_name, pool_position) VALUES (?,?,?,?) RETURNING pool_id";
        try (PreparedStatement poolState = connection.prepareStatement(pString)) {
            poolState.setLong(1, bracketID);
            poolState.setInt(2, pool.getStageNumber());
            poolState.setString(3, pool.getPoolName());
            poolState.setInt(4, pool.getPoolPosition());

            ResultSet res = poolState.executeQuery();
            if (res.next()) {
                pool.setPoolID(res.getLong("pool_id"));
            }
            return true;
        }
        catch (SQLException e) {
            System.err.println("Failed to insert pool: " + e.getMessage());
            return false;
        }
    }
}
