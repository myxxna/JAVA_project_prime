package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 데이터베이스 연결을 관리하는 클래스.
 */
public class DBConnection {

    // 데이터베이스 연결 정보
    // TODO: 이 정보들은 별도의 설정 파일로 분리하는 것이 좋습니다.
	private static final String DB_URL = "jdbc:mysql://10.0.19.232:3306/prime_db?serverTimezone=UTC";
	private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "rkdtpdnd12!@"; // 실제 비밀번호로 변경해야 합니다.

    /**
     * 데이터베이스 커넥션을 가져옵니다.
     * 
     * @return Connection 객체 또는 null (연결 실패 시)
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // JDBC 드라이버 로드. MySQL 8.0 이상에서는 com.mysql.cj.jdbc.Driver를 사용합니다.
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 데이터베이스 연결
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC 드라이버를 찾을 수 없습니다. 빌드 경로를 확인하세요.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("데이터베이스 연결에 실패했습니다. URL, 사용자 이름, 비밀번호를 확인하세요.");
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 데이터베이스 연결을 테스트하기 위한 main 메소드.
     */
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("데이터베이스 연결 성공!");
            try {
                conn.close();
                System.out.println("데이터베이스 연결 종료.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("데이터베이스 연결 실패.");
        }
    }
}
