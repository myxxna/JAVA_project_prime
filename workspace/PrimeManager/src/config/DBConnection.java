package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DB_HOST = "34.47.107.140";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Abcd1234@";

    private static final String DB_NAME = "java-prime-db";

    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;


    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC 드라이버를 찾을 수 없습니다!");
            e.printStackTrace();
            throw new SQLException("JDBC Driver not found", e);
        }

        return DriverManager.getConnection(DB_URL + "?serverTimezone=UTC", DB_USER, DB_PASS);
    }

    public static void close(Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void close(Connection conn, java.sql.Statement stmt) {
        close(conn, stmt, null);
    }
}