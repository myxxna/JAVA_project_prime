package config;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDBConnection {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ DB 연결 성공: " + conn.getMetaData().getURL());
            } else {
                System.out.println("❌ DB 연결 실패: Connection이 null 또는 닫혀 있습니다.");
            }
        } catch (SQLException e) {
            System.out.println("❌ DB 연결 중 오류 발생:");
            e.printStackTrace();
        }
    }
}
