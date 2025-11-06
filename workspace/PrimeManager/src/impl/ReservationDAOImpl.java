package impl;

import interfaces.IReservationDAO;
import model.Reservation;
import model.Reservation.ReservationStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional; 
import java.util.concurrent.atomic.AtomicLong;

public class ReservationDAOImpl implements IReservationDAO {

    // 인메모리 데이터 저장소: seatId를 키로 활성 Reservation 객체를 저장합니다.
    public final Map<Integer, Reservation> seatReservationsMap = new HashMap<>(); 
    
    private final AtomicLong reservationIdCounter = new AtomicLong(1);

    @Override
    public boolean createReservation(Reservation reservation) {
        if (seatReservationsMap.containsKey(reservation.getSeatId())) {
            return false;
        }
        if (findActiveReservationByUserId(reservation.getUserId()) != null) {
            return false;
        }

        reservation.setReservationId(reservationIdCounter.getAndIncrement());
        reservation.setStatus(ReservationStatus.PENDING);
        seatReservationsMap.put(reservation.getSeatId(), reservation);
        return true;
    }

    @Override
    public Reservation findActiveReservationByUserId(String userId) {
        return seatReservationsMap.values().stream()
                .filter(r -> r.getUserId().equals(userId) && 
                             (r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.IN_USE))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public boolean updateStatusToInUse(String userId) {
        Reservation reservation = findActiveReservationByUserId(userId);
        
        if (reservation != null && reservation.getStatus() == ReservationStatus.PENDING) {
            reservation.setStatus(ReservationStatus.IN_USE);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean cancelReservation(String userId) { 
        Reservation reservation = findActiveReservationByUserId(userId);
        
        if (reservation != null && reservation.getStatus() == ReservationStatus.PENDING) {
            seatReservationsMap.remove(reservation.getSeatId());
            return true;
        }
        return false;
    } 
    
    @Override
    public Reservation findActiveReservationBySeatId(int seatId) {
        Reservation reservation = seatReservationsMap.get(seatId);
        
        if (reservation != null && 
            (reservation.getStatus() == ReservationStatus.PENDING || reservation.getStatus() == ReservationStatus.IN_USE)) {
            
            if (reservation.getExpectedEndTime().isBefore(LocalDateTime.now()) && reservation.getStatus() == ReservationStatus.IN_USE) {
                // 시간 만료 자동 퇴실
                finishReservation(reservation.getReservationId(), LocalDateTime.now());
                return null;
            }
            return reservation;
        }
        return null;
    }

    @Override
    public boolean updateExpectedEndTime(long reservationId, LocalDateTime newEndTime, int minutesToAdd) {
        Optional<Reservation> targetReservation = seatReservationsMap.values().stream()
            .filter(r -> r.getReservationId() == reservationId)
            .findFirst();

        if (targetReservation.isPresent()) {
            Reservation reservation = targetReservation.get();
            // 총 이용 시간 제한 로직 제거됨
            
            reservation.setExpectedEndTime(newEndTime);
            
            int currentDuration = reservation.getDurationMinutes();
            reservation.setDurationMinutes(currentDuration + minutesToAdd);
            
            return true;
        }
        return false;
    }

    @Override
    public boolean finishReservation(long reservationId, LocalDateTime actualEndTime) { 
        Optional<Reservation> finished = seatReservationsMap.values().stream()
                .filter(r -> r.getReservationId() == reservationId && r.getStatus() == ReservationStatus.IN_USE)
                .findFirst();

        if (finished.isPresent()) {
            Reservation r = finished.get();
            r.setStatus(ReservationStatus.FINISHED); 
            r.setActualEndTime(actualEndTime);
            seatReservationsMap.remove(r.getSeatId());
            return true;
        }
        return false;
    }
}