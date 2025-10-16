package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.Seat;

/**
 * seats 테이블과 관련된 데이터베이스 작업을 처리하는 클래스.
 */
public class SeatDAO {

    /**
     * 좌석 정보와 해당 좌석을 예약한 사용자 정보를 함께 담는 DTO 클래스.
     * static nested class로 선언하여 SeatDAO와 강하게 연관됨을 나타냅니다.
     */
    public static class SeatWithUserDTO {
        // Seat 정보
        private int seatId;
        private String roomNumber;
        private String seatNumber;
        private boolean isAvailable;

        // User 정보 (예약된 경우)
        private String userId;
        private String username;
        private String studentId;
        private String department;

        // Getters and Setters
        public int getSeatId() { return seatId; }
        public void setSeatId(int seatId) { this.seatId = seatId; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        @Override
        public String toString() {
            if (userId != null) {
                return String.format("좌석[ID=%d, 번호=%s, 사용가능=%b] - 사용자[ID=%s, 이름=%s, 학번=%s, 학과=%s]",
                        seatId, seatNumber, isAvailable, userId, username, studentId, department);
            } else {
                return String.format("좌석[ID=%d, 번호=%s, 사용가능=%b] - (사용자 없음)", seatId, seatNumber, isAvailable);
            }
        }
    }

    /**
     * 모든 좌석 정보를 사용자 정보와 함께 조회합니다.
     * 좌석이 예약된 경우, 해당 사용자의 정보(학번, 이름, 학과)를 포함합니다.
     * @return 사용자 정보가 포함된 좌석 목록 (List<SeatWithUserDTO>)
     */
    public List<SeatWithUserDTO> findAllSeatsWithUser() {
        List<SeatWithUserDTO> seatsWithUsers = new ArrayList<>();
        // seats 테이블을 기준으로 reservations와 users 테이블을 LEFT JOIN 합니다.
        // 이렇게 하면 예약되지 않은 좌석 정보도 모두 가져올 수 있습니다.
        String sql = "SELECT s.seat_id, s.room_number, s.seat_number, s.is_available, " +
                     "u.user_id, u.username, u.student_id, u.department " +
                     "FROM seats s " +
                     "LEFT JOIN reservations r ON s.seat_id = r.seat_id " +
                     "LEFT JOIN users u ON r.user_id = u.user_id " +
                     "ORDER BY s.seat_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                SeatWithUserDTO dto = new SeatWithUserDTO();
                dto.setSeatId(rs.getInt("seat_id"));
                dto.setRoomNumber(rs.getString("room_number"));
                dto.setSeatNumber(rs.getString("seat_number"));
                dto.setAvailable(rs.getBoolean("is_available"));
                
                // LEFT JOIN의 결과로 사용자 정보가 NULL일 수 있습니다.
                dto.setUserId(rs.getString("user_id"));
                dto.setUsername(rs.getString("username"));
                dto.setStudentId(rs.getString("student_id"));
                dto.setDepartment(rs.getString("department"));
                
                seatsWithUsers.add(dto);
            }

        } catch (SQLException e) {
            System.err.println("사용자 정보를 포함한 좌석 정보 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("알 수 없는 오류가 발생했습니다.");
            e.printStackTrace();
        }

        return seatsWithUsers;
    }


    /**
     * 데이터베이스에서 모든 좌석 정보를 조회합니다.
     * 
     * @return 좌석 정보가 담긴 List<Seat> 객체. 조회 실패 시 비어있는 리스트를 반환합니다.
     */
    public List<Seat> findAllSeats() {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats ORDER BY seat_id ASC"; // 'seat_id'로 컬럼 이름 수정

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Seat seat = new Seat();
                // 실제 DB 컬럼 이름으로 수정
                seat.setSeatId(rs.getInt("seat_id"));
                seat.setRoomNumber(rs.getString("room_number"));
                seat.setSeatNumber(rs.getString("seat_number"));
                seat.setAvailable(rs.getBoolean("is_available"));
                seats.add(seat);
            }

        } catch (SQLException e) {
            System.err.println("모든 좌석 정보 조회 중 오류가 발생했습니다.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("알 수 없는 오류가 발생했습니다.");
            e.printStackTrace();
        }

        return seats;
    }

    /**
     * SeatDAO의 기능을 테스트하기 위한 main 메소드.
     */
    public static void main(String[] args) {
        SeatDAO seatDAO = new SeatDAO();
        List<SeatWithUserDTO> seats = seatDAO.findAllSeatsWithUser();

        if (seats.isEmpty()) {
            System.out.println("조회된 좌석 정보가 없습니다. DB 연결 정보나 테이블 데이터를 확인해주세요.");
        } else {
            System.out.println("--- 모든 좌석 및 사용자 정보 ---");
            for (SeatWithUserDTO seat : seats) {
                System.out.println(seat); // SeatWithUserDTO.java의 toString() 메소드가 호출됩니다.
            }
            System.out.println("---------------------");
        }
    }
}