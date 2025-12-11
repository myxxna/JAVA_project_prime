package service;

import impl.SeatDAOImpl;
import model.Seat;
import java.time.LocalDateTime;
import java.util.List;
import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
public class SeatService {
    
    // DAO 인스턴스 생성
    private final SeatDAOImpl seatDAO = new SeatDAOImpl();
    
    /**
     * 모든 좌석 정보를 가져옵니다. (기존 코드 유지)
     */
    public List<Seat> getAllSeats() {
        return seatDAO.getAllSeats();
    }

    /**
     * ID로 특정 좌석 정보를 가져옵니다.
     * 용도: 버튼 클릭 시 해당 좌석의 현재 상태(이미 누가 앉았는지 등) 확인
     */
    public Seat getSeatById(int seatId) {
        return seatDAO.getSeatById(seatId);
    }

    /**
     * 특정 사용자(userId)가 이용 중인 좌석을 가져옵니다.
     * 용도: 중복 입실 방지, 내 좌석 퇴실 처리, '내 좌석' 표시
     */
    public Seat getSeatByUserId(int userId) {
        return seatDAO.getSeatByUserId(userId);
    }

    /**
     * 입실 처리 (Check-In)
     * @param seatId 입실할 좌석 ID
     * @param userId 사용자 ID
     * @param durationMinutes 이용 시간 (분)
     * @return 성공 여부
     */
    public boolean checkIn(int seatId, int userId, int durationMinutes) {
        // 1. 좌석 정보 가져오기
        Seat seat = seatDAO.getSeatById(seatId);
        
        // 2. 유효성 검사: 좌석이 없거나 이미 사용 중('U')이면 실패
        if (seat == null || "U".equals(seat.getStatus())) {
            return false;
        }

        // 3. 좌석 객체 정보 업데이트
        seat.setCurrentUserId(userId);     // 사용자 ID 설정
        seat.setStatus("U");               // 상태: 사용중(U)
        
        LocalDateTime now = LocalDateTime.now();
        seat.setStartTime(now);            // 시작 시간
        seat.setEndTime(now.plusMinutes(durationMinutes)); // 종료 시간

        // 4. DB 업데이트 실행 (DAO 호출)
        return seatDAO.updateSeatStatus(seat);
    }

    /**
     * 퇴실 처리 (Check-Out)
     * @param userId 퇴실할 사용자 ID
     * @return 성공 여부
     */
    public boolean checkOut(int userId) {
        // 1. 사용자가 이용 중인 좌석 찾기
        Seat seat = seatDAO.getSeatByUserId(userId);
        
        if (seat == null) {
            return false; // 이용 중인 좌석이 없음
        }

        // 2. 좌석 정보 초기화 (퇴실)
        seat.setCurrentUserId(null);       // 사용자 제거 (DB에 NULL로 들어감)
        seat.setCurrentUserName(null);     // 이름 제거
        seat.setStatus("Available");       // 상태: 사용가능으로 변경 (DB 약속에 따라 빈 문자열이나 "A" 등 수정 가능)
        seat.setStartTime(null);
        seat.setEndTime(null);

        // 3. DB 업데이트 실행
        return seatDAO.updateSeatStatus(seat);
    }
    /**
     * [추가] 좌석 이용 시간 연장
     */
    public boolean extendTime(int seatId, int addMinutes) {
        // 종료 시간(end_time)을 현재 설정된 시간에서 addMinutes만큼 더함
        String sql = "UPDATE seats SET end_time = DATE_ADD(end_time, INTERVAL ? MINUTE) WHERE seat_index = ?";

        try (Connection conn = config.DBConnection.getConnection(); // 또는 config.DBConnection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, addMinutes);
            pstmt.setInt(2, seatId);

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}