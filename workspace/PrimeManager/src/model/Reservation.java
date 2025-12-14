package model;

import java.time.LocalDateTime;

public class Reservation {

    private int id;
    private int userId;
    private int seatId;
    private LocalDateTime reservationTime; // 예약 시각
    private String status;                 // 상태 (R:예약, I:입실 등)
    private LocalDateTime createdAt;       // 입실 예정 시간
    private int usingTime;                 // 이용 시간 (시간 단위)
    
    // [추가] 비즈니스 로직용 필드 (DB 컬럼 아님, 계산용)
    private LocalDateTime expectedEndTime; 

    public Reservation() {
    }

    // Getters & Setters
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

    public int getUsingTime() { return usingTime; }
    public void setUsingTime(int usingTime) { this.usingTime = usingTime; }

    // [추가됨] 오류 해결용 Getter/Setter
    public LocalDateTime getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(LocalDateTime expectedEndTime) { this.expectedEndTime = expectedEndTime; }
}