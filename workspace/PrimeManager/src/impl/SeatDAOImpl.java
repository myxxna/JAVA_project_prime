package impl;

import config.DBConnection;
import model.Seat;
import interfaces.ISeatDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl implements ISeatDAO {

    @Override
    public List<Seat> getAllSeats() {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT seatId, roomNumber, seatNumber, isAvailable FROM Seats";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                seats.add(new Seat(
                        rs.getInt("seatId"),
                        rs.getString("roomNumber"),
                        rs.getString("seatNumber"),
                        rs.getBoolean("isAvailable")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seats;
    }

    @Override
    public Seat getSeatById(int seatId) {
        String query = "SELECT seatId, roomNumber, seatNumber, isAvailable FROM Seats WHERE seatId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, seatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Seat(
                            rs.getInt("seatId"),
                            rs.getString("roomNumber"),
                            rs.getString("seatNumber"),
                            rs.getBoolean("isAvailable")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateSeatAvailability(int seatId, boolean available) {
        String query = "UPDATE Seats SET isAvailable = ? WHERE seatId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setBoolean(1, available);
            pstmt.setInt(2, seatId);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
