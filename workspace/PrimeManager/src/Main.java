import model.Seat;
import interfaces.ISeatDAO;
import impl.SeatDAOImpl;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("데이터베이스에서 좌석 정보를 조회합니다...");

        // ISeatDAO 구현체 생성
        ISeatDAO seatDAO = new SeatDAOImpl();

        // DB에서 모든 좌석 조회
        List<Seat> seats = seatDAO.getAllSeats();

        if (seats.isEmpty()) {
            System.out.println("조회된 좌석 정보가 없습니다. 'Seats' 테이블에 데이터가 있는지 확인해주세요.");
        } else {
            System.out.println("--- 좌석 목록 ---");
            for (Seat seat : seats) {
                // Seat.java의 toString() 호출
                System.out.println(seat);
            }
            System.out.println("-----------------");
        }
    }
}

