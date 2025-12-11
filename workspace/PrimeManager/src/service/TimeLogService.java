package service;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TimeLogService {

    public void insertTimeLog(int stId, String stName, String type, String seatNum) {
        
        String sql = "INSERT INTO times (st_id, st_name, log_time, log_type, seat_log) VALUES (?, ?, NOW(), ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stId);       
            pstmt.setString(2, stName);  
            // ★ 여기서 파라미터로 받은 type (I 또는 E)을 그대로 저장합니다.
            pstmt.setString(3, type);    
            pstmt.setString(4, seatNum); 

            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("✅ [DB 저장 성공] 타입: " + type + ", 좌석: " + seatNum);
            }

        } catch (SQLException e) {
            System.err.println("❌ [DB 에러] times 저장 실패!");
            e.printStackTrace();
        }
    }
}