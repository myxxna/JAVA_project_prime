import impl.UserDAOImpl;
import model.User;
import service.SeatService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import config.DBConnection;

public class Main {
    public static void main(String[] args) {
    	
    	System.out.println("데이터베이스 연결 테스트 시작...");

        // 2. try-with-resources 구문을 사용하여 연결을 시도하고 자동으로 닫히게 합니다.
        try (Connection connection = DBConnection.getConnection()) {
            
            // 3. 연결 객체가 null이 아니고, 닫혀있지 않다면 성공
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ 데이터베이스 연결 성공!");
                System.out.println("연결된 URL: " + connection.getMetaData().getURL());
            } else {
                System.out.println("⚠️ 연결에 실패했으나 예외(Exception)는 발생하지 않았습니다.");
            }

        } catch (SQLException e) {
            // 4. SQLException이 발생하면 연결 실패
            System.err.println("❌ 데이터베이스 연결 실패!");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            
            // 5. GCP에서 가장 흔한 오류 힌트 제공
            if (e.getMessage().contains("Communications link failure") || e.getMessage().contains("timed out")) {
                System.err.println("\n[힌트] 'Communications link failure' 오류는 GCP 방화벽 문제일 가능성이 높습니다.");
                System.err.println("GCP Cloud SQL의 [연결] > [승인된 네트워크]에 현재 PC의 IP 주소가 등록되었는지 확인하세요.");
            }
            if (e.getMessage().contains("Access denied")) {
                 System.err.println("\n[힌트] 'Access denied' 오류는 사용자 이름 또는 비밀번호가 틀렸다는 의미입니다.");
            }

        } catch (Exception e) {
            // 6. 기타 예외 (예: 드라이버 클래스를 못 찾는 경우)
            System.err.println("❌ 알 수 없는 오류 발생!");
            e.printStackTrace();
        }
    
//    	
//    	
//        UserDAOImpl userDAO = new UserDAOImpl();
//        SeatService seatService = new SeatService();
//
//        // 1. 로그인
//        User user = userDAO.login("20250001"); // 학번으로 로그인
//        if (user == null) {
//            System.out.println("로그인 실패");
//            return;
//        }
//        System.out.println("로그인 성공: " + user.getName());
//
//        // 2. 좌석 입실 (2시간)
//        boolean entered = seatService.enterSeat(1, user.getId(), 120);
//        System.out.println(entered ? "입실 완료" : "입실 실패");
//
//        // 3. 좌석 연장 (30분)
//        boolean extended = seatService.extendSeat(1, 30);
//        System.out.println(extended ? "연장 완료" : "연장 실패");
//
//        // 4. 좌석 퇴실
//        boolean exited = seatService.exitSeat(1);
//        System.out.println(exited ? "퇴실 완료" : "퇴실 실패");
//
//        // 5. 예약
//        LocalDateTime start = LocalDateTime.of(2025, 11, 2, 14, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 11, 2, 15, 30);
//        boolean reserved = seatService.reserveSeat(2, user.getId(), start, end);
//        System.out.println(reserved ? "예약 완료" : "예약 실패");
    }
}
