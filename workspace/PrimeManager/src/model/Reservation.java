package model;

import java.time.LocalDateTime;

/**
 * 예약 정보를 나타내는 모델 클래스. 사용자와 좌석 간의 관계를 정의합니다.
 */
public class Reservation {
    private int reservationId; // 예약 ID
    private String userId; // 예약한 사용자의 ID
    private int seatId; // 예약된 좌석의 ID
    private LocalDateTime reservationTime; // 예약 시간

    public Reservation() {
    }

    public Reservation(int reservationId, String userId, int seatId, LocalDateTime reservationTime) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.seatId = seatId;
        this.reservationTime = reservationTime;
    }

    // Getters and Setters
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }
}