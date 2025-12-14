package impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection; 
import model.Penalty;
import model.Seat;
import model.User;

public class AdminDAOimpl {

    // =========================================================
    // [신규] 0. 일일 초기화 (하루가 지나면 실행)
    // =========================================================
    /**
     * 점검중('M')인 좌석을 제외하고 모든 좌석을 'A'(빈 좌석)로 초기화합니다.
     * 또한 현재 대기 중이거나 사용 중인 예약을 모두 'EXPIRED' 처리합니다.
     */
    public void dailyCleanup() {
        Connection conn = null;
        PreparedStatement pstmtSeat = null;
        PreparedStatement pstmtRes = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 좌석 초기화: 상태가 'M'(점검)이 아닌 좌석들을 'A'(사용가능)로 변경, 사용자ID 0으로 초기화
            String sqlSeat = "UPDATE seats SET status = 'A', current_user_id = 0 WHERE status != 'M'";
            pstmtSeat = conn.prepareStatement(sqlSeat);
            int clearedSeats = pstmtSeat.executeUpdate();
            System.out.println("[Daily Cleanup] 초기화된 좌석 수: " + clearedSeats);

            // 2. 예약 정리: 상태가 'R'(예약대기) 또는 'IN_USE'(사용중)인 건들을 'EXPIRED'로 변경
            String sqlRes = "UPDATE reservations SET status = 'EXPIRED' WHERE status IN ('R', 'IN_USE')";
            pstmtRes = conn.prepareStatement(sqlRes);
            int expiredReservations = pstmtRes.executeUpdate();
            System.out.println("[Daily Cleanup] 만료 처리된 예약/사용 수: " + expiredReservations);

            conn.commit(); // 적용
            
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            System.err.println("SQL 오류 - dailyCleanup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmtSeat != null) pstmtSeat.close();
                if (pstmtRes != null) pstmtRes.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {}
        }
    }

