package impl;

import config.DBConnection; // 기존 코드의 DBConnection 사용
import interfaces.IUserDAO;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAOImpl implements IUserDAO {

    // --- 1. 기존 로그인 (주석 처리된 부분) ---
    //    @Override
    //    public User login(String studentId) {
    //        String sql = "SELECT * FROM users WHERE st_id=?";
    //        try (Connection conn = DBConnection.getConnection();
    //             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    //
    //            pstmt.setInt(1, Integer.parseInt(studentId));
    //            
    //            try (ResultSet rs = pstmt.executeQuery()) {
    //                if (rs.next()) {
    //                    // (★수정★) email(null)이 빠진 6개 인자 생성자 호출
    //                    return new User(
    //                            rs.getInt("id"),
    //                            rs.getInt("penalty_count"),
    //                            String.valueOf(rs.getInt("st_id")),
    //                            rs.getString("name"),
    //                            // null,  <-- email 파라미터 삭제
    //                            rs.getString("role"),
    //                            rs.getString("password")
    //                    );
    //                }
    //            }
    //        } catch (SQLException e) {
    //            e.printStackTrace();
    //        } catch (NumberFormatException e) {
    //            System.err.println("잘못된 학번 형식입니다: " + studentId);
    //        }
    //        return null;
    //    }
      
	private User mapResultSetToUser(ResultSet rs) throws SQLException {
        // Model의 Setter를 사용하여 객체 생성 (이전 답변에서 생성자 대신 사용 가정)
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setStudentId(rs.getString("st_id")); // DB: st_id (INT)를 Model: String으로 변환
        user.setName(rs.getString("name"));
        user.setPenaltyCount(rs.getInt("penalty_count"));
        user.setRole(rs.getString("role"));
        user.setPassword(rs.getString("password"));
        return user;
    }
	
    // --- 2. 기존 로그인 (ID/PW 동시 검증) ---
    @Override
    public User login(String studentId, String password) {
        String sql = "SELECT * FROM users WHERE st_id = ? AND password = ?"; // 테이블명 'users' 사용
        
        try (Connection conn = DBConnection.getConnection(); // DBConnection 사용
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(studentId)); // st_id를 int로 변환
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

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    // ------------------------------------------------------------------
    // 👇👇👇 [오류 수정] 회원가입을 위해 추가된 메서드 👇👇👇
    // ------------------------------------------------------------------

    /**
     * 💡 [추가] 학번이 DB에 이미 존재하는지 확인합니다.
     * (UserService의 'isIdExists(String) is undefined' 오류 해결)
     * @param studentId 확인할 학번
     * @return 존재하면 true, 아니면 false
     */
    public boolean isIdExists(String studentId) {
        String sql = "SELECT COUNT(*) FROM users WHERE st_id = ?"; // 테이블명 'users' 사용
        
        try (Connection conn = DBConnection.getConnection(); // DBConnection 사용
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(studentId)); // st_id를 int로 변환
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // COUNT(*) 값이 0보다 크면 이미 존재
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("잘못된 학번 형식입니다 (isIdExists): " + studentId);
        }
        return false;
    }

    /**
     * 💡 [추가] 새로운 사용자 객체를 DB에 저장합니다 (회원가입).
     * (UserService의 'save(User) is undefined' 오류 해결)
     * @param user 저장할 User 객체
     * @return 성공적으로 저장되면 true, 아니면 false
     */
    public boolean save(User user) {
        String sql = "INSERT INTO users (st_id, name, role, penalty_count, password) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection(); // DBConnection 사용
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // UserService에서 설정한 User 객체의 값들을 사용합니다.
            pstmt.setInt(1, Integer.parseInt(user.getStudentId())); // st_id를 int로 변환
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getPenaltyCount());
            pstmt.setString(5, user.getPassword()); // 🚨 실제로는 해시된 비밀번호를 저장해야 합니다.
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // 1개 이상의 행이 영향을 받았다면 성공
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("잘못된 학번 형식입니다 (save): " + user.getStudentId());
        }
        return false;
    }
}