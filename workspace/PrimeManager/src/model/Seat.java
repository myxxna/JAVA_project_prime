package model;

import java.time.LocalDateTime;

public class Seat {

    private int id;                     // DB column: id (INT)
    private int currentUserId;      // DB column: current_user_id (INT) - NULL 가능하므로 Integer 사용
    private String currentUserName;     // DB column: current_user_name (VARCHAR)
    private int floor;                  // DB column: floor (INT)
    private String roomIndex;           // DB column: room_index (VARCHAR) - 기존 roomNumber 대체 가능성 큼
    private int seatIndex;              // DB column: seat_index (INT)
    private String status;              // DB column: status (VARCHAR)
    private LocalDateTime startTime;    // DB column: start_time (DATETIME)
    private LocalDateTime endTime;      // DB column: end_time (DATETIME)
    private String seatNumber;          // DB column: seat_number (VARCHAR)

    // 기본 생성자
    public Seat() {
    }

    // 필수 정보 기반 생성자
    public Seat(int id, int floor, String roomIndex, int seatIndex, String seatNumber) {
        this.id = id;
        this.floor = floor;
        this.roomIndex = roomIndex;
        this.seatIndex = seatIndex;
        this.seatNumber = seatNumber;
        this.status = "Available"; // 기본 상태 설정 (예시)
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCurrentUserId() { return currentUserId; }
    public void setCurrentUserId(int currentUserId) { this.currentUserId = currentUserId; }

    public String getCurrentUserName() { return currentUserName; }
    public void setCurrentUserName(String currentUserName) { this.currentUserName = currentUserName; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getRoomNumber() { return roomIndex; }
    public void setRoomNumber(String roomIndex) { this.roomIndex = roomIndex; }

    public int getSeatIndex() { return seatIndex; }
    public void setSeatIndex(int seatIndex) { this.seatIndex = seatIndex; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
}