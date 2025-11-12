package model;

import java.time.LocalDateTime;

public class Seat {

    private int id;
    private String number;

    private int row;
    private int col;

    private int floor;
    private String roomNumber;
    private int seatIndex;
    private String status;
    private Integer currentUserId;
    private String currentUserName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Seat() {
    }

    public Seat(int id, String number, int row, int col) {
        this.id = id;
        this.number = number;
        this.row = row;
        this.floor = floor;
        
        this.col = col;
        this.status = "G";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumber() { return number; }
    public String getSeatNumber() { return number; }
    public void setSeatNumber(String number) { this.number = number; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getSeatIndex() { return seatIndex; }
    public void setSeatIndex(int seatIndex) { this.seatIndex = seatIndex; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCurrentUserId() { return currentUserId; }
    public void setCurrentUserId(Integer currentUserId) { this.currentUserId = currentUserId; }

    public String getCurrentUserName() { return currentUserName; }
    public void setCurrentUserName(String currentUserName) { this.currentUserName = currentUserName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}