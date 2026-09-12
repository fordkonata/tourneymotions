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
            if (res.next()) {
                return res.getBoolean(1);
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public Tournament retrieveTournament(long tournamentID) {
        if (!checkIfTournamentInDB(tournamentID)) {
            System.err.println("No tournament found with id " + tournamentID);
            return null;
        }

        String selectTournament = "SELECT tournament_id, tournament_name, total_entrants FROM tournaments WHERE tournament_id = ?";

        try (PreparedStatement tournamentState = connection.prepareStatement(selectTournament)) {
            tournamentState.setLong(1, tournamentID);
            ResultSet res = tournamentState.executeQuery();

            if (res.next()) {
                return new Tournament(res.getString("tournament_name"), this, res.getLong("tournament_id"),
                        res.getInt("total_entrants"));
            }
            return null;
        }
        catch (SQLException e) {
            System.err.println("Failed to retrieve tournament: " + e.getMessage());
            return null;
        }
    }



    public boolean insertTournamentToDB(Tournament tournament) {
        String tString = "insert into tournaments (tournament_name) VALUES (?) RETURNING tournament_id";
        try (PreparedStatement tourneyState = connection.prepareStatement(tString)) {
            tourneyState.setString(1, tournament.getTournamentName());
            ResultSet res = tourneyState.executeQuery();
            if (res.next()) {
                tournament.setTournamentID(res.getLong("tournament_id"));
            }
            return true;
        }
        catch (SQLException e) {
            System.err.println("Failed to insert tournament: " + e.getMessage());
            return false;
        }
    }

}