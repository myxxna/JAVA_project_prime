package service;

import impl.ReservationDAOImpl;
import model.Reservation;
import java.time.LocalDateTime;

public class ReservationService {

    private final ReservationDAOImpl reservationDAO = new ReservationDAOImpl();

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

    // @return 1: 성공, -1: 오류
    public int extendReservation(long reservationId, int minutes) {
        Reservation reservation = findActiveReservationByReservationId(reservationId);
        if (reservation == null) return -1; 

        LocalDateTime newExpectedEndTime = reservation.getExpectedEndTime().plusMinutes(minutes);
        
        boolean success = reservationDAO.updateExpectedEndTime(reservationId, newExpectedEndTime, minutes);
        
        if (success) return 1;
        
        return -1; // 일반적인 업데이트 실패
    }
    
    public Reservation findActiveReservationByUserId(String userId) {
        return reservationDAO.findActiveReservationByUserId(userId);
    }
    
    public Reservation getActiveReservationBySeatId(int seatId) {
        return reservationDAO.findActiveReservationBySeatId(seatId);
    }

    private Reservation findActiveReservationByReservationId(long reservationId) {
        return reservationDAO.seatReservationsMap.values().stream()
                .filter(r -> r.getReservationId() == reservationId)
                .findFirst()
                .orElse(null);
    }
}