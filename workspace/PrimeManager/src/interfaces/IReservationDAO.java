package interfaces;

import model.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IReservationDAO {
    
    // 예약 생성 (createReservation으로 통일)
    Optional<Integer> createReservation(Reservation reservation);

    // 조회 메서드들
    Optional<Reservation> findActiveReservationBySeatId(int seatId);
    Optional<Reservation> findActiveReservationByUserId(int userId);
    
    // 특정 좌석의 미래 예약 조회 (예약 차단용)
    List<Reservation> getFutureReservationsBySeatId(int seatId);

    // 상태 변경
    void updateStatusToInUse(int reservationId);
    void cancelReservation(int reservationId);
    void finishReservation(int reservationId, LocalDateTime actualEndTime); 

    // 전체 조회
    List<Reservation> getAllActiveReservations(); 
}