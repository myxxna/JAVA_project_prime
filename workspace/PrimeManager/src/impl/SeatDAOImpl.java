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
                // (★수정★) 생성자가 아닌 Setter를 사용
                Seat seat = new Seat();
                seat.setId(rs.getInt("id"));
                seat.setFloor(rs.getInt("floor"));
                seat.setRoomNumber(rs.getString("room_index")); // (★수정★) room_number -> room_index
                seat.setSeatIndex(rs.getInt("seat_index"));
                seat.setSeatNumber(rs.getString("seat_number"));
                seat.setStatus(rs.getString("status"));
                
                int userId = rs.getInt("current_user_id"); // (★수정★) user_id -> current_user_id
                if (rs.wasNull()) {
                    seat.setCurrentUserId(null);
                } else {
                    seat.setCurrentUserId(userId);
                }
                
                seat.setCurrentUserName(rs.getString("current_user_name"));

                java.sql.Timestamp startTime = rs.getTimestamp("start_time");
                seat.setStartTime(startTime != null ? startTime.toLocalDateTime() : null);
                
                java.sql.Timestamp endTime = rs.getTimestamp("end_time");
                seat.setEndTime(endTime != null ? endTime.toLocalDateTime() : null);
                
                // (★수정★) rs.getBoolean("reserved") 삭제
                
                seats.add(seat);
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
                    // (★수정★) 생성자가 아닌 Setter를 사용
                    Seat seat = new Seat();
                    seat.setId(rs.getInt("id"));
                    seat.setFloor(rs.getInt("floor"));
                    seat.setRoomNumber(rs.getString("room_index")); // (★수정★) room_number -> room_index
                    seat.setSeatIndex(rs.getInt("seat_index"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    seat.setStatus(rs.getString("status"));
                    
                    int userId = rs.getInt("current_user_id"); // (★수정★) user_id -> current_user_id
                    if (rs.wasNull()) {
                        seat.setCurrentUserId(null);
                    } else {
                        seat.setCurrentUserId(userId);
                    }
                    
                    seat.setCurrentUserName(rs.getString("current_user_name"));
    
                    java.sql.Timestamp startTime = rs.getTimestamp("start_time");
                    seat.setStartTime(startTime != null ? startTime.toLocalDateTime() : null);
                    
                    java.sql.Timestamp endTime = rs.getTimestamp("end_time");
                    seat.setEndTime(endTime != null ? endTime.toLocalDateTime() : null);
                    
                    // (★수정★) rs.getBoolean("reserved") 삭제
                    
                    return seat;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateSeat(Seat seat) {
        // (★수정★) SeatService가 변경하는 컬럼들만 업데이트
        String sql = "UPDATE seats SET status=?, current_user_id=?, current_user_name=?, start_time=?, end_time=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, seat.getStatus());
            
            // Integer (nullable)
            if (seat.getCurrentUserId() == null) {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(2, seat.getCurrentUserId());
            }
            
            pstmt.setString(3, seat.getCurrentUserName());
            
            // LocalDateTime (nullable)
            if (seat.getStartTime() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(seat.getStartTime()));
            } else {
                pstmt.setNull(4, java.sql.Types.TIMESTAMP);
            }
            
            if (seat.getEndTime() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(seat.getEndTime()));
            } else {
                pstmt.setNull(5, java.sql.Types.TIMESTAMP);
            }
            
            pstmt.setInt(6, seat.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}