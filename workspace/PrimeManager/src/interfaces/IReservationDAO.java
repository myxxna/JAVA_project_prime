package interfaces;

import model.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IReservationDAO {
    boolean createReservation(Reservation reservation);
    Reservation findActiveReservationByUserId(String userId);
    boolean updateStatusToInUse(String userId);
    boolean cancelReservation(String userId);
    Reservation findActiveReservationBySeatId(int seatId);

    boolean updateExpectedEndTime(long reservationId, LocalDateTime newEndTime, int minutesToAdd);

    boolean finishReservation(long reservationId, LocalDateTime actualEndTime);

    Map<Integer, Reservation> getAllActiveSeatReservations();
}