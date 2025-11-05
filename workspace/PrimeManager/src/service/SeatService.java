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

    // 좌석 입실 (★수정★)
    public boolean enterSeat(int seatId, int userId, int minutes) {
        Seat seat = seatDAO.getSeatById(seatId);
        // (★수정★) 'isReserved()' 삭제, 'G' -> 'Ｅ', 'R' -> 'Ｕ'
        if (seat != null && "Ｅ".equals(seat.getStatus())) {
            // (★수정★) 'setReserved(true)' 삭제
            seat.setStatus("Ｕ");
            seat.setCurrentUserId(userId);
            // (★신규★) DAO가 이 ID로 이름을 찾아 저장할 것을 기대
            // seat.setCurrentUserName(null); // (이름을 모르므로 null)
            seat.setStartTime(LocalDateTime.now());
            seat.setEndTime(LocalDateTime.now().plusMinutes(minutes));
            return seatDAO.updateSeat(seat);
        }
        return false;
    }

    // 좌석 퇴실 (★수정★)
    public boolean exitSeat(int seatId) {
        Seat seat = seatDAO.getSeatById(seatId);
        // (★수정★) 'isReserved()' 삭제, 'R' -> 'Ｕ', 'G' -> 'Ｅ'
        if (seat != null && "Ｕ".equals(seat.getStatus())) {
            // (★수정★) 'setReserved(false)' 삭제
            seat.setStatus("Ｅ");
            seat.setCurrentUserId(null); // (★수정★) 0 -> null
            seat.setCurrentUserName(null); // (★신규★)
            seat.setStartTime(null);
            seat.setEndTime(null);
            return seatDAO.updateSeat(seat);
        }
        return false;
    }

    // 좌석 예약 (★수정★)
    public boolean reserveSeat(int seatId, int userId, LocalDateTime start, LocalDateTime end) {
        Seat seat = seatDAO.getSeatById(seatId);
        // (★수정★) 'isReserved()' 삭제, 'G' -> 'Ｅ', 'Y' -> 'Ｒ'
        if (seat != null && "Ｅ".equals(seat.getStatus())) {
            // (★수정★) 'setReserved(true)' 삭제
            seat.setStatus("Ｒ");
            seat.setCurrentUserId(userId);
            // seat.setCurrentUserName(null); // (이름을 모르므로 null)
            seat.setStartTime(start);
            seat.setEndTime(end);
            return seatDAO.updateSeat(seat);
        }
        return false;
    }

    // 좌석 연장 (★수정★)
    public boolean extendSeat(int seatId, int additionalMinutes) {
        Seat seat = seatDAO.getSeatById(seatId);
        // (★수정★) 'R' -> 'Ｕ'
        if (seat != null && "Ｕ".equals(seat.getStatus()) && seat.getEndTime() != null) {
            seat.setEndTime(seat.getEndTime().plusMinutes(additionalMinutes));
            return seatDAO.updateSeat(seat);
        }
        return false;
    }
}