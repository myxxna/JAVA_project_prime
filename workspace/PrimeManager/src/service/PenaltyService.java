package service;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
public class PenaltyService {

    // 1. 해당 좌석(seatIndex)에 누가 앉아있는지 확인 (reservations 또는 seats 테이블 조회)
    // 주의: 실제 사용 중인 테이블(seats) 구조에 맞춰 쿼리를 확인하세요.
    public int getStudentIdBySeat(int seatIndex) {
        // 예시: seats 테이블에 st_id 컬럼이 있고, 빈자리가 아니라고 가정
        String sql = "SELECT st_id FROM seats WHERE seat_index = ?"; 
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatIndex);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("st_id"); // 앉아있는 사람의 학번 리턴
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0; // 0이면 빈자리거나 오류
    }

    // 2. 신고 정보 저장 (image_473eb8.png 스키마 반영)
public String insertPenalty(int reporterId, String reason, int seatIndex) {
        
        // 1. 쿼리문: reporter_type은 'USER'로 문자열 그대로 넣습니다 (ENUM 처리됨)
        String sql = "INSERT INTO penalty (st_id, reason, report_time, seat_index, reporter_type) VALUES (?, ?, NOW(), ?, 'USER')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 2. [중요] DB가 Not Null이므로 setInt로 값을 강제로 넣습니다.
            // 만약 reporterId가 0이면 외래키 오류가 날 수 있지만, 일단 시도합니다.
            pstmt.setInt(1, reporterId);
            
            pstmt.setString(2, reason);    // 신고 사유
            pstmt.setInt(3, seatIndex);    // 좌석 번호

            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                return "SUCCESS";
            } else {
                return "DB 저장 실패 (영향받은 행 없음)";
            }

        } catch (SQLException e) {
            e.printStackTrace(); // 콘솔에 에러 출력
            // ★ 에러가 나면 이 메시지가 팝업창에 뜹니다
            return "DB 에러: " + e.getMessage(); 
        }
    }
    
    
}
