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

public class AdminDAOimpl  {
	
    // --- 층/방 목록 조회 메서드 (수정 없음) ---

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

    // --- 좌석 상태 조회 메서드 (수정 완료) ---
    
    public List<Seat> getAllSeatStatusByRoom(String roomName) {
        List<Seat> seatList = new ArrayList<>();
        
        // ★수정: users 테이블과 LEFT JOIN하여 u.name(사용자 이름)을 가져옵니다. 
        // ★수정: JOIN 조건에서 CAST를 제거하고 INT 타입으로 직접 비교합니다.
        String sql = "SELECT s.*, u.name AS actual_user_name " + 
                     "FROM seats s " +
                     "LEFT JOIN users u ON s.current_user_id = u.id " + 
                     "WHERE s.room_index = ? ORDER BY s.seat_number"; 
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomName); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.seatId(rs.getInt("id"));
                    seat.seatFloor(rs.getInt("floor"));
                    seat.setRoomNumber(rs.getString("room_index")); 
                    seat.setSeatIndex(rs.getInt("seat_index"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    
                    // DB에서 가져온 상태값을 반각 대문자로 정규화
                    String statusFromDB = rs.getString("status");
                    seat.setStatus(normalizeStatus(statusFromDB)); 
                    
                    int currentUserId = rs.getInt("current_user_id");
                    if (rs.wasNull()) {
                        seat.setCurrentUserId(null); 
                        seat.setCurrentUserName(null); 
                    } else {
                        seat.setCurrentUserId(currentUserId);
                        // ★수정: JOIN으로 가져온 실제 사용자 이름 사용
                        seat.setCurrentUserName(rs.getString("actual_user_name"));
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
    
    // --- 유저 퇴출 메서드 (수정 완료) ---

    public boolean ejectUserFromSeat(int userId) {
        // ★수정: 전각 문자 'Ｅ' 대신 반각 문자 'E'를 사용합니다.
        String sql = "UPDATE seats SET status = 'E', current_user_id = NULL, " +
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
    
    // --- 좌석 상태 설정 메서드 (수정 완료) ---

    public boolean setSeatStatus(int seatId, String newStatus) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // ★수정: 입력된 상태값을 반각 대문자로 정규화하여 저장합니다.
            String normalizedStatus = normalizeStatus(newStatus); 
                                     
            pstmt.setString(1, normalizedStatus); 
            pstmt.setInt(2, seatId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; 

        } catch (SQLException e) {
            System.out.println("setSeatStatus 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
    
    // --- 헬퍼 메서드 추가 (전각 문자를 반각 대문자로 변환) ---
    
    private String normalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "G"; // 기본값 (공석)
        }
        // 전각 문자를 반각 문자로 대체 후, 전체를 대문자로 변환
        return status.trim().toUpperCase()
                     .replace('Ｒ', 'R')
                     .replace('Ｇ', 'G')
                     .replace('Ｅ', 'E')
                     .replace('Ｃ', 'C');
    }
    
    // --- 페널티 관련 메서드 (수정 없음) ---
    
    public boolean addPenalty(Penalty penalty) {
        // ... (이전 코드)
        return false;
    }

    public List<Penalty> getAllPenalties() {
        List<Penalty> penaltyList = new ArrayList<>();
        String sql = "SELECT * FROM penalty ORDER BY report_time DESC"; 
        
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