package backend.dbconnector;

import backend.Tournament;

import java.sql.*;

public class TournamentQueries {
    private final Connection connection;

    public TournamentQueries(Connection connection) {
        this.connection = connection;
    }

    public boolean checkIfTournamentInDB(long tournamentID) {
        String select = "SELECT EXISTS(SELECT 1 FROM tournaments WHERE tournament_id = ?)";
        try (PreparedStatement state = connection.prepareStatement(select)) {
            state.setLong(1, tournamentID);
            ResultSet res = state.executeQuery();
            if (res.next()) { return res.getBoolean(1); }
            return false;
        }
        catch (SQLException e) { return false; }
    }

    public boolean insertTournamentToDB(Tournament tournament)  {
        try {
            String tString = "insert into tournaments (tournament_name) VALUES (?)";
            PreparedStatement tourneyState = connection.prepareStatement(tString);
            tourneyState.setString(1, tournament.getTournamentName());
            ResultSet res = tourneyState.executeQuery();
            if (res.next()) tournament.setTournamentID(res.getInt("tournament_id"));
            tourneyState.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            System.err.println("That tournament already in the database.");
            return false;
        }
    }

}