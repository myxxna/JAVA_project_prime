package model;

import java.time.LocalDateTime;

public class Seat {
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
    public String getSeatNumber() { return seatNumber; }
    public String getStatus() { return status; }
    public Integer getCurrentUserId() { return currentUserId; }
    public String getCurrentUserName() { return currentUserName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    // 🛑 [참고] DB에서 null일 때 setCurrentUserId(null)을 호출할 수 있도록
    // AdminDAOimpl.java의 rs.getInt("current_user_id") 로직도 rs.getObject("current_user_id")로 수정해야 합니다.
}