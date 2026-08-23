package backend.dbconnector;

import backend.Player;

import java.sql.*;
import java.util.ArrayList;

public class PlayerQueries {
    private final Connection connection;

    public PlayerQueries(Connection connection) {
        this.connection = connection;
    }

    public boolean checkIfPlayerInDB(long playerID) {
        String select = "SELECT EXISTS(SELECT 1 FROM players WHERE playerID = ?)";
        try (PreparedStatement state = connection.prepareStatement(select)) {
            state.setLong(1, playerID);
            ResultSet res = state.executeQuery();
            if (res.next()) { return res.getBoolean(1); }
            return false;
        }
        catch (SQLException e) { return false; }
    }

    public boolean insertOnePlayerToDB(Player player) {
        String insertToPlayers = "insert into players (player_nickname) VALUES (?) RETURNING player_id";
        String insertPlayerPointsMap = "insert into player_points_map (player_id, game_name, points) VALUES (?,?,?)";
        String insertPlayerTierMap = "insert into player_tier_map (player_id, game_name, tier) VALUES (?,?,?)";

        try (PreparedStatement pState = connection.prepareStatement(insertToPlayers);
             PreparedStatement pPointsState = connection.prepareStatement(insertPlayerPointsMap);
             PreparedStatement pTierState = connection.prepareStatement(insertPlayerTierMap)) {

            //insert the player ID first, to get create the id for later queries
            pState.setString(1, player.getNickname());
            ResultSet res = pState.executeQuery();

            long newPlayerID;
            if (res.next()) {
                newPlayerID = res.getLong("player_id");
                player.setPlayerID(newPlayerID);
            }
            else {
                System.err.println("Failed to retrieve generated player ID.");
                return false;
            }

            pPointsState.setLong(1, newPlayerID);
            pPointsState.setString(2, "Street Fighter 6");
            pPointsState.setInt(3, player.getPlayerPoints("Street Fighter 6"));

            pTierState.setLong(1, newPlayerID);
            pTierState.setString(2, "Street Fighter 6");
            pTierState.setInt(3, player.getTier("Street Fighter 6"));

            int affected2 = pPointsState.executeUpdate();
            int affected3 = pTierState.executeUpdate();

            System.out.println("Successfully inserted player with id " + newPlayerID);
            System.out.println("Successfully inserted " + affected2 + " points.");
            System.out.println("Successfully inserted " + affected3 + " tier.");

            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean insertPlayerListToDB(ArrayList<Player> playerList) {
        String insertToPlayers = "insert into players (player_nickname) VALUES (?) RETURNING player_id";
        String insertPlayerPointsMap = "insert into player_points_map (player_id, game_name, points) VALUES (?,?,?)";
        String insertPlayerTierMap = "insert into player_tier_map (player_id, game_name, tier) VALUES (?,?,?)";

        try (PreparedStatement pState = connection.prepareStatement(insertToPlayers);
             PreparedStatement pPointsState = connection.prepareStatement(insertPlayerPointsMap);
             PreparedStatement pTierState = connection.prepareStatement(insertPlayerTierMap)) {

            for (Player player : playerList) {
                pState.setString(1, player.getNickname());
                ResultSet res = pState.executeQuery();

                long newPlayerID;
                if (res.next()) {
                    newPlayerID = res.getLong("player_id");
                    player.setPlayerID(newPlayerID);
                }
                else {
                    System.err.println("Failed to retrieve generated player ID.");
                    return false;
                }

                pPointsState.setLong(1, newPlayerID);
                pPointsState.setString(2, "Street Fighter 6");
                pPointsState.setInt(3, player.getPlayerPoints("Street Fighter 6"));

                pTierState.setLong(1, newPlayerID);
                pTierState.setString(2, "Street Fighter 6");
                pTierState.setInt(3, player.getTier("Street Fighter 6"));

                int affected2 = pPointsState.executeUpdate();
                int affected3 = pTierState.executeUpdate();

                System.out.println("Successfully inserted player with id " + newPlayerID);
                System.out.println("Successfully inserted " + affected2 + " points.");
                System.out.println("Successfully inserted " + affected3 + " tier.");
            }
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
