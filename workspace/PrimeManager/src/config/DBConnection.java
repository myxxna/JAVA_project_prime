package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static final String URL = "jdbc:mysql://34.47.107.140:3306/java-prime-db";
	private static final String USER = "root"; // MySQL 계정
    private static final String PASSWORD = "Abcd1234@"; // MySQL 비밀번호

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}