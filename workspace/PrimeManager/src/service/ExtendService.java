package service;

import impl.SeatDAOImpl;
import model.Seat;
import java.time.LocalDateTime;

public class ExtendService {

    private final SeatDAOImpl seatDAO = new SeatDAOImpl();

    // 1. 연장하기
    public boolean extendSeat(int seatId, int additionalMinutes) {
        Seat seat = seatDAO.getSeatById(seatId);
        if (seat == null || seat.getEndTime() == null) return false;

        LocalDateTime newEndTime = seat.getEndTime().plusMinutes(additionalMinutes);
        seat.setEndTime(newEndTime);

        return seatDAO.updateSeatStatus(seat);
    }

    // 2. 퇴실하기 (추가됨)
    public boolean checkOut(int userId) {
        Seat seat = seatDAO.getSeatByUserId(userId);
        if (seat == null) return false;

        // 퇴실 시 정보 초기화
        seat.setCurrentUserId(null);
        seat.setCurrentUserName(null);
        seat.setStatus("Available"); // 빈 좌석 상태로 변경
        seat.setStartTime(null);
        seat.setEndTime(null);

        return seatDAO.updateSeatStatus(seat);
    }

    // 3. 내 좌석 조회
    public Seat getSeatByUserId(int userId) {
        return seatDAO.getSeatByUserId(userId);
    }
}