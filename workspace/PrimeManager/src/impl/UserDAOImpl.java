package impl;

import config.DBConnection;
import interfaces.IUserDAO;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements IUserDAO {

    @Override
    public User login(String studentId) {
        String sql = "SELECT * FROM users WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getInt("penalty_Count"),
                            rs.getString("student_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
   


    // --- 2. findUserByIdAndPwd(String, String) 구현 (이전 오류: The method findUserByIdAndPwd(String, String) is undefined) ---

    @Override
    public User findUserByIdAndPwd(String studentId, String password) {
        // ID와 비밀번호를 모두 사용하여 사용자를 찾는 쿼리
        // 주의: 실제 환경에서는 비밀번호를 해시(Hash)하여 비교해야 합니다.
        String sql = "SELECT * FROM users WHERE student_id = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, password); // 비밀번호 설정 (DB 테이블에 password 컬럼이 있다고 가정)

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getInt("penalty_Count"),
                            rs.getString("student_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("password")
                            
                            // User model에 password 필드가 없다면 여기서 조회할 필요는 없습니다.
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID and Password.");
            e.printStackTrace();
        }
        return null;
    }
}


