package service;

import impl.ReservationDAOImpl;
import interfaces.IReservationDAO;
import model.Reservation;
import java.time.LocalDateTime;

public class ReservationService {

    private final IReservationDAO reservationDAO = new ReservationDAOImpl();

    public boolean reserveSeat(String userId, int seatId, int durationMinutes) {
        Reservation newReservation = new Reservation(userId, seatId, LocalDateTime.now(), durationMinutes);
        return reservationDAO.createReservation(newReservation);
    }

    public boolean checkIn(String userId) {
        return reservationDAO.updateStatusToInUse(userId);
    }

    public boolean cancelReservation(String userId) {
        return reservationDAO.cancelReservation(userId);
    }

    public boolean checkOut(long reservationId) {
        return reservationDAO.finishReservation(reservationId, LocalDateTime.now());
    }

    public int extendReservation(long reservationId, int minutes) {

        boolean success = reservationDAO.updateExpectedEndTime(reservationId, null, minutes);

        if (success) return 1;

        return -1;
    }

    public Reservation findActiveReservationByUserId(String userId) {
        return reservationDAO.findActiveReservationByUserId(userId);
    }

    public Reservation getActiveReservationBySeatId(int seatId) {
        return reservationDAO.findActiveReservationBySeatId(seatId);
    }

}