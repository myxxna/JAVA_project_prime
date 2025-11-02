package interfaces;

import model.Seat;
import java.util.List;

public interface ISeatDAO {

    /**
     * 모든 좌석 정보를 DB에서 조회
     * @return 좌석 목록
     */
    List<Seat> getAllSeats();

    /**
     * 특정 좌석 ID 조회
     * @param seatId 조회할 좌석 ID
     * @return Seat 객체, 없으면 null
     */
    Seat getSeatById(int seatId);

    /**
     * 좌석 예약 가능 여부 업데이트
     * @param seatId 좌석 ID
     * @param available 예약 가능 여부
     * @return 성공 시 true
     */
    boolean updateSeatAvailability(int seatId, boolean available);
}
