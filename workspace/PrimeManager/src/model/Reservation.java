package model;

import java.time.LocalDateTime;

public class Reservation {
    
    public enum ReservationStatus {
        PENDING, IN_USE, FINISHED, CANCELED
    }
    
    private long reservationId;
    private String userId;
    private int seatId;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;
    private LocalDateTime actualEndTime; 
    private int durationMinutes;
    private int initialDurationMinutes;
    private ReservationStatus status;

    public Reservation(String userId, int seatId, LocalDateTime startTime, int durationMinutes) {
        this.userId = userId;
        this.seatId = seatId;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.initialDurationMinutes = durationMinutes; 
        this.expectedEndTime = startTime.plusMinutes(durationMinutes);
        this.status = ReservationStatus.PENDING; 
    }
    
    // Getter 및 Setter (생략된 부분 포함)

    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public String getUserId() { return userId; }
    public int getSeatId() { return seatId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(LocalDateTime expectedEndTime) { this.expectedEndTime = expectedEndTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getInitialDurationMinutes() { return initialDurationMinutes; }
    public void setInitialDurationMinutes(int initialDurationMinutes) { this.initialDurationMinutes = initialDurationMinutes; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }
}