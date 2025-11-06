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
        String sql = "SELECT * FROM users WHERE st_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(studentId));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // (★수정★) email(null)이 빠진 6개 인자 생성자 호출
                    return new User(
                            rs.getInt("id"),

                            rs.getInt("penalty_count"),
                            String.valueOf(rs.getInt("st_id")),
                            rs.getString("name"),
                            // null,  <-- email 파라미터 삭제

                            rs.getString("role"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("잘못된 학번 형식입니다: " + studentId);
        }
        return null;
    }
    

    


    // --- 2. findUserByIdAndPwd(String, String) 구현 ---

    @Override
    public User findUserByIdAndPwd(String studentId, String password) {
        String sql = "SELECT * FROM users WHERE st_id = ? AND password = ?";

        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setInt(1, Integer.parseInt(studentId));
            pstmt.setString(2, password); 

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // (★수정★) email(null)이 빠진 6개 인자 생성자 호출
                    return new User(
                            rs.getInt("id"),
                            rs.getInt("penalty_count"),
                            String.valueOf(rs.getInt("st_id")),
                            rs.getString("name"),
                            // null, <-- email 파라미터 삭제
                            rs.getString("role"),
                            rs.getString("password")

                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID and Password.");
            e.printStackTrace();

        } catch (NumberFormatException e) {
            System.err.println("잘못된 학번 형식입니다: " + studentId);
        }
        return null;
}
}

