package impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.Penalty;
import model.Seat;

public class AdminDAOimpl {

    // 1. 프로그램 시작 시 30분 지난 미입실 예약을 'NOSHOW'로 변경 (좌석 비우기)
    public void processNoShowReservations() {
        String sql = "UPDATE reservations " +
                     "SET status = 'NOSHOW' " +
                     "WHERE status = 'PENDING' " +
                     "AND reservation_time < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int count = pstmt.executeUpdate();
            if(count > 0) {
                System.out.println("[시스템] 30분 초과 예약 " + count + "건을 NOSHOW 처리했습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. 시간 초과자(NOSHOW) 목록 조회
    public List<String> getOverdueReservations() {
        List<String> overdueList = new ArrayList<>();
        String sql = "SELECT u.id AS user_id, u.name, s.seat_index, r.reservation_time, r.id AS res_id " +
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN seats s ON r.seat_id = s.id " + 
                     "WHERE r.status = 'NOSHOW'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("name");
                int seatIdx = rs.getInt("seat_index");
                String time = rs.getString("reservation_time");
                int resId = rs.getInt("res_id");
                overdueList.add(userId + "," + seatIdx + "," + userName + "," + time + "," + resId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return overdueList;
    }

    // 3. 패널티 부여 완료 후 상태 변경 (NOSHOW -> PENALIZED)
    public void updateReservationStatusToPenalized(int reservationId) {
        String sql = "UPDATE reservations SET status = 'PENALIZED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 4. 특정 좌석의 예약자 명단 확인
    public List<String> getSeatReservations(int seatId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT u.name, u.st_id, r.reservation_time " + 
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.seat_id = ? AND r.status = 'PENDING'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String studentId = String.valueOf(rs.getInt("st_id")); 
                    String timeStr = rs.getString("reservation_time");
                    String time = (timeStr != null && timeStr.length() > 16) ? timeStr.substring(11, 16) : timeStr;
                    list.add(name + " (" + studentId + ") - " + time);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 5. 층 목록
    public List<Integer> getFloors() {
        List<Integer> floors = new ArrayList<>();
        String sql = "SELECT DISTINCT floor FROM seats ORDER BY floor";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) floors.add(rs.getInt("floor"));
        } catch (SQLException e) { e.printStackTrace(); }
        return floors;
    }

    // 6. 룸 목록
    public List<String> getRoomsByFloor(int floor) {
        List<String> rooms = new ArrayList<>();
        String sql = "SELECT DISTINCT room_index FROM seats WHERE floor = ? ORDER BY room_index";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, floor);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) rooms.add(rs.getString("room_index"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }
    
    // 7. 좌석 정보 가져오기 (★수정됨: 예약 시간 확인 로직 추가)
 // 7. 좌석 정보 가져오기 (수정: 학번 + 시간 정보 포함)
    public List<Seat> getAllSeatStatusByRoom(int floor, String roomName) {
        List<Seat> seats = new ArrayList<>();
        
        // ★ u.st_id 추가됨 (학번 가져오기)
        String sql = "SELECT s.*, u.name AS user_name, u.st_id, " +
                     "r.reservation_time AS start_time, " +
                     "DATE_ADD(r.reservation_time, INTERVAL 4 HOUR) AS end_time " + 
                     "FROM seats s " +
                     "LEFT JOIN users u ON s.current_user_id = u.id " +
                     "LEFT JOIN reservations r ON s.current_user_id = r.user_id " +
                     "    AND s.id = r.seat_id " +
                     "    AND (r.status = 'IN_USE' OR r.status = 'PENDING' OR r.status = 'NOSHOW') " + 
                     "WHERE s.floor = ? AND s.room_index = ? " +
                     "ORDER BY s.seat_index";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, floor);
            pstmt.setString(2, roomName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getInt("id"));
                    seat.setRoomNumber(rs.getString("room_index"));
                    seat.setSeatIndex(rs.getInt("seat_index"));
                    seat.setStatus(rs.getString("status"));
                    seat.setFloor(rs.getInt("floor"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    
                    int currentUid = rs.getInt("current_user_id");
                    if (!rs.wasNull()) seat.setCurrentUserId(currentUid);
                    
                    // ★ [수정됨] 이름 + 학번 같이 저장 (예: "홍길동 (20231234)")
                    String uName = rs.getString("user_name");
                    int uId = rs.getInt("st_id");
                    if(uName != null) {
                        seat.setCurrentUserName(uName + "\n(" + uId + ")"); // 줄바꿈 추가
                    } else {
                        seat.setCurrentUserName(null);
                    }

                    // 시간 저장
                    if (rs.getTimestamp("start_time") != null) {
                        seat.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                    }
                    if (rs.getTimestamp("end_time") != null) {
                        seat.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                    }
                    
                    seats.add(seat);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return seats;
    }
    
    // 8. 패널티 부여 (ADMIN)
    public boolean insertPenalty(int userId, String reason, int seatIndex) {
        String sql = "INSERT INTO penalty (st_id, reason, seat_index, report_time, reporter_type) VALUES (?, ?, ?, NOW(), 'ADMIN')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, reason);
            pstmt.setInt(3, seatIndex);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    // 9. 강제 퇴실
    public boolean forceEjectUser(int userId, String reason) {
        String sql = "UPDATE seats SET status = 'A', current_user_id = 0 WHERE current_user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    // 10. 좌석 상태 변경
    public boolean updateSeatStatus(int seatId, String status) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, seatId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 11. 관리자 패널티 현황
    public List<Penalty> getAllAdminPenalties() {
        List<Penalty> list = new ArrayList<>();
        String sql = "SELECT p.num, p.seat_index, p.reason, p.report_time, u.st_id AS student_real_id, u.name " +
                     "FROM penalty p " +
                     "JOIN users u ON p.st_id = u.id " + 
                     "WHERE p.reporter_type = 'ADMIN' " + 
                     "ORDER BY p.report_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {
                Penalty p = new Penalty();
                p.setSeatIndex(rs.getInt("seat_index"));
                p.setStudentRealId(String.valueOf(rs.getInt("student_real_id"))); 
                p.setStudentName(rs.getString("name"));
                p.setReason(rs.getString("reason"));
                if (rs.getTimestamp("report_time") != null) {
                    p.setReportTime(rs.getTimestamp("report_time").toLocalDateTime());
                }
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    // 12. 신고 접수 목록
    public List<Penalty> getAllUserReports() {
        List<Penalty> list = new ArrayList<>();
        String sql = "SELECT p.num, p.seat_index, u.st_id AS student_real_id, u.name, p.reason, p.report_time, u.id AS u_id " +
                     "FROM penalty p " +
                     "JOIN users u ON p.st_id = u.id " +
                     "WHERE p.reporter_type = 'USER' " + 
                     "ORDER BY p.report_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {
                Penalty p = new Penalty();
                p.setSeatIndex(rs.getInt("seat_index"));
                p.setStudentRealId(String.valueOf(rs.getInt("student_real_id")));
                p.setStudentName(rs.getString("name"));
                p.setReason(rs.getString("reason"));
                p.setStId(rs.getInt("u_id")); 
                if (rs.getTimestamp("report_time") != null) {
                    p.setReportTime(rs.getTimestamp("report_time").toLocalDateTime());
                }
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}