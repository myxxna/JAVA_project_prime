package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	
    
    public Connection getConnection() {
    	try {
    		String url = "jdbc:mysql://localhost:3306/test";
    	    String user = "root";
    	    String password = "1234";
    	    
			Connection conn = DriverManager.getConnection(url, user, password);
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    
}