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

    // 층 목록 조회
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

    // 층별 룸 목록 조회
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
    
    // 전체 룸 이름 조회
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

    // ★(핵심) 좌석 상태 조회 (Seat.java 필드 재활용 로직 포함)
    public List<Seat> getAllSeatStatusByRoom(int floor, String roomName) {
        List<Seat> seatList = new ArrayList<>();
        
        // SQL 설명: 
        // 1. users u -> 현재 좌석을 사용 중인 사람의 정보
        // 2. 서브쿼리(next_reserver_name) -> 현재 시간 이후, 대기 중인(PENDING) 가장 빠른 예약자 이름
        String sql = "SELECT s.*, " +
                "u.name AS current_user_realname, " +
                "(SELECT u2.name FROM reservations r " +
                " JOIN users u2 ON r.user_id = u2.id " +
                " WHERE r.seat_id = s.id AND r.status = 'PENDING' " + 
                // 시간 컬럼 대신 id로 정렬 (대부분의 경우 id순서 = 시간순서)
                " ORDER BY r.id ASC LIMIT 1) AS next_reserver_name " +
                "FROM seats s " +
                "LEFT JOIN users u ON s.current_user_id = u.id " + 
                "WHERE s.floor = ? AND s.room_index = ? ORDER BY s.seat_index";                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, floor);
            pstmt.setString(2, roomName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getInt("id"));
                    seat.setFloor(rs.getInt("floor"));
                    
                    // DB 컬럼 room_index -> Java 필드 roomNumber
                    seat.setRoomNumber(rs.getString("room_index"));
                    
                    // seat_index 처리 (없으면 0)
                    try { 
                        seat.setSeatIndex(rs.getInt("seat_index")); 
                    } catch(Exception e) { 
                        seat.setSeatIndex(0); 
                    }
                    
                    // seat_number 처리 (없으면 index를 문자로 변환)
                    String sNum = rs.getString("seat_number");
                    if (sNum == null || sNum.isEmpty()) {
                        seat.setSeatNumber(String.valueOf(seat.getSeatIndex()));
                    } else {
                        seat.setSeatNumber(sNum);
                    }
                    
                    String status = normalizeStatus(rs.getString("status"));
                    seat.setStatus(status);
                    
                    // ★필드 재활용 로직 시작★
                    // 1. DB에서 가져온 실제 사용자 이름
                    String actualUser = rs.getString("current_user_realname");
                    // 2. DB에서 가져온 다음 예약자 이름
                    String nextReserver = rs.getString("next_reserver_name");
                    
                    // currentUserId 세팅 (사용 중일 때만 유효)
                    int uid = rs.getInt("current_user_id");
                    if (!rs.wasNull()) seat.setCurrentUserId(uid);
                    
                    if ("U".equals(status)) {
                        // 사용 중(U)일 때는 -> '사용자 이름'을 넣는다.
                        seat.setCurrentUserName(actualUser != null ? actualUser : "사용자");
                    } else {
                        // 사용 중이 아닐 때(E, R, C)는 -> '예약자 이름'을 넣는다.
                        // 예약자가 없으면 null이 들어감 -> 화면에서 '빈 좌석'으로 처리됨
                        seat.setCurrentUserName(nextReserver); 
                    }
                    // ★필드 재활용 로직 끝★

                    java.sql.Timestamp startTs = rs.getTimestamp("start_time");
                    if (startTs != null) seat.setStartTime(startTs.toLocalDateTime());
                    
                    java.sql.Timestamp endTs = rs.getTimestamp("end_time");
                    if (endTs != null) seat.setEndTime(endTs.toLocalDateTime());
                    
                    seatList.add(seat);
                }
            }
        } catch (SQLException e) { 
            System.out.println("getAllSeatStatusByRoom 중 DB 오류 발생");
            e.printStackTrace(); 
        }
        return seatList;
    }

    // ★(신규) 예약 명단 조회 -> 문자열 리스트로 반환 (DTO 대체)
    public List<String> getSeatReservations(int seatId) {
        List<String> list = new ArrayList<>();
        
        // users 테이블과 조인하여 이름, 학번, 시간을 가져옴
        String sql = "SELECT r.id, r.user_id, u.name, r.seat_id, r.status " + 
                "FROM reservations r " +
                "JOIN users u ON r.user_id = u.id " +
                "WHERE r.seat_id = ? AND r.status = 'PENDING'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String stId = rs.getString("st_id");
                    java.sql.Timestamp ts = rs.getTimestamp("start_time");
                    String timeStr = (ts != null) ? ts.toLocalDateTime().toLocalTime().toString() : "시간미정";
                    
                    // 문자열로 포맷팅: "홍길동 (20201234) - 14:00"
                    list.add(name + " (" + stId + ") - " + timeStr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // 패널티 추가
    public boolean addPenalty(Connection conn, Penalty penalty, String reporterType) throws SQLException {
        String sql = "INSERT INTO penalty (st_id, reason, report_time, reporter_type, seat_index) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, penalty.getStId()); 
            pstmt.setString(2, penalty.getReason()); 
            pstmt.setObject(3, penalty.getReportTime()); 
            pstmt.setString(4, reporterType); 
            pstmt.setInt(5, penalty.getSeatIndex()); 
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } 
    }
    
    // 유저 패널티 카운트 증가
    public boolean incrementUserPenaltyCount(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users SET penalty_count = penalty_count + 1 WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // 강제 퇴실
    public boolean ejectUserFromSeat(int userId) {
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
    
    // 좌석 상태 변경
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
    
    // 유저 신고 목록 조회
    public List<Penalty> getAllUserReports() {
        List<Penalty> penaltyList = new ArrayList<>();
        String sql = "SELECT p.*, u.name AS studentName, u.st_id AS studentRealId " +
                     "FROM penalty p " +
                     "LEFT JOIN users u ON p.st_id = u.id " +
                     "WHERE p.reporter_type = 'USER' ORDER BY p.report_time DESC"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Penalty p = new Penalty();
                p.setNum(rs.getInt("num"));
                p.setStId(rs.getInt("st_id"));
                p.setReason(rs.getString("reason"));
                try { p.setSeatIndex(rs.getInt("seat_index")); } catch(SQLException e) { p.setSeatIndex(0); }
                java.sql.Timestamp ts = rs.getTimestamp("report_time");
                if (ts != null) p.setReportTime(ts.toLocalDateTime());
                p.setStudentName(rs.getString("studentName"));
                p.setStudentRealId(rs.getString("studentRealId"));
                penaltyList.add(p);
            }
        } catch (SQLException e) {
            System.out.println("getAllUserReports 중 DB 오류 발생");
            e.printStackTrace();
        }
        return penaltyList;
    }
    
    // 관리자 패널티 목록 조회
    public List<Penalty> getAllAdminPenalties() {
        List<Penalty> penaltyList = new ArrayList<>();
        String sql = "SELECT p.*, u.name AS studentName, u.st_id AS studentRealId " +
                     "FROM penalty p " +
                     "LEFT JOIN users u ON p.st_id = u.id " +
                     "WHERE p.reporter_type = 'ADMIN' ORDER BY p.report_time DESC"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Penalty p = new Penalty();
                p.setNum(rs.getInt("num"));
                p.setStId(rs.getInt("st_id"));
                p.setReason(rs.getString("reason"));
                try { p.setSeatIndex(rs.getInt("seat_index")); } catch(SQLException e) { p.setSeatIndex(0); }
                java.sql.Timestamp ts = rs.getTimestamp("report_time");
                if (ts != null) p.setReportTime(ts.toLocalDateTime());
                p.setStudentName(rs.getString("studentName"));
                p.setStudentRealId(rs.getString("studentRealId"));
                penaltyList.add(p);
            }
        } catch (SQLException e) {
            System.out.println("getAllAdminPenalties 중 DB 오류 발생");
            e.printStackTrace();
        }
        return penaltyList;
    }
    
    private String normalizeStatus(String status) {
        if (status == null || status.isEmpty()) return "G"; 
        return status.trim().toUpperCase()
                     .replace('Ｒ', 'R').replace('Ｇ', 'G').replace('Ｅ', 'E').replace('Ｃ', 'C').replace('Ｕ', 'U');
    }
}