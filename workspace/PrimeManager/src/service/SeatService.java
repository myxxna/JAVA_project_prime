
package service;

import impl.SeatDAOImpl;
import model.Seat;

import java.time.LocalDateTime;
import java.util.List;

public class SeatService {
    private SeatDAOImpl seatDAO = new SeatDAOImpl();

    // 전체 좌석 조회
    public List<Seat> getAllSeats() {
        return seatDAO.getAllSeats();
    }

    // 좌석 입실
    public boolean enterSeat(int seatId, int userId, int minutes) {
        Seat seat = seatDAO.getSeatById(seatId);
        if (seat != null && !seat.isReserved() && "G".equals(seat.getStatus())) {
            seat.setReserved(true);
            seat.setStatus("R");
            seat.setCurrentUserId(userId);
            seat.setStartTime(LocalDateTime.now());
            seat.setEndTime(LocalDateTime.now().plusMinutes(minutes));
            return seatDAO.updateSeat(seat);
        }
        return false;
    }

    // 좌석 퇴실
    public boolean exitSeat(int seatId) {
        Seat seat = seatDAO.getSeatById(seatId);
        if (seat != null && seat.isReserved() && "R".equals(seat.getStatus())) {
            seat.setReserved(false);
            seat.setStatus("G");
            seat.setCurrentUserId(0);
            seat.setStartTime(null);
            seat.setEndTime(null);
            return seatDAO.updateSeat(seat);
        }
        return false;
    }

    // 좌석 예약
    public boolean reserveSeat(int seatId, int userId, LocalDateTime start, LocalDateTime end) {
        Seat seat = seatDAO.getSeatById(seatId);
        if (seat != null && !seat.isReserved() && "G".equals(seat.getStatus())) {
            seat.setReserved(true);
            seat.setStatus("Y");
            seat.setCurrentUserId(userId);
            seat.setStartTime(start);
            seat.setEndTime(end);
            return seatDAO.updateSeat(seat);
        }
        return false;
    }

    // 좌석 연장
    public boolean extendSeat(int seatId, int additionalMinutes) {
        Seat seat = seatDAO.getSeatById(seatId);
        if (seat != null && "R".equals(seat.getStatus()) && seat.getEndTime() != null) {
            seat.setEndTime(seat.getEndTime().plusMinutes(additionalMinutes));
            return seatDAO.updateSeat(seat);
        }
        return false;
    }
}
