// src/config/DBConnection.java (개선된 버전)
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Statement, PreparedStatement를 닫기 위해 추가

/**
 * 데이터베이스 연결을 관리하는 클래스.
 */
public class DBConnection {

    // 데이터베이스 연결 정보 (private static final 유지)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/taekang?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "yes050278??";

    // 생성자에서 JDBC 드라이버 로드
    public DBConnection() {
        try {
            // Class.forName은 한 번만 실행되도록 생성자에서 처리하거나 static 블록에서 처리
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC 드라이버를 찾을 수 없습니다. 빌드 경로를 확인하세요.");
            throw new RuntimeException("JDBC Driver not found", e); // 치명적 오류로 처리
        }
    }

    /**
     * 데이터베이스 커넥션을 가져옵니다. (인스턴스 메서드로 변경)
     * * @return Connection 객체
     * @throws SQLException 연결 실패 시 예외를 호출자에게 던짐
     */
    public Connection getConnection() throws SQLException {
        // 인스턴스 메서드에서는 try-catch 대신 throws를 사용하여 DAO에서 연결 실패를 처리하도록 유도
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * JDBC 자원 (Connection, Statement/PreparedStatement, ResultSet)을 안전하게 닫습니다.
     * DAO 패턴에서는 이 메서드를 finally 블록에서 호출합니다.
     */
    public void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            // Statement 타입은 PreparedStatement의 부모 타입이므로 모두 닫을 수 있습니다.
            if (stmt != null) stmt.close(); 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // PreparedStatement를 직접 받는 오버로드 메서드 (편의를 위해 추가)
    public void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        close(conn, (Statement) pstmt, rs); // Statement 오버로드 메서드를 호출하여 자원을 닫음
    }


    /**
     * 데이터베이스 연결을 테스트하기 위한 main 메소드. (유지)
     */
    public static void main(String[] args) {
        DBConnection db = new DBConnection();
        Connection conn = null;
        try {
            conn = db.getConnection();
            System.out.println("데이터베이스 연결 성공!");
        } catch (RuntimeException e) {
            // RuntimeException (드라이버 로드 실패) 처리
            System.out.println(e.getMessage());
        } catch (SQLException e) {
             System.out.println("데이터베이스 연결 실패.");
             e.printStackTrace();
        } finally {
            if (conn != null) {
                db.close(conn, (Statement)null, null); // 닫기 메서드 사용
                System.out.println("데이터베이스 연결 종료.");
            }
        }
    }
}