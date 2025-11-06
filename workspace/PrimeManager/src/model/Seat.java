package model;

import java.time.LocalDateTime;

public class Seat {
<<<<<<< HEAD
    // DB 칼럼 이름에 기반하여 필드를 정의합니다.
    private int id; // seat_id
    private int floor; 
    private String roomNumber; // room_index
    private int seatIndex;
    private String seatNumber; // seat_number
    private String status;
    private Integer currentUserId; // 🛑 DB의 NULL 값을 위해 Integer (Wrapper) 사용
    private String currentUserName; // JOIN으로 가져온 실제 사용자 이름
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // ----------------------------------------------------
    // 🛑 1. 기본 생성자 (AdminDAOimpl에서 'new Seat()' 호출 시 사용)
    // ----------------------------------------------------
    public Seat() {
        // 기본 초기화
=======

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
        this.col = col;
        this.status = "G";
>>>>>>> origin
    }
    
    // 🛑 2. Setter 정의 (AdminDAOimpl에서 데이터를 채울 때 사용)
    // ----------------------------------------------------
    
    // AdminDAOimpl.java 오류 해결: set...()
    public void setId(int id) { this.id = id; }
    public void setFloor(int floor) { this.floor = floor; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setSeatIndex(int seatIndex) { this.seatIndex = seatIndex; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public void setStatus(String status) { this.status = status; }

    // 🛑 DAO 오류 해결: NULL을 받기 위해 Integer를 사용해야 합니다.
    public void setCurrentUserId(Integer currentUserId) { this.currentUserId = currentUserId; } 
    public void setCurrentUserName(String currentUserName) { this.currentUserName = currentUserName; }

    // DAO 오류 해결: LocalDateTime을 받거나 null을 받습니다.
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }


    // ----------------------------------------------------
    // 🛑 3. Getter 정의 (AdminController에서 데이터를 읽을 때 사용)
    // ----------------------------------------------------

    // AdminController.java 오류 해결: get...()
    public int getId() { return id; }
<<<<<<< HEAD
    public String getSeatNumber() { return seatNumber; }
    public String getStatus() { return status; }
    public Integer getCurrentUserId() { return currentUserId; }
    public String getCurrentUserName() { return currentUserName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    // 🛑 [참고] DB에서 null일 때 setCurrentUserId(null)을 호출할 수 있도록
    // AdminDAOimpl.java의 rs.getInt("current_user_id") 로직도 rs.getObject("current_user_id")로 수정해야 합니다.
=======
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
>>>>>>> origin
}