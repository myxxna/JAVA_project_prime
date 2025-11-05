package model;

import java.time.LocalDateTime;

// (★수정★) 새 DB 스키마에 맞춘 Seat 모델
public class Seat {

    private int id;
    private int floor;
    private String roomIndex; // (room_number -> room_index)
    private int seatIndex;
    private String seatNumber;
    private String status;
    private Integer currentUserId;
    private String currentUserName; // (current_user_name)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // (reserved 필드 삭제됨)

    // 기본 생성자
    public Seat() {}

    // --- Getter & Setter ---
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getFloor() {
        return floor;
    }
    public void setFloor(int floor) {
        this.floor = floor;
    }
    
    public String getRoomNumber() {
        return roomIndex;
    }
    public void setRoomNumber(String roomIndex) {
        this.roomIndex = roomIndex;
    }
    
    public int getSeatIndex() {
        return seatIndex;
    }
    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }
    public String getSeatNumber() {
        return seatNumber;
    }
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
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
    public String getCurrentUserName() {
        return currentUserName;
    }
    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
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
}