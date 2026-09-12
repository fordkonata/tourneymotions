package backend.dbconnector;


import backend.Player;
import backend.Match;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayerQueries {
    private final Connection connection;

    public PlayerQueries(Connection connection) {
        this.connection = connection;
    }

    public record PlayerIDPair(String playerNickname, long playerID) {}

    public ArrayList<PlayerIDPair> findPlayerByName(String nickName) {
        ArrayList<PlayerIDPair> retrievedPlayers = new ArrayList<>();
        String select = "SELECT player_id, player_nickname FROM players WHERE player_nickname = ?";

        try (PreparedStatement findPlayerState = connection.prepareStatement(select)) {
            findPlayerState.setString(1, nickName);
            ResultSet res = findPlayerState.executeQuery();
            while (res.next()) {
                long id = res.getLong("player_id");
                retrievedPlayers.add(new PlayerIDPair(nickName, id));
            }
        }
        catch (SQLException e) { System.err.println("Failed to search for player(s): " + e.getMessage()); }
        return retrievedPlayers;
    }

    public PlayerIDPair retrievePlayerByID(long playerID) {
        String select = "SELECT player_id, player_nickname FROM players WHERE player_id = ?";
        try (PreparedStatement findPlayerState = connection.prepareStatement(select)) {
            findPlayerState.setLong(1, playerID);
            ResultSet res = findPlayerState.executeQuery();
            if (res.next()) return new PlayerIDPair(res.getString("player_nickname"), res.getLong("player_id"));
        }
        catch (SQLException e) {System.err.println("Failed to search for player(s): " + e.getMessage()); }
        return null;
    }

    public boolean playerExists(String nickName) {
        return !findPlayerByName(nickName).isEmpty();
    }

    public HashMap<String, Integer> retrievePlayerPoints(long playerID) {
        String selectPoints = "SELECT game_name, points FROM player_points_map WHERE player_id = ?";
        HashMap<String, Integer> pointsMap = new HashMap<>();
        try (PreparedStatement retrievePointsState = connection.prepareStatement(selectPoints)) {
            retrievePointsState.setLong(1, playerID);
            ResultSet res = retrievePointsState.executeQuery();
            while (res.next()) {
                String gameName = res.getString("game_name");
                Integer points = res.getInt("points");
                pointsMap.put(gameName, points);
            }
            return pointsMap;
        }
        catch (SQLException e) {System.err.println("Failed to find that points map: " + e.getMessage()); }
        return pointsMap;
    }

    public HashMap<String, Integer> retrievePlayerTier(long playerID) {
        String selectTiers = "SELECT game_name, tier FROM player_tier_map WHERE player_id = ?";
        HashMap<String, Integer> tierMap = new HashMap<>();
        try (PreparedStatement retrieveTiersState = connection.prepareStatement(selectTiers)) {
            retrieveTiersState.setLong(1, playerID);
            ResultSet res = retrieveTiersState.executeQuery();
            while (res.next()) {
                String gameName = res.getString("game_name");
                Integer tier = res.getInt("tier");
                tierMap.put(gameName, tier);
            }
            return tierMap;
        }
        catch (SQLException e) {System.err.println("Failed to find that points map: " + e.getMessage()); }
        return tierMap;
    }

    public HashMap<Long, HashMap<String, Integer>> retrievePlayerBracketPlacements(long playerID) {
        String selectPlacements =
                "SELECT tournament_id, game_name, placement FROM player_bracket_placements_map WHERE player_id = ?";

        HashMap<Long, HashMap<String, Integer>> bracketPlacementMap = new HashMap<>();

        try (PreparedStatement bState = connection.prepareStatement(selectPlacements)) {
            bState.setLong(1, playerID);
            ResultSet res = bState.executeQuery();

            while (res.next()) {
                long tournamentID = res.getLong("tournament_id");
                String gameName = res.getString("game_name");
                int placement = res.getInt("placement");
                bracketPlacementMap.computeIfAbsent(tournamentID, k -> new HashMap<>()).put(gameName, placement);
            }
        }
        catch (SQLException e) { System.err.println("Failed to retrieve bracket placements: " + e.getMessage()); }

        return bracketPlacementMap;
    }



    //Might need to change this and the table in a later build, before production.
    public void updateTotalMatchHistory(Player player, Match match) {
        String insertToTotalMatchHistory = "insert into player_total_match_history (player_id, match_id) VALUES (?,?)";

        try (PreparedStatement hState = connection.prepareStatement(insertToTotalMatchHistory);) {
            hState.setLong(1, player.getPlayerID());
            hState.setLong(2, match.getMatchID());
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public void updatePlayerPoints(Player player, String gameName) {
        String insertToPlayerPointsMap = "insert into player_points_map (player_id, game_name, points) VALUES (?,?)";
        try (PreparedStatement pState = connection.prepareStatement(insertToPlayerPointsMap)) {
            pState.setLong(1, player.getPlayerID());
            pState.setString(2, gameName);
            pState.setInt(3, player.getPoints(gameName));
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public void updatePlayerTier(Player player, String gameName) {
        String insertToPlayerTierMap = "insert into player_tier_map (player_id, game_name, tier) VALUES (?,?,?)";
        try (PreparedStatement pState = connection.prepareStatement(insertToPlayerTierMap)) {
            pState.setLong(1, player.getPlayerID());
            pState.setString(2, gameName);
            pState.setInt(3, player.getPlayerTier(gameName));
        }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateBracketPlacementsMap(Player player, Long tournamentID, String gameName) {
        String insertToBracketPlacementsMap = "insert into player_bracket_placements_map (player_id, tournamentID, " +
                "game_name, placement) VALUES (?,?,?,?) ";
        try (PreparedStatement pState = connection.prepareStatement(insertToBracketPlacementsMap)) {
            pState.setLong(1, player.getPlayerID());
            pState.setLong(2, tournamentID);
            pState.setString(3, gameName);
            pState.setInt(4, player.getPlayerPlacement(tournamentID, gameName));
        }
        catch (SQLException e) { e.printStackTrace(); }
    }


    public boolean insertOnePlayerToDB(Player player) {
        String insertToPlayers = "insert into players (player_nickname) VALUES (?) RETURNING player_id";
        String insertPlayerPointsMap = "insert into player_points_map (player_id, game_name, points) VALUES (?,?,?)" ;
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
        catch (SQLException e) {e.printStackTrace();
            return false;
        }
    }
}



//    public boolean insertPlayerListToDB(ArrayList<Player> playerList) {
//        String insertToPlayers = "insert into players (player_nickname) VALUES (?) RETURNING player_id";
//        String insertPlayerPointsMap = "insert into player_points_map (player_id, game_name, points) VALUES (?,?,?)";
//        String insertPlayerTierMap = "insert into player_tier_map (player_id, game_name, tier) VALUES (?,?,?)";
//
//        try (PreparedStatement pState = connection.prepareStatement(insertToPlayers);
//             PreparedStatement pPointsState = connection.prepareStatement(insertPlayerPointsMap);
//             PreparedStatement pTierState = connection.prepareStatement(insertPlayerTierMap)) {
//
//            for (Player player : playerList) {
//                pState.setString(1, player.getNickname());
//                ResultSet res = pState.executeQuery();
//
//                long newPlayerID;
//                if (res.next()) {
//                    newPlayerID = res.getLong("player_id");
//                    player.setPlayerID(newPlayerID);
//                }
//                else {
//                    System.err.println("Failed to retrieve generated player ID.");
//                    return false;
//                }
//
//                pPointsState.setLong(1, newPlayerID);
//                pPointsState.setString(2, "Street Fighter 6");
//                pPointsState.setInt(3, player.getPlayerPoints("Street Fighter 6"));
//
//                pTierState.setLong(1, newPlayerID);
//                pTierState.setString(2, "Street Fighter 6");
//                pTierState.setInt(3, player.getTier("Street Fighter 6"));
//
//                int affected2 = pPointsState.executeUpdate();
//                int affected3 = pTierState.executeUpdate();
//
//                System.out.println("Successfully inserted player with id " + newPlayerID);
//                System.out.println("Successfully inserted " + affected2 + " points.");
//                System.out.println("Successfully inserted " + affected3 + " tier.");
//            }
//            return true;
//        }
//        catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
