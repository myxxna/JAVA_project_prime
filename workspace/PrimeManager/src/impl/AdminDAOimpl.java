package impl; 

import config.DBConnection;
import model.Penalty;
import model.Seat; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime; // (★필수★)
import java.util.ArrayList;
import java.util.List;

public class AdminDAOimpl {

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

    public List<String> getUniqueRoomsByFloor(int floor) {
        List<String> roomNames = new ArrayList<>();
        String sql = "SELECT DISTINCT room_index FROM seats WHERE floor = ? ORDER BY room_index";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, floor);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roomNames.add(rs.getString("room_index"));
                }
            }
        } catch (SQLException e) {
            System.out.println("getUniqueRoomsByFloor 중 DB 오류 발생");
            e.printStackTrace();
        }
        return roomNames;
    }
    
    public List<String> getUniqueRoomNames() {
        List<String> roomNames = new ArrayList<>();
        String sql = "SELECT DISTINCT room_index FROM seats ORDER BY room_index";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                roomNames.add(rs.getString("room_index"));
            }
        } catch (SQLException e) {
            System.out.println("getUniqueRoomNames 중 DB 오류 발생");
            e.printStackTrace();
        }
        return roomNames;
    }

    public List<Seat> getAllSeatStatusByRoom(String roomName) {
        List<Seat> seatList = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE room_index = ? ORDER BY seat_number"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomName); 
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getInt("id"));
                    seat.setFloor(rs.getInt("floor"));
                    seat.setRoomNumber(rs.getString("room_index")); 
                    seat.setSeatIndex(rs.getInt("seat_index"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    seat.setStatus(rs.getString("status")); 
                    int currentUserId = rs.getInt("current_user_id");
                    if (rs.wasNull()) {
                        seat.setCurrentUserId(null); 
                    } else {
                        seat.setCurrentUserId(currentUserId);
                    }
                    seat.setCurrentUserName(rs.getString("current_user_name"));
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
        } catch (SQLException e) { e.printStackTrace(); }
        return seatList;
    }
    
    public boolean addPenalty(Penalty penalty) {
        // (★주의★) 이 메서드는 '이전' Penalty 모델을 사용하고 있을 수 있습니다.
        // 지금은 '신고 목록 조회'만 구현하므로 이 메서드는 수정하지 않습니다.
        // ... (이전 코드)
        return false;
    }
    
    public boolean ejectUserFromSeat(int userId) {
        String sql = "UPDATE seats SET status = 'Ｅ', current_user_id = NULL, " +
                     "current_user_name = NULL, " + 
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
    
    public boolean setSeatStatus(int seatId, String newStatus) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus); 
            pstmt.setInt(2, seatId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; 

        } catch (SQLException e) {
            System.out.println("setSeatStatus 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * (★신규★) '신고 목록' 탭에 보여줄 'penalty' 테이블의 모든 데이터를 가져옵니다.
     * @return Penalty 객체 리스트
     */
    public List<Penalty> getAllPenalties() {
        List<Penalty> penaltyList = new ArrayList<>();
        String sql = "SELECT * FROM penalty ORDER BY report_time DESC"; // 최신순 정렬
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Penalty p = new Penalty();
                p.setNum(rs.getInt("num"));
                p.setStId(rs.getInt("st_id"));
                p.setReason(rs.getString("reason"));
                p.setSeatIndex(rs.getInt("seat_index"));
                
                java.sql.Timestamp ts = rs.getTimestamp("report_time");
                if (ts != null) {
                    p.setReportTime(ts.toLocalDateTime());
                }
                
                penaltyList.add(p);
            }
        } catch (SQLException e) {
            System.out.println("getAllPenalties 중 DB 오류 발생");
            e.printStackTrace();
        }
        return penaltyList;
    }
}