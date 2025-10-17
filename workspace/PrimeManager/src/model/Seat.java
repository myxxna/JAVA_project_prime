package model;

/**
 * 좌석 정보를 나타내는 모델 클래스.
 */
public class Seat {
    private int seatId; // 좌석 ID
    private String roomNumber; // 방 번호
    private String seatNumber; // 좌석 번호 (e.g., "A1")
    private boolean isAvailable; // 예약 가능 여부

    public Seat() {
    }

    public Seat(int seatId, String roomNumber, String seatNumber, boolean isAvailable) {
        this.seatId = seatId;
        this.roomNumber = roomNumber;
        this.seatNumber = seatNumber;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", roomNumber='" + roomNumber + "'" +
                ", seatNumber='" + seatNumber + "'" +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
