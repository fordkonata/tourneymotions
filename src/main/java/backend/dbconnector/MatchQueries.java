package backend.dbconnector;

import backend.Match;

import java.sql.*;

public class MatchQueries {
    private final Connection connection;

    public MatchQueries(Connection connection) {
        this.connection = connection;
    }

    public boolean checkIfMatchInDB(long matchID) {
        String select = "SELECT EXISTS(SELECT 1 FROM matches WHERE match_id = ?)";
        try (PreparedStatement state = connection.prepareStatement(select)) {
            state.setLong(1, matchID);
            ResultSet res = state.executeQuery();
            if (res.next()) { return res.getBoolean(1); }
            return false;
        }
        catch (SQLException e) { return false; }
    }

    public boolean setMatchPlayer(long matchID, int playerSlot, long playerID) {
        String column = (playerSlot == 1) ? "p1" : "p2";
        String update = "update matches set " + column + " = ? where match_id = ?";

        try (PreparedStatement matchState = connection.prepareStatement(update)) {
            matchState.setLong(1, playerID);
            matchState.setLong(2, matchID);

            int rowsAffected = matchState.executeUpdate();
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            System.err.println("Failed to set match player: " + e.getMessage());
            return false;
        }
    }

    public boolean setMatchWinnerInDB(long matchID, long winnerID) {
        String sql = "update matches set winner = ? where match_id = ?";

        try (PreparedStatement matchState = connection.prepareStatement(sql)) {
            matchState.setLong(1, winnerID);
            matchState.setLong(2, matchID);

            int rowsAffected = matchState.executeUpdate();
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            System.err.println("Failed to set match winner: " + e.getMessage());
            return false;
        }
    }

    public boolean setMatchLoserInDB(long matchID, long loserID) {
        String sql = "update matches set loser = ? where match_id = ?";

        try (PreparedStatement matchState = connection.prepareStatement(sql)) {
            matchState.setLong(1, loserID);
            matchState.setLong(2, matchID);

            int rowsAffected = matchState.executeUpdate();
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            System.err.println("Failed to set match loser: " + e.getMessage());
            return false;
        }
    }


    public boolean insertMatch(long poolID, Match match) {
        String sql = "insert into matches (pool_id, match_position, match_side, p1, p2, winner, loser) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING match_id";

        try (PreparedStatement matchState = connection.prepareStatement(sql)) {
            matchState.setLong(1, poolID);
            matchState.setInt(2, match.getMatchPosition());
            matchState.setString(3, match.getMatchSide());

            if (match.getP1() != null) matchState.setLong(4, match.getP1().getPlayerID());
            else matchState.setNull(4, Types.BIGINT);

            if (match.getP2() != null) matchState.setLong(5, match.getP2().getPlayerID());
            else matchState.setNull(5, Types.BIGINT);

            if (match.getWinner() != null) matchState.setLong(6, match.getWinner().getPlayerID());
            else matchState.setNull(6, Types.BIGINT);

            if (match.getLoser() != null) matchState.setLong(7, match.getLoser().getPlayerID());
            else matchState.setNull(7, Types.BIGINT);

            ResultSet res = matchState.executeQuery();
            if (res.next()) match.setMatchID(res.getLong("match_id"));
            return true;
        }
        catch (SQLException e) {
            System.err.println("Failed to insert match: " + e.getMessage());
            return false;
        }
    }
}
