package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/prime_db";
	private static final String USER = "root"; // MySQL 계정
    private static final String PASSWORD = "1234"; // MySQL 비밀번호

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}


