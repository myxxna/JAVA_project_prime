package impl;

import model.Reservation;
import config.DBConnection;
import java.sql.*;

public class ReservationDAOImpl {

    public boolean insertReservation(Reservation res) {
        // ★ 주의: 현재 DB 테이블에 'duration_minutes' 컬럼이 없으므로 SQL에서 제외했습니다.
        // 만약 이용 시간을 DB에 꼭 기록해야 한다면, MySQL 워크벤치에서 컬럼을 추가하고 아래 주석을 푸세요.
        
        String sql = "INSERT INTO reservations (user_id, seat_id, reservation_time, status, created_at) " +
                     "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, res.getUserId());
            pstmt.setInt(2, res.getSeatId());
            pstmt.setTimestamp(3, Timestamp.valueOf(res.getReservationTime()));
            // pstmt.setInt(4, res.getDurationMinutes()); // DB 컬럼 없음 -> 제거
            pstmt.setString(4, res.getStatus());       // 인덱스 5 -> 4로 변경

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}