package service;

import impl.ReservationDAOImpl;
import interfaces.IReservationDAO;
import model.Reservation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    
    // 인터페이스 타입 사용 권장
    private final IReservationDAO reservationDAO = new ReservationDAOImpl();

    public boolean makeReservation(int userId, int seatId, LocalDateTime startTime, int durationHours) {
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setReservationTime(LocalDateTime.now()); 
        reservation.setCreatedAt(startTime);                 
        reservation.setUsingTime(durationHours);             
        reservation.setStatus("R"); 

        return reservationDAO.createReservation(reservation).isPresent();
    }

    // [추가됨] Controller에서 호출하는 메서드
    public List<Integer> getReservedHours(int seatId) {
        List<Reservation> reservations = reservationDAO.getFutureReservationsBySeatId(seatId);
        List<Integer> blockedHours = new ArrayList<>();

        for (Reservation res : reservations) {
            if (res.getCreatedAt() != null) {
                int startHour = res.getCreatedAt().getHour();
                int duration = res.getUsingTime();
                for (int i = 0; i < duration; i++) {
                    blockedHours.add(startHour + i);
                }
            }
        }
        return blockedHours;
    }
}