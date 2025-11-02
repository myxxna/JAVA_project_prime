package impl;

import config.DBConnection;
import interfaces.ISeatDAO;
import model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl implements ISeatDAO {

    @Override
    public List<Seat> getSeatsByRoom(String roomNumber) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE room_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                seats.add(new Seat(
                        0,//rs.getInt("id"),
                        rs.getString("room_number"),
                        rs.getString("seat_number"),
                        true//rs.getBoolean("reserved")
                        
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    @Override
    public boolean reserveSeat(int seatId) {
        String sql = "UPDATE seats SET reserved = 1 WHERE id = ? AND reserved = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