    // =========================================================
    // 1. 범인 찾기 (로그 기반)
    // =========================================================
    public User getOffenderByLog(int seatIndex, LocalDateTime reportTime) {
        if (reportTime == null) reportTime = LocalDateTime.now();
        
        String sql = "SELECT * FROM times ORDER BY log_time DESC LIMIT 200";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
                
            while(rs.next()) {
                String dbSeat = rs.getString("seat_log");
                String dbType = rs.getString("log_type");
                Timestamp dbTimestamp = rs.getTimestamp("log_time");
                
                if (dbSeat == null || dbTimestamp == null) continue;
                
                if (dbSeat.trim().equals(String.valueOf(seatIndex))) {
                    if (dbType != null && dbType.trim().equalsIgnoreCase("I")) { // 입실(I)
                        LocalDateTime logDateTime = dbTimestamp.toLocalDateTime();
                        
                        // 같은 날짜이거나 그 이전이면 범인으로 간주
                        boolean isSameDay = logDateTime.toLocalDate().isEqual(reportTime.toLocalDate());
                        boolean isBefore = logDateTime.isBefore(reportTime);

                        if (isSameDay || isBefore) {
                            User u = new User();
                            int stId = rs.getInt("st_id");
                            String name = rs.getString("st_name"); 
                            u.setStudentId(String.valueOf(stId));
                            u.setName(name != null ? name : "이름미상");
                            try { u.setId(getUserPkByStudentId(stId)); } catch (Exception e) { u.setId(0); }
                            return u;
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null; 
    }

    // =========================================================
    // 2. 신고 목록 조회
    // =========================================================
    public List<Penalty> getAllUserReports() {
        List<Penalty> list = new ArrayList<>();
        String sql = "SELECT p.num, p.seat_index, p.reason, p.report_time, " +
                     "u.name AS reporter_name, u.st_id AS reporter_student_id " +
                     "FROM penalty p LEFT JOIN users u ON p.st_id = u.id " +
                     "WHERE p.reporter_type = 'USER' ORDER BY p.report_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {
                Penalty p = new Penalty();
                p.setSeatIndex(rs.getInt("seat_index"));
                p.setReason(rs.getString("reason"));
                if (rs.getTimestamp("report_time") != null) {
                    p.setReportTime(rs.getTimestamp("report_time").toLocalDateTime());
                }
                String rName = rs.getString("reporter_name");
                int rStId = rs.getInt("reporter_student_id");
                if (rName != null) {
                    p.setStudentName("[신고] " + rName); 
                    p.setStudentRealId(String.valueOf(rStId)); 
                } else {
                    p.setStudentName("정보 없음"); p.setStudentRealId("-");
                }
                p.setStId(0); 
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================
    // 3. 노쇼 자동 처리 (30분 지난 대기 상태 변경)
    // =========================================================
    public void processNoShowReservations() {
        String sql = "UPDATE reservations SET status = 'NOSHOW' WHERE status = 'PENDING' AND reservation_time < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================
    // 4. 미퇴실 시간 초과자 목록 (이름 뒤에 (미퇴실) 태그 추가)
    // =========================================================
    public List<String> getOverdueReservations() {
        List<String> overdueList = new ArrayList<>();
        // NOSHOW 상태(30분 초과 미입실) 혹은 이용중인데 시간 초과된 경우 등을 가져오는 로직이라고 가정
        String sql = "SELECT u.id AS user_id, u.name, s.seat_index, r.reservation_time, r.id AS res_id " +
                     "FROM reservations r JOIN users u ON r.user_id = u.id " +
                     "JOIN seats s ON r.seat_id = s.id " +
                     "WHERE r.status = 'NOSHOW'"; // 혹은 필요한 조건
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                overdueList.add(
                    rs.getInt("user_id") + "," + 
                    rs.getInt("seat_index") + "," + 
                    rs.getString("name") + " (미퇴실)" + "," + // (미퇴실) 태그
                    rs.getString("reservation_time") + "," + 
                    rs.getInt("res_id")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return overdueList;
    }

    // =========================================================
    // 5. 노쇼(10분 초과) 사용자 조회 (이름 뒤에 (노쇼) 태그 추가)
    // =========================================================
    public List<String> getNoShowUsers() {
        List<String> noShowUsers = new ArrayList<>();
        String sql = "SELECT r.user_id, r.seat_id, u.name, r.reservation_time, r.id AS res_id " +
                     "FROM reservations r JOIN users u ON r.user_id = u.id " +
                     "WHERE r.status = 'R' AND r.reservation_time < DATE_SUB(NOW(), INTERVAL 10 MINUTE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String result = rs.getInt("user_id") + "," +
                                rs.getInt("seat_id") + "," +
                                rs.getString("name") + " (노쇼)" + "," + // (노쇼) 태그
                                rs.getObject("reservation_time", LocalDateTime.class).toString() + "," +
                                rs.getInt("res_id");
                noShowUsers.add(result);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return noShowUsers;
    }

    // =========================================================
    // 6. 예약자 명단 조회 (R 상태)
    // =========================================================
    public List<String> getSeatReservations(int seatId) {
        List<String> reservations = new ArrayList<>();
        String sql = "SELECT u.name, r.reservation_time FROM reservations r " + 
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.seat_id = ? AND r.status = 'R' " + 
                     "ORDER BY r.reservation_time ASC"; 

        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId); 
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    LocalDateTime startTime = rs.getObject("reservation_time", LocalDateTime.class);
                    String timeStr = startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    reservations.add(name + " (예정: " + timeStr + ")"); 
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return reservations; 
    }

    // --- [기타 필수 메서드들] ---

    public void updateReservationStatusToPenalized(int reservationId) {
        String sql = "UPDATE reservations SET status = 'PENALIZED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId); pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Integer> getFloors() {
        List<Integer> floors = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT floor FROM seats ORDER BY floor"); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) floors.add(rs.getInt("floor"));
        } catch (SQLException e) { e.printStackTrace(); }
        return floors;
    }

    public List<String> getRoomsByFloor(int floor) {
        List<String> rooms = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT room_index FROM seats WHERE floor = ? ORDER BY room_index")) {
            pstmt.setInt(1, floor);
            try (ResultSet rs = pstmt.executeQuery()) { while (rs.next()) rooms.add(rs.getString("room_index")); }
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }
    
    public List<Seat> getAllSeatStatusByRoom(int floor, String roomName) {
        List<Seat> seats = new ArrayList<>();
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
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, floor); pstmt.setString(2, roomName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getInt("id")); seat.setRoomNumber(rs.getString("room_index")); seat.setSeatIndex(rs.getInt("seat_index")); seat.setStatus(rs.getString("status")); seat.setFloor(rs.getInt("floor")); seat.setSeatNumber(rs.getString("seat_number"));
                    int currentUid = rs.getInt("current_user_id"); if (!rs.wasNull()) seat.setCurrentUserId(currentUid);
                    String uName = rs.getString("user_name"); int uId = rs.getInt("st_id");
                    if(uName != null) { seat.setCurrentUserName(uName + "\n(" + uId + ")"); } else { seat.setCurrentUserName(null); }
                    if (rs.getTimestamp("start_time") != null) seat.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                    if (rs.getTimestamp("end_time") != null) seat.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                    seats.add(seat);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return seats;
    }
    
    public boolean insertPenalty(int userId, String reason, int seatIndex) {
        String sql = "INSERT INTO penalty (st_id, reason, seat_index, report_time, reporter_type) VALUES (?, ?, ?, NOW(), 'ADMIN')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId); pstmt.setString(2, reason); pstmt.setInt(3, seatIndex); return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    public boolean forceEjectUser(int userId, String reason) {
        String sql = "UPDATE seats SET status = 'A', current_user_id = 0 WHERE current_user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId); return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    public boolean updateSeatStatus(int seatId, String status) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status); pstmt.setInt(2, seatId); return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Penalty> getAllAdminPenalties() {
        List<Penalty> list = new ArrayList<>();
        String sql = "SELECT p.num, p.seat_index, p.reason, p.report_time, u.st_id AS student_real_id, u.name FROM penalty p JOIN users u ON p.st_id = u.id WHERE p.reporter_type = 'ADMIN' ORDER BY p.report_time DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {
                Penalty p = new Penalty();
                p.setSeatIndex(rs.getInt("seat_index")); p.setStudentRealId(String.valueOf(rs.getInt("student_real_id"))); p.setStudentName(rs.getString("name")); p.setReason(rs.getString("reason"));
                if (rs.getTimestamp("report_time") != null) p.setReportTime(rs.getTimestamp("report_time").toLocalDateTime()); list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    public int getUserPkByStudentId(int studentId) {
        String sql = "SELECT id FROM users WHERE st_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try(ResultSet rs = pstmt.executeQuery()) { if(rs.next()) return rs.getInt("id"); }
        } catch (SQLException e) {}
        return 0;
    }
    
    public List<User> getSeatHistory(int seatIndex) {
        List<User> historyList = new ArrayList<>();
        String sql = "SELECT * FROM times ORDER BY log_time DESC LIMIT 200"; 
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String dbSeat = rs.getString("seat_log"); String dbType = rs.getString("log_type");
                if (dbSeat == null) continue;
                try {
                    int dbSeatInt = Integer.parseInt(dbSeat.trim());
                    if (dbSeatInt == seatIndex) {
                        if (dbType != null && dbType.trim().equalsIgnoreCase("I")) {
                            User u = new User(); int stId = rs.getInt("st_id"); String name = rs.getString("st_name"); 
                            u.setStudentId(String.valueOf(stId)); u.setName(name != null ? name : "이름미상");
                            Timestamp ts = rs.getTimestamp("log_time");
                            if(ts != null) { String timeStr = ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")); u.setName(u.getName() + " (" + timeStr + ")"); }
                            historyList.add(u);
                        }
                    }
                } catch (NumberFormatException e) { }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return historyList;
    }
}