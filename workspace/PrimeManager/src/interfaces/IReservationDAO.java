package interfaces;

import model.Reservation;
import java.time.LocalDateTime;
import java.util.List;

// ⚠️ DB 연결 전 로직 정의를 위한 인터페이스
public interface IReservationDAO {
    boolean createReservation(Reservation reservation);
    Reservation findActiveReservationByUserId(String userId);
    boolean updateStatusToInUse(String userId);
    boolean cancelReservation(String userId);
    Reservation findActiveReservationBySeatId(int seatId);
    boolean updateExpectedEndTime(long reservationId, LocalDateTime newEndTime, int newDurationMinutes);
    boolean finishReservation(long reservationId, LocalDateTime actualEndTime);
    
    // 이 외의 메서드는 필요에 따라 추가
}