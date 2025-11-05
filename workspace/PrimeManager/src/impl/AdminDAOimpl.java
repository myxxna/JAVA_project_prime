package impl; 

import config.DBConnection;
import model.Penalty;
import model.Seat; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminDAOimpl {

    /**
     * (★수정★) DB에서 중복 없이 '층' 목록을 가져옵니다.
     * @return 층 번호 리스트 (e.g., [4, 7])
     */
    public List<Integer> getUniqueFloors() {
        List<Integer> floors = new ArrayList<>();
        String sql = "SELECT DISTINCT floor FROM seats ORDER BY floor";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                floors.add(rs.getInt("floor"));
            }
        } catch (SQLException e) {
            System.out.println("getUniqueFloors 중 DB 오류 발생");
            e.printStackTrace();
        }
        return floors;
    }

    /**
     * (★신규★) 특정 층에 속한 중복 없는 '룸' 목록을 가져옵니다.
     * @param floor 층 번호
     * @return 룸 이름 리스트 (e.g., ["Room1"])
     */
    public List<String> getUniqueRoomsByFloor(int floor) {
        List<String> roomNames = new ArrayList<>();
        String sql = "SELECT DISTINCT room_number FROM seats WHERE floor = ? ORDER BY room_number";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, floor);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roomNames.add(rs.getString("room_number"));
                }
            }
        } catch (SQLException e) {
            System.out.println("getUniqueRoomsByFloor 중 DB 오류 발생");
            e.printStackTrace();
        }
        return roomNames;
    }

    /**
     * 특정 룸의 모든 좌석 현황 조회 (SELECT) (변경 없음)
     */
    public List<Seat> getAllSeatStatusByRoom(String roomName) {
        List<Seat> seatList = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE room_number = ? ORDER BY seat_number"; 
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, roomName); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getInt("id"));
                    // seat.setFloor(rs.getInt("floor")); // (모델에 추가했다면)
                    seat.setRoomNumber(rs.getString("room_number"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    seat.setReserved(rs.getBoolean("reserved"));
                    seat.setStatus(rs.getString("status")); 
 
                    int currentUserId = rs.getInt("current_user_id");
                    if (rs.wasNull()) {
                        seat.setCurrentUserId(null); 
                    } else {
                        seat.setCurrentUserId(currentUserId);
                    }

                    java.sql.Timestamp startTime = rs.getTimestamp("start_time");
                    if (startTime != null) {
                        seat.setStartTime(startTime.toLocalDateTime());
                    } else {
                        seat.setStartTime(null);
                    }

                    java.sql.Timestamp endTime = rs.getTimestamp("end_time");
                    if (endTime != null) {
                        seat.setEndTime(endTime.toLocalDateTime());
                    } else {
                        seat.setEndTime(null);
                    }
                    
                    seatList.add(seat);
                }
            }
        } catch (SQLException e) {
            System.out.println("getAllSeatStatusByRoom 중 DB 오류 발생");
            e.printStackTrace();
        }
        return seatList;
    }

    /**
     * 패널티 부여 (INSERT) (변경 없음)
     */
    public boolean addPenalty(Penalty penalty) {
        String sql = "INSERT INTO penalty (user_id, reason, date) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, penalty.getUserId());
            pstmt.setString(2, penalty.getReason()); 
            pstmt.setObject(3, penalty.getDate()); 
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("addPenalty 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 사용자 강제 퇴실 (UPDATE) (변경 없음)
     */
    public boolean ejectUserFromSeat(int userId) {
        String sql = "UPDATE seats SET status = 'G', current_user_id = NULL, " +
                     "start_time = NULL, end_time = NULL WHERE current_user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("ejectUserFromSeat 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 좌석 'status' 컬럼 변경 (G <-> R) (변경 없음)
     */
    public boolean setSeatStatus(int seatId, String newStatus) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus); // "G" 또는 "R"
            pstmt.setInt(2, seatId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; 

        } catch (SQLException e) {
            System.out.println("setSeatStatus 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
}