package model;
import java.time.LocalDateTime;

public class Seat {
    private int id;
    private String roomNumber;
    private String seatNumber;
    private boolean reserved;
    private String status; // G: 이용가능, R: 이용중, Y: 예약중
    private Integer currentUserId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 기본 생성자
    public Seat() {}

    // 전체 필드 생성자 (필요시)
    public Seat(int id, String roomNumber, String seatNumber, boolean reserved, String status,
                Integer currentUserId, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.seatNumber = seatNumber;
        this.reserved = reserved;
        this.status = status;
        this.currentUserId = currentUserId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // --- Getter & Setter ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Integer currentUserId) {
        this.currentUserId = currentUserId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    // --- 디버깅용 출력 ---
    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", roomNumber='" + roomNumber + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", reserved=" + reserved +
                ", status='" + status + '\'' +
                ", currentUserId=" + currentUserId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
