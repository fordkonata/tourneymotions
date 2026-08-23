package backend.dbconnector;

import java.sql.*;

public class DbTest {
    public static void main(String[] args) {
        String insertSql = "INSERT INTO players (playerID, player_nickname) VALUES (?, ?) RETURNING playerID";

        try (Connection conn = DbConnection.get();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setInt(1, 32719);
            ps.setString(2, "TrueJit");
//            ps.setDouble(3, 1200.0);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Inserted player with id: " + rs.getInt("playerID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}