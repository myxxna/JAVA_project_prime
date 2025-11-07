package impl; 

import config.DBConnection;
import model.Penalty;
import model.Seat; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime; // ★추가: DB Timestamp를 Java LocalDateTime으로 변환하기 위해 필요
import java.util.ArrayList;
import java.util.List;

<<<<<<< Updated upstream
<<<<<<< HEAD
public class AdminDAOimpl  {
	
    // --- 층/방 목록 조회 메서드 (수정 없음) ---

=======
/**
 * 관리자 기능(Admin Service)에 필요한 모든 데이터베이스 접근 객체(DAO) 구현 클래스입니다.
 * seats, users, penalty 테이블에 대한 CRUD 작업을 수행합니다.
 */
public class AdminDAOimpl {

=======
/**
 * 관리자 기능(Admin Service)에 필요한 모든 데이터베이스 접근 객체(DAO) 구현 클래스입니다.
 * seats, users, penalty 테이블에 대한 CRUD 작업을 수행합니다.
 */
public class AdminDAOimpl {

>>>>>>> Stashed changes
    /**
     * DB에서 중복되지 않는 모든 층(floor) 목록을 조회합니다.
     * @return 층 번호(Integer) 리스트
     */
<<<<<<< Updated upstream
>>>>>>> main
=======
>>>>>>> Stashed changes
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
     * 특정 층(floor)에 속하는 중복되지 않는 룸(room_index) 목록을 조회합니다.
     * @param floor 조회할 층 번호
     * @return 룸 이름(String) 리스트
     */
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
    
    /**
     * DB 전체에서 중복되지 않는 모든 룸(room_index) 목록을 조회합니다.
     * @return 룸 이름(String) 리스트
     */
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

    /**
     * 특정 룸에 속하는 모든 좌석의 상태와 사용자 정보를 조회합니다.
     * users 테이블과 LEFT JOIN하여 사용자 이름(u.name)을 가져옵니다.
     * @param roomName 조회할 룸 이름
     * @return Seat 객체 리스트
     */
    public List<Seat> getAllSeatStatusByRoom(String roomName) {
        List<Seat> seatList = new ArrayList<>();
        
        // ★수정 반영: seats와 users를 JOIN하여 사용자 이름(u.name)을 가져오고 별칭(actual_user_name)을 지정
        String sql = "SELECT s.*, u.name AS actual_user_name " + 
                     "FROM seats s " +
                     // current_user_id와 users.id는 INT 타입으로 가정하여 JOIN
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
                    
                    // ★수정: DB에서 가져온 상태값을 정규화하여 설정 (전각/반각 문제 해결)
                    seat.setStatus(normalizeStatus(rs.getString("status"))); 
                    
                    int currentUserId = rs.getInt("current_user_id");
                    if (rs.wasNull()) {
                        seat.setCurrentUserId(null); 
                        seat.setCurrentUserName(null); 
                    } else {
                        seat.setCurrentUserId(currentUserId);
                        // ★수정: JOIN으로 가져온 실제 사용자 이름(actual_user_name) 사용
                        seat.setCurrentUserName(rs.getString("actual_user_name"));
                    }
                    
                    // DB Timestamp를 Java LocalDateTime으로 변환
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
     * DB에 패널티 정보를 추가합니다.
     * (이전 오류 해결: Penalty 모델의 getter를 getStId(), getReportTime()으로 가정하고 수정)
     * @param penalty Penalty 객체
     * @return 성공 여부
     */
    public boolean addPenalty(Penalty penalty) {
        // ★수정: user_id 대신 st_id, date 대신 report_time 컬럼 사용 가정
        String sql = "INSERT INTO penalty (st_id, reason, report_time) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // ★수정: getUserId() 대신 getStId() 사용
            pstmt.setInt(1, penalty.getStId()); 
            pstmt.setString(2, penalty.getReason()); 
            
            // ★수정: getDate() 대신 getReportTime() 사용
            pstmt.setObject(3, penalty.getReportTime()); 
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("addPenalty 중 DB 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 특정 사용자 ID의 좌석을 강제 퇴실 처리합니다.
     * 좌석 상태를 '사용 가능(E)'로 변경하고, 사용자 관련 정보를 모두 NULL 처리합니다.
     * @param userId 퇴실시킬 사용자 ID
     * @return 성공 여부
     */
    public boolean ejectUserFromSeat(int userId) {
        // ★수정: status를 반각 문자 'E'로 설정하고, current_user_name도 NULL 처리
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
    
    /**
     * 특정 좌석의 상태(status)를 변경합니다.
     * @param seatId 변경할 좌석 ID
     * @param newStatus 새로운 상태 값 (예: "C", "E")
     * @return 성공 여부
     */
    public boolean setSeatStatus(int seatId, String newStatus) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // ★수정: 상태값 정규화 후 DB에 저장
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
     * 'penalty' 테이블의 모든 데이터를 조회하여 신고 목록을 가져옵니다.
     * @return Penalty 객체 리스트 (최신순 정렬 가정)
     */
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
                
                // DB Timestamp를 Java LocalDateTime으로 변환
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
    
    /**
     * 상태 문자열을 반각 대문자로 정규화하는 헬퍼 메서드입니다.
     * 전각 문자 (Ｒ, Ｇ 등)를 반각 문자 (R, G 등)로 변환하여 DB의 일관성을 유지합니다.
     * @param status 변환할 상태 문자열
     * @return 정규화된 상태 문자열 (반각 대문자)
     */
    private String normalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "G"; // 기본값 (공석)
        }
        // 전각 문자를 반각 문자로 대체 후, 전체를 대문자로 변환
        return status.trim().toUpperCase()
                     .replace('Ｒ', 'R')
                     .replace('Ｇ', 'G')
                     .replace('Ｅ', 'E')
                     .replace('Ｃ', 'C')
                     .replace('Ｕ', 'U');
    }
}