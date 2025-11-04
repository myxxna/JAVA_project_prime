// src/impl/UserDAOImpl.java
package impl;

import config.DBConnection;
import interfaces.IUserDAO; // 💡 IUserDAO cannot be a superinterface 오류 해결
import model.User;
import java.sql.Connection;        // Connection cannot be resolved to a type 해결
import java.sql.PreparedStatement; // PreparedStatement cannot be resolved to a type 해결
import java.sql.ResultSet;         // ResultSet cannot be resolved to a type 해결
import java.sql.SQLException;
// ... (나머지 import)

public class UserDAOImpl implements IUserDAO {

    private DBConnection dbConnection;

    public UserDAOImpl() {
        this.dbConnection = new DBConnection();
    }

    @Override
    public User findUserById(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        // 🛑 [수정] SQL에서 존재하지 않는 studentId, department 칼럼을 제거해야 합니다.
        String sql = "SELECT user_id, name, password, isAdmin FROM users WHERE user_id = ?"; 

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                    rs.getString("user_id"), // User ID
                    rs.getString("name"),      // name (DB) -> User Name
                    rs.getString("password"),
                    rs.getBoolean("isAdmin"),
                    // 🛑 [수정] DB에서 조회되지 않는 필드는 모두 null을 명시적으로 전달합니다.
                    null, // studentId (DB에 없음)
                    null  // department (DB에 없음)
                );
            }
        } catch (SQLException e) {
            System.err.println("사용자 조회 중 오류 발생: " + e.getMessage());
            // 🛑 오류 발생 시 null 대신 Exception을 던지는 것이 더 좋습니다.
        } finally {
            dbConnection.close(conn, pstmt, rs); 
        }
        return user;
    }
    
    // findUserByIdAndPwd 메서드 (SQL 쿼리 문제 없음)
    public User findUserByIdAndPwd(String userId, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        String sql = "SELECT user_id, password, name, isAdmin FROM users WHERE user_id = ? AND password = ?"; 

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getBoolean("isAdmin"),
                    rs.getString("user_id"), // studentId 역할로 member_id 재사용 가정
                    null 
                );
            }
        } catch (SQLException e) {
            System.err.println("로그인 인증 중 오류 발생: " + e.getMessage());
        } finally {
            dbConnection.close(conn, pstmt, rs); 
        }
        return user;
    }
}