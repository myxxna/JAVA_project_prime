package impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.Penalty;
import model.Seat;
import model.User;
import java.time.format.DateTimeFormatter;

public class AdminDAOimpl {

    // [신규 기능] 로그(times) 테이블에서 특정 시간, 특정 좌석의 사용자(범인) 찾기
	// [최종 해결판] 타임존(Timezone) 문제 무시하고 같은 날짜면 범인 잡는 코드
	public User getOffenderByLog(int seatIndex, LocalDateTime reportTime) {
	    System.out.println("\n========== [범인 찾기 로직 시작] ==========");
	    
	    // 1. 신고 시간이 없으면 현재 시간으로 대체
	    if (reportTime == null) {
	        reportTime = LocalDateTime.now();
	    }
	    System.out.println("🎯 비교 기준: 좌석 " + seatIndex + "번 | 신고시간: " + reportTime);

	    // 2. 최신 로그 200개 조회
	    String sql = "SELECT * FROM times ORDER BY log_time DESC LIMIT 200";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql);
	         ResultSet rs = pstmt.executeQuery()) {
	            
	        while(rs.next()) {
	            String dbSeat = rs.getString("seat_log");
	            String dbType = rs.getString("log_type");
	            Timestamp dbTimestamp = rs.getTimestamp("log_time");
	            
	            if (dbSeat == null || dbTimestamp == null) continue;
	            
	            // 3. 좌석 번호 비교 (공백 제거)
	            if (dbSeat.trim().equals(String.valueOf(seatIndex))) {
	                
	                // 4. 입실(I) 타입 확인
	                if (dbType != null && dbType.trim().equalsIgnoreCase("I")) {
	                    
	                    LocalDateTime logDateTime = dbTimestamp.toLocalDateTime();
	                    System.out.println("   >>> 🔍 후보 발견! 로그시간: " + logDateTime);

	                    // ★★★ [핵심 수정] 타임존 꼬임 방지 ★★★
	                    // "시간(시/분/초)"까지 따지니 오류가 납니다.
	                    // 그냥 "같은 날짜(연/월/일)"이거나 "그 이전 날짜"면 범인으로 인정합니다.
	                    boolean isSameDay = logDateTime.toLocalDate().isEqual(reportTime.toLocalDate());
	                    boolean isBefore = logDateTime.isBefore(reportTime);

	                    if (isSameDay || isBefore) {
	                         System.out.println("   >>> ✅ 날짜 조건 통과! (범인 확정)");

	                        // === 범인 객체 리턴 ===
	                        User u = new User();
	                        int stId = rs.getInt("st_id");
	                        String name = rs.getString("st_name"); 
	                        
	                        u.setStudentId(String.valueOf(stId));
	                        u.setName(name != null ? name : "이름미상");
	                        
	                        try {
	                            u.setId(getUserPkByStudentId(stId));
	                        } catch (Exception e) {
	                            u.setId(0); 
	                        }
	                        return u;
	                    } else {
	                         System.out.println("   >>> ❌ 날짜가 미래라서 탈락 (로그: " + logDateTime + ")");
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    System.out.println("💀 [실패] 조건에 맞는 범인을 못 찾음.");
	    return null; 
	}

    // [신규 로직] 신고 접수 목록 (화면에는 '신고자'만 보여줌, 범인은 컨트롤러에서 찾음)
    public List<Penalty> getAllUserReports() {
        List<Penalty> list = new ArrayList<>();
        
        // p.st_id는 '신고한 사람'입니다. LEFT JOIN으로 신고자 정보를 가져옵니다.
        String sql = "SELECT p.num, p.seat_index, p.reason, p.report_time, " +
                     "u.name AS reporter_name, u.st_id AS reporter_student_id " +
                     "FROM penalty p " +
                     "LEFT JOIN users u ON p.st_id = u.id " +
                     "WHERE p.reporter_type = 'USER' " + 
                     "ORDER BY p.report_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next()) {
                Penalty p = new Penalty();
                // 모델에 setNum이 있다면 주석 해제, 없으면 setId 사용
                // p.setNum(rs.getInt("num")); 
                // p.setId(rs.getInt("num")); 
                
                p.setSeatIndex(rs.getInt("seat_index"));
                p.setReason(rs.getString("reason"));
                
                if (rs.getTimestamp("report_time") != null) {
                    p.setReportTime(rs.getTimestamp("report_time").toLocalDateTime());
                }
                
                // 화면 표시용: 신고자 정보 설정
                String rName = rs.getString("reporter_name");
                int rStId = rs.getInt("reporter_student_id");
                
                if (rName != null) {
                    p.setStudentName("[신고] " + rName); 
                    p.setStudentRealId(String.valueOf(rStId)); 
                } else {
                    p.setStudentName("정보 없음");
                    p.setStudentRealId("-");
                }
                
                // 여기서는 범인을 찾지 않습니다. (버튼 누를 때 팝업으로 띄우기 위해)
                p.setStId(0); 
                
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 1. 노쇼 자동 처리
    public void processNoShowReservations() {
        String sql = "UPDATE reservations SET status = 'NOSHOW' WHERE status = 'PENDING' AND reservation_time < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 2. 시간 초과자 목록
    public List<String> getOverdueReservations() {
        List<String> overdueList = new ArrayList<>();
        String sql = "SELECT u.id AS user_id, u.name, s.seat_index, r.reservation_time, r.id AS res_id FROM reservations r JOIN users u ON r.user_id = u.id JOIN seats s ON r.seat_id = s.id WHERE r.status = 'NOSHOW'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                overdueList.add(rs.getInt("user_id") + "," + rs.getInt("seat_index") + "," + rs.getString("name") + "," + rs.getString("reservation_time") + "," + rs.getInt("res_id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return overdueList;
    }

    // 3. 상태 변경 (패널티 부여 후)
    public void updateReservationStatusToPenalized(int reservationId) {
        String sql = "UPDATE reservations SET status = 'PENALIZED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // 4. 예약자 확인
    public List<String> getSeatReservations(int seatId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT u.name, u.st_id, r.reservation_time FROM reservations r JOIN users u ON r.user_id = u.id WHERE r.seat_id = ? AND r.status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String timeStr = rs.getString("reservation_time");
                    String time = (timeStr != null && timeStr.length() > 16) ? timeStr.substring(11, 16) : timeStr;
                    list.add(rs.getString("name") + " (" + rs.getInt("st_id") + ") - " + time);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 5. 층 목록
    public List<Integer> getFloors() {
        List<Integer> floors = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT floor FROM seats ORDER BY floor"); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) floors.add(rs.getInt("floor"));
        } catch (SQLException e) { e.printStackTrace(); }
        return floors;
    }

    // 6. 룸 목록
    public List<String> getRoomsByFloor(int floor) {
        List<String> rooms = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT DISTINCT room_index FROM seats WHERE floor = ? ORDER BY room_index")) {
            pstmt.setInt(1, floor);
            try (ResultSet rs = pstmt.executeQuery()) { while (rs.next()) rooms.add(rs.getString("room_index")); }
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }
    
    // 7. 좌석 정보 가져오기 (시간, 학번 포함)
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
                    
                    String uName = rs.getString("user_name");
                    int uId = rs.getInt("st_id");
                    if(uName != null) {
                        seat.setCurrentUserName(uName + "\n(" + uId + ")");
                    } else {
                        seat.setCurrentUserName(null);
                    }

                    if (rs.getTimestamp("start_time") != null) seat.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                    if (rs.getTimestamp("end_time") != null) seat.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                    seats.add(seat);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return seats;
    }
    
    // 8. 패널티 부여 (관리자용, reporter_type = ADMIN)
    public boolean insertPenalty(int userId, String reason, int seatIndex) {
        String sql = "INSERT INTO penalty (st_id, reason, seat_index, report_time, reporter_type) VALUES (?, ?, ?, NOW(), 'ADMIN')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId); pstmt.setString(2, reason); pstmt.setInt(3, seatIndex);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    // 9. 강제 퇴실 (seats 테이블만 초기화)
    public boolean forceEjectUser(int userId, String reason) {
        String sql = "UPDATE seats SET status = 'A', current_user_id = 0 WHERE current_user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId); return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    // 10. 좌석 상태 변경
    public boolean updateSeatStatus(int seatId, String status) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status); pstmt.setInt(2, seatId); return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 11. 관리자 패널티 부여 현황 목록
    public List<Penalty> getAllAdminPenalties() {
        List<Penalty> list = new ArrayList<>();
        String sql = "SELECT p.num, p.seat_index, p.reason, p.report_time, u.st_id AS student_real_id, u.name FROM penalty p JOIN users u ON p.st_id = u.id WHERE p.reporter_type = 'ADMIN' ORDER BY p.report_time DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {
                Penalty p = new Penalty();
                // p.setNum(rs.getInt("num")); 
                p.setSeatIndex(rs.getInt("seat_index")); p.setStudentRealId(String.valueOf(rs.getInt("student_real_id"))); p.setStudentName(rs.getString("name")); p.setReason(rs.getString("reason"));
                if (rs.getTimestamp("report_time") != null) p.setReportTime(rs.getTimestamp("report_time").toLocalDateTime());
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    // [보조] 학번으로 users PK 찾기
    public int getUserPkByStudentId(int studentId) {
        String sql = "SELECT id FROM users WHERE st_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try(ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {}
        return 0;
    }
 // [새로운 메서드] 해당 좌석의 과거 사용 이력을 "모두" 가져오기
 // [추가] 특정 좌석의 '모든' 로그 기록을 가져오는 메서드
 // [AdminDAOimpl.java] 교체 또는 수정
 // [AdminDAOimpl.java] 이 메서드로 교체하세요.
 // [AdminDAOimpl.java] 무조건 조회 테스트용
    public List<User> getSeatHistory(int seatIndex) {
        List<User> historyList = new ArrayList<>();
        
        // 1. 최신 로그 200개 조회
        String sql = "SELECT * FROM times ORDER BY log_time DESC LIMIT 200"; 

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
                
            while (rs.next()) {
                String dbSeat = rs.getString("seat_log");
                String dbType = rs.getString("log_type");
                
                if (dbSeat == null) continue;
                
                // ★ [핵심 수정] 문자열("04")을 숫자(4)로 변환해서 비교
                // 이렇게 하면 "04"와 "4"를 같은 것으로 인식합니다.
                try {
                    int dbSeatInt = Integer.parseInt(dbSeat.trim()); // "04" -> 4
                    if (dbSeatInt == seatIndex) {
                        
                        // 입실(I) 기록 확인
                        if (dbType != null && dbType.trim().equalsIgnoreCase("I")) {
                            User u = new User();
                            int stId = rs.getInt("st_id");
                            String name = rs.getString("st_name"); 
                            
                            u.setStudentId(String.valueOf(stId));
                            u.setName(name != null ? name : "이름미상");
                            
                            Timestamp ts = rs.getTimestamp("log_time");
                            if(ts != null) {
                                String timeStr = ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
                                u.setName(u.getName() + " (" + timeStr + ")");
                            }
                            historyList.add(u);
                        }
                    }
                } catch (NumberFormatException e) {
                    // 좌석 번호가 숫자가 아닌 경우(예: "A1")는 그냥 문자열로 비교
                    if (dbSeat.trim().equals(String.valueOf(seatIndex))) {
                         // 위와 동일한 로직 (중복 줄이므로 생략 가능하나 안전을 위해 유지 가능)
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }
    public void runDBDiagnosis() {
        System.out.println("\n========== [🔥 DB 연결 정밀 진단 시작] ==========");
        
        try (Connection conn = DBConnection.getConnection()) {
            // 1. 자바가 연결된 DB 주소 확인 (가장 중요!)
            String dbUrl = conn.getMetaData().getURL();
            System.out.println("✅ 연결된 DB 주소: " + dbUrl);
            System.out.println("   (이 주소가 Workbench/DBeaver의 주소와 같은지 확인하세요!)");

            // 2. times 테이블 내용 몽땅 조회 (조건 없이)
            String sql = "SELECT * FROM times ORDER BY log_time DESC LIMIT 5";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            boolean isEmpty = true;
            while (rs.next()) {
                isEmpty = false;
                String seat = rs.getString("seat_log");
                String type = rs.getString("log_type");
                String time = rs.getString("log_time");
                // 좌석값에 공백이 있는지 대괄호[]로 감싸서 확인
                System.out.println("🔎 데이터 발견 -> 좌석:[" + seat + "] | 타입:[" + type + "] | 시간: " + time);
            }
            
            if (isEmpty) {
                System.out.println("💀 [충격] 자바가 보는 'times' 테이블은 텅 비어있습니다!");
                System.out.println("   -> COMMIT이 안 됐거나, 다른 DB(스키마)를 보고 있는 것입니다.");
            }

        } catch (Exception e) {
            System.out.println("🚨 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=================================================\n");
    }
}