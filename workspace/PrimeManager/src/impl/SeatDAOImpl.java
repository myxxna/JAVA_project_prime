package impl;

import config.DBConnection;
import model.Seat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl { 
    
    public List<Seat> getAllSeats() {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM seats";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToSeat(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Seat getSeatById(int seatId) {
        String sql = "SELECT * FROM seats WHERE seat_index = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToSeat(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; 
    }
    
    public Seat getSeatBySeatNumber(String seatNumber) {
        String sql = "SELECT * FROM seats WHERE seat_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, seatNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSeat(rs);
                } else {
                    System.out.println("❌ DB 조회 실패: 버튼 값('" + seatNumber + "')과 일치하는 seat_number가 DB에 없습니다.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("🔥 DB 에러: " + e.getMessage());
        }
        return null;
    }

    public int getSeatIdBySeatNumber(String seatNumber) {
        String sql = "SELECT seat_index FROM seats WHERE seat_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, seatNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("seat_index");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Seat getSeatByUserId(int userId) {
        String sql = "SELECT * FROM seats WHERE current_user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToSeat(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; 
    }

    public boolean updateSeatStatus(Seat seat) {
        // [수정] current_use -> current_user_name 으로 변경
        String sql = "UPDATE seats SET current_user_name = ?, current_user_id = ?, status = ?, start_time = ?, end_time = ? WHERE seat_index = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, seat.getCurrentUserName()); 
            
            if (seat.getCurrentUserId() == 0) pstmt.setNull(2, java.sql.Types.INTEGER);
            else pstmt.setInt(2, seat.getCurrentUserId());
            
            pstmt.setString(3, seat.getStatus());
            
            if (seat.getStartTime() != null) pstmt.setTimestamp(4, Timestamp.valueOf(seat.getStartTime()));
            else pstmt.setNull(4, java.sql.Types.TIMESTAMP);

            if (seat.getEndTime() != null) pstmt.setTimestamp(5, Timestamp.valueOf(seat.getEndTime()));
            else pstmt.setNull(5, java.sql.Types.TIMESTAMP);
            
            pstmt.setInt(6, seat.getSeatIndex()); 

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    public boolean extendTime(int seatId, int addMinutes) {
        String sql = "UPDATE seats SET end_time = DATE_ADD(end_time, INTERVAL ? MINUTE) WHERE seat_index = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, addMinutes);
            pstmt.setInt(2, seatId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Seat mapResultSetToSeat(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        try { seat.setId(rs.getInt("id")); } catch (SQLException e) {} 
        seat.setSeatIndex(rs.getInt("seat_index"));
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setFloor(rs.getInt("floor"));
        
        String status = rs.getString("status");
        if (status != null) seat.setStatus(status.trim());
        else seat.setStatus("A"); 
        
        int userId = rs.getInt("current_user_id");
        if (rs.wasNull()) seat.setCurrentUserId(0);
        else seat.setCurrentUserId(userId);
        
        // [수정] current_use -> current_user_name 으로 변경
        // 만약 DB 컬럼명이 user_name 이라면 "user_name"으로 바꾸셔야 합니다.
        try {
            seat.setCurrentUserName(rs.getString("current_user_name")); 
        } catch (SQLException e) {
            // 혹시 몰라 예외처리: 컬럼명이 다를 경우 null 처리
            System.out.println("⚠️ 컬럼 이름 확인 필요: current_user_name이 아닐 수 있음");
        }
        
        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) seat.setStartTime(startTs.toLocalDateTime());
        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) seat.setEndTime(endTs.toLocalDateTime());
        
        return seat;
    }
}