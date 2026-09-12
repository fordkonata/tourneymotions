package backend.dbconnector;

import backend.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MatchQueries {
    private final Connection connection;
    private final PlayerQueries playerQueries;

    public MatchQueries(Connection connection, PlayerQueries playerQueries) {
        this.connection = connection;
        this.playerQueries = playerQueries;
    }

    public boolean findMatchByID(long matchID) {
        String select = "SELECT EXISTS(SELECT 1 FROM matches WHERE match_id = ?)";
        try (PreparedStatement state = connection.prepareStatement(select)) {
            state.setLong(1, matchID);
            ResultSet res = state.executeQuery();
            if (res.next()) { return res.getBoolean(1); }
            return false;
        }
        catch (SQLException e) { return false; }
    }

    public boolean matchExists(long matchID) {
        return findMatchByID(matchID);
    }

    //Need to edit this retrieve function later to access full match data, also full Pool data, and full bracket data for java reconstruction
    public Match retrieveMatch(long matchID) {
        String selectMatch = "SELECT match_id, pool_id, tournament_id, match_position, match_side, p1, p2, winner, loser " +
                "FROM matches WHERE match_id = ?";

        try (PreparedStatement mState = connection.prepareStatement(selectMatch)) {
            mState.setLong(1, matchID);
            ResultSet res = mState.executeQuery();

            if (res.next()) {
                Match match = new Match(res.getLong("match_id"), res.getInt("match_position"), res.getString("match_side"),
                        this);

//                match.setTournamentID(res.getLong("tournament_id"));

                long player1ID =  playerQueries.retrievePlayerByID(res.getLong("p1")).playerID();
                String player1Nickname = playerQueries.retrievePlayerByID(res.getLong("p1")).playerNickname();
                long player2ID = playerQueries.retrievePlayerByID(res.getLong("p2")).playerID();
                String player2Nickname = playerQueries.retrievePlayerByID(res.getLong("p2")).playerNickname();

                long winnerID =  playerQueries.retrievePlayerByID(res.getLong("winner")).playerID();
                String winnerNickname = playerQueries.retrievePlayerByID(res.getLong("winner")).playerNickname();
                long loserID =  playerQueries.retrievePlayerByID(res.getLong("loser")).playerID();
                String loserNickname = playerQueries.retrievePlayerByID(res.getLong("loser")).playerNickname();

                match.setPlayer1FromDB(player1ID);
                match.setPlayer2FromDB(player2ID);
                match.setWinnerFromDB(winnerID);
                match.setLoserFromDB(loserID);
                return match;
            }
            else {
                System.err.println("No match found with id " + matchID);
                return null;
            }
        }
        catch (SQLException e) {
            System.err.println("Failed to retrieve match: " + e.getMessage());
            return null;
        }
    }

    public HashMap<Long, HashMap<String, ArrayList<Match>>> retrievePlayerMatchHistory(long playerID) {
        String selectHistory =
                "SELECT m.match_id, m.match_position, m.match_side, m.p1, m.p2, m.winner, m.loser, " +
                        "b.tournament_id, b.game_name " +
                        "FROM player_total_match_history ptmh " +
                        "JOIN matches m ON ptmh.match_id = m.match_id " +
                        "JOIN pools p ON m.pool_id = p.pool_id " +
                        "JOIN brackets b ON p.bracket_id = b.bracket_id " +
                        "WHERE ptmh.player_id = ?";

        HashMap<Long, HashMap<String, ArrayList<Match>>> playerTotalMatchHistory = new HashMap<>();

        try (PreparedStatement state = connection.prepareStatement(selectHistory)) {
            state.setLong(1, playerID);
            ResultSet res = state.executeQuery();
            while (res.next()) {
                Match match = retrieveMatch(res.getLong("m.match_id"));

                long tournamentID = res.getLong("tournament_id");
                String gameName = res.getString("game_name");

                playerTotalMatchHistory
                        .computeIfAbsent(tournamentID, k -> new HashMap<>())
                        .computeIfAbsent(gameName, k -> new ArrayList<>())
                        .add(match);
            }
        }
        catch (SQLException e) { System.err.println("Failed to retrieve match history: " + e.getMessage()); }

        return playerTotalMatchHistory;
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
        String sql = "insert into matches (pool_id, tournament_id, match_position, match_side, p1, p2, winner, loser) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING match_id";

        try (PreparedStatement matchState = connection.prepareStatement(sql)) {
            matchState.setLong(1, poolID);
            matchState.setLong(2, match.getParentPool().getParentBracket().getParentTournament().getTournamentID());
            matchState.setInt(3, match.getMatchPosition());
            matchState.setString(4, match.getMatchSide());


            if (match.getP1() != null) matchState.setLong(5, match.getP1().getPlayerID());
            else matchState.setNull(5, Types.BIGINT);

            if (match.getP2() != null) matchState.setLong(6, match.getP2().getPlayerID());
            else matchState.setNull(6, Types.BIGINT);

            if (match.getWinner() != null) matchState.setLong(7, match.getWinner().getPlayerID());
            else matchState.setNull(7, Types.BIGINT);

            if (match.getLoser() != null) matchState.setLong(8, match.getLoser().getPlayerID());
            else matchState.setNull(8, Types.BIGINT);

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
