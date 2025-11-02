import impl.UserDAOImpl;
import model.User;
import service.SeatService;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        UserDAOImpl userDAO = new UserDAOImpl();
        SeatService seatService = new SeatService();

        // 1. 로그인
        User user = userDAO.login("20250001"); // 학번으로 로그인
        if (user == null) {
            System.out.println("로그인 실패");
            return;
        }
        System.out.println("로그인 성공: " + user.getName());

        // 2. 좌석 입실 (2시간)
        boolean entered = seatService.enterSeat(1, user.getId(), 120);
        System.out.println(entered ? "입실 완료" : "입실 실패");

        // 3. 좌석 연장 (30분)
        boolean extended = seatService.extendSeat(1, 30);
        System.out.println(extended ? "연장 완료" : "연장 실패");

        // 4. 좌석 퇴실
        boolean exited = seatService.exitSeat(1);
        System.out.println(exited ? "퇴실 완료" : "퇴실 실패");

        // 5. 예약
        LocalDateTime start = LocalDateTime.of(2025, 11, 2, 14, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 2, 15, 30);
        boolean reserved = seatService.reserveSeat(2, user.getId(), start, end);
        System.out.println(reserved ? "예약 완료" : "예약 실패");
    }
}
