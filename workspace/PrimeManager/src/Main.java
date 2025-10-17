import java.util.List;
import dao.SeatDAO;
import model.Seat;

public class Main {

    public static void main(String[] args) {
        System.out.println("데이터베이스에서 좌석 정보를 조회합니다...");
        
        SeatDAO seatDAO = new SeatDAO();
        // 'seats' 테이블만 조회하는 메소드로 변경합니다.
        List<Seat> seats = seatDAO.findAllSeats();

        if (seats.isEmpty()) {
            System.out.println("조회된 좌석 정보가 없습니다. 'seats' 테이블에 데이터가 있는지 확인해주세요.");
        } else {
            System.out.println("--- 좌석 목록 ---");
            for (Seat seat : seats) {
                // Seat.java의 toString() 메소드가 호출됩니다.
                System.out.println(seat);
            }
            System.out.println("-----------------");
        }
    }
}
