package impl;

import config.DBConnection;
import interfaces.ISeatDAO;
import model.Seat;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl implements ISeatDAO {

    @Override
    public List<Seat> getAllSeats() {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                seats.add(new Seat(
                        rs.getInt("id"),
                        rs.getString("room_number"),
                        rs.getString("seat_number"),
                        rs.getBoolean("reserved"),
                        rs.getString("status"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null,
                        rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    @Override
    public Seat getSeatById(int id) {
        String sql = "SELECT * FROM seats WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Seat(
                            rs.getInt("id"),
                            rs.getString("room_number"),
                            rs.getString("seat_number"),
                            rs.getBoolean("reserved"),
                            rs.getString("status"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null,
                            rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateSeat(Seat seat) {
        String sql = "UPDATE seats SET reserved=?, status=?, user_id=?, start_time=?, end_time=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, seat.isReserved());
            pstmt.setString(2, seat.getStatus());
            pstmt.setInt(3, seat.getCurrentUserId());
            pstmt.setTimestamp(4, seat.getStartTime() != null ? Timestamp.valueOf(seat.getStartTime()) : null);
            pstmt.setTimestamp(5, seat.getEndTime() != null ? Timestamp.valueOf(seat.getEndTime()) : null);
            pstmt.setInt(6, seat.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

