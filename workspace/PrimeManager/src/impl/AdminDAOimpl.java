package impl; 

import config.DBConnection;
import model.Penalty;
import model.Seat; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime; 
import java.util.ArrayList;
import java.util.List;

public class AdminDAOimpl {

    // (getUniqueFloors, getUniqueRoomsByFloor, getUniqueRoomNames, getAllSeatStatusByRoom 메서드는 동일)
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
        
        String sql = "SELECT s.*, u.name AS actual_user_name, u.st_id AS actual_student_id " + 
                     "FROM seats s " +
                     "LEFT JOIN users u ON s.current_user_id = u.id " + 
                     "WHERE s.room_index = ? ORDER BY s.seat_number"; 
                     
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
                    
                    seat.setStatus(normalizeStatus(rs.getString("status"))); 
                    
                    int currentUserId = rs.getInt("current_user_id");
                    if (rs.wasNull()) { 
                        seat.setCurrentUserId(null); 
                        seat.setCurrentUserName(null); 
                    } else {
                        seat.setCurrentUserId(currentUserId);
                        
                        String name = rs.getString("actual_user_name");
                        String studentId = rs.getString("actual_student_id");
                        
                        if (name == null) name = "";
                        if (studentId == null) studentId = "";

                        seat.setCurrentUserName(name + "|" + studentId);
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
        } catch (SQLException e) { e.printStackTrace(); }
        return seatList;
    }
    
    /**
     * (★핵심 수정★)
     * DB에 패널티 정보를 추가할 때, 'reporter_type' 컬럼도 함께 INSERT합니다.
     * (AUTO_INCREMENT 오류는 DB 스키마에서 'num' 컬럼을 수정해야 함)
     */
    public boolean addPenalty(Penalty penalty, String reporterType) {
        
        // ★(수정)★ SQL 쿼리에 reporter_type 추가
        String sql = "INSERT INTO penalty (st_id, reason, report_time, seat_index, reporter_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, penalty.getStId()); // (penalty.st_id는 users.id PK)
            pstmt.setString(2, penalty.getReason()); 
            pstmt.setObject(3, penalty.getReportTime()); 
            pstmt.setInt(4, penalty.getSeatIndex()); // (좌석 인덱스)
            pstmt.setString(5, reporterType); // ★(수정)★ 5번째 파라미터로 'ADMIN' 또는 'USER' 설정
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("addPenalty 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * (★신규 추가★)
     * 'users' 테이블의 'penalty_count'를 1 증가시킵니다.
     * @param userId (패널티를 받을 users.id PK)
     * @return 성공 여부
     */
    public boolean incrementUserPenaltyCount(int userId) {
        String sql = "UPDATE users SET penalty_count = penalty_count + 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("incrementUserPenaltyCount 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * (★수정★)
     * 강제 퇴실 시 current_user_name 컬럼에 NULL 대신 빈 문자열('')을 삽입합니다.
     */
    public boolean ejectUserFromSeat(int userId) {
        
        String sql = "UPDATE seats SET status = 'E', current_user_id = NULL, " +
                     "current_user_name = '', " + 
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
     * 특정 좌석의 상태(status)를 변경합니다.
     */
    public boolean setSeatStatus(int seatId, String newStatus) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, normalizeStatus(newStatus)); 
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
     * (★수정★)
     * 'penalty' 테이블에서 **'USER'가 신고한 내역(신고 목록)**만 조회합니다.
     */
    public List<Penalty> getAllUserReports() {
        List<Penalty> penaltyList = new ArrayList<>();
        // ★(수정)★ WHERE reporter_type = 'USER' 조건 추가
        String sql = "SELECT * FROM penalty WHERE reporter_type = 'USER' ORDER BY report_time DESC"; 
        
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
            System.out.println("getAllUserReports 중 DB 오류 발생");
            e.printStackTrace();
        }
        return penaltyList;
    }
    
    /**
     * (★신규 추가★)
     * 'penalty' 테이블에서 **'ADMIN'이 부여한 내역(패널티 관리 목록)**만 조회합니다.
     */
    public List<Penalty> getAllAdminPenalties() {
        List<Penalty> penaltyList = new ArrayList<>();
        // ★(수정)★ WHERE reporter_type = 'ADMIN' 조건 추가
        String sql = "SELECT * FROM penalty WHERE reporter_type = 'ADMIN' ORDER BY report_time DESC"; 
        
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
            System.out.println("getAllAdminPenalties 중 DB 오류 발생");
            e.printStackTrace();
        }
        return penaltyList;
    }
    
    /**
     * 상태 문자열을 반각 대문자로 정규화하는 헬퍼 메서드입니다.
     */
    private String normalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "G"; 
        }
        return status.trim().toUpperCase()
                     .replace('Ｒ', 'R')
                     .replace('Ｇ', 'G')
                     .replace('Ｅ', 'E')
                     .replace('Ｃ', 'C')
                     .replace('Ｕ', 'U');
    }
}