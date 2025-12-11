package model;

import java.time.LocalDateTime;

public class Reservation {

    private int id;
    private int userId;
    private int seatId;
    private LocalDateTime reservationTime;
    private String status;
    private LocalDateTime createdAt;
    
    // ★ 추가된 필드: 에러 해결을 위해 추가 (DB에는 저장되지 않더라도 로직상 필요)
    private int durationMinutes; 

    public Reservation() {
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { this.reservationTime = reservationTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ★ 추가된 메서드 (이게 없어서 빨간줄 에러가 났던 것입니다)
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}