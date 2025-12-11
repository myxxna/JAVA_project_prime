package impl;

import model.Seat;
import config.DBConnection; // 👈 여기 변경됨 (DBUtil -> DBConnection)

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl {

    // 1. 모든 좌석 조회
    public List<Seat> getAllSeats() {
        List<Seat> seatList = new ArrayList<>();
        String sql = "SELECT * FROM seats ORDER BY floor, seat_number";

        // 👈 DBConnection.getConnection()으로 변경됨
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                seatList.add(mapRowToSeat(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seatList;
    }

    // 2. ID로 좌석 조회
    public Seat getSeatById(int id) {
        String sql = "SELECT * FROM seats WHERE id = ?";
        Seat seat = null;

        try (Connection conn = DBConnection.getConnection(); // 👈 변경됨
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    seat = mapRowToSeat(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seat;
    }

    // 3. 사용자 ID로 좌석 조회
    public Seat getSeatByUserId(int userId) {
        String sql = "SELECT * FROM seats WHERE current_user_id = ?";
        Seat seat = null;

        try (Connection conn = DBConnection.getConnection(); // 👈 변경됨
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    seat = mapRowToSeat(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seat;
    }

    // 4. 좌석 상태 업데이트
    public boolean updateSeatStatus(Seat seat) {
        String sql = "UPDATE seats SET " +
                     "current_user_id = ?, " +
                     "current_user_name = ?, " +
                     "status = ?, " +
                     "start_time = ?, " +
                     "end_time = ? " +
                     "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection(); // 👈 변경됨
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (seat.getCurrentUserId() == null) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, seat.getCurrentUserId());
            }

            pstmt.setString(2, seat.getCurrentUserName());
            pstmt.setString(3, seat.getStatus());

            if (seat.getStartTime() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(seat.getStartTime()));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }

            if (seat.getEndTime() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(seat.getEndTime()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }

            pstmt.setInt(6, seat.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // [헬퍼 메서드] ResultSet -> Seat 변환
    private Seat mapRowToSeat(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setId(rs.getInt("id"));
        seat.setFloor(rs.getInt("floor"));
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setSeatIndex(rs.getInt("seat_index"));
        seat.setRoomNumber(rs.getString("room_index"));
        seat.setStatus(rs.getString("status"));
        
        int userId = rs.getInt("current_user_id");
        if (rs.wasNull()) {
            seat.setCurrentUserId(null);
        } else {
            seat.setCurrentUserId(userId);
        }
        
        seat.setCurrentUserName(rs.getString("current_user_name"));

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) seat.setStartTime(startTs.toLocalDateTime());

        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) seat.setEndTime(endTs.toLocalDateTime());

        return seat;
    }
}