package model;

import java.time.LocalDateTime;

// ★ Admin과 Kiosk 기능을 통합한 Seat 모델 ★
public class Seat {

    // --- 공통 필드 ---
    private int id;
    private String number; // Kiosk(number), Admin(seat_number) 공통 사용

    // --- Kiosk용 필드 (Grid) ---
    private int row;
    private int col;

    // --- Admin용 필드 (DB) ---
    private int floor;
    private String roomNumber;  // (AdminDAOImpl은 room_index를 사용)
    private int seatIndex;
    private String status;
    private Integer currentUserId; // ★ null을 허용하기 위해 Integer 타입으로 변경
    private String currentUserName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // --- 생성자 ---

    /**
     * AdminDAOImpl에서 DB로부터 좌석 정보를 매핑할 때 사용
     * (new Seat())
     */
    public Seat() {
    }

    /**
     * Kiosk의 SeatDAOImpl에서 더미 데이터를 생성할 때 사용
     * (new Seat(1, "A1", 0, 0))
     */
    public Seat(int id, String number, int row, int col) {
        this.id = id;
        this.number = number;
        this.row = row;
        this.col = col;
        this.status = "G"; // Kiosk용 좌석도 기본 상태를 'G'로 설정
    }

    // --- Getter 및 Setter ---

    // ID (공통)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Seat Number (공통)
    public String getNumber() { return number; } // Kiosk (SeatController)
    public String getSeatNumber() { return number; } // Admin (AdminController)
    public void setSeatNumber(String number) { this.number = number; } // Admin (AdminDAOImpl)

    // Kiosk 필드
    public int getRow() { return row; }
    public int getCol() { return col; }

    // Admin 필드 (AdminDAOImpl + AdminController에서 필요)

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