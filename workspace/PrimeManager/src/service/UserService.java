// src/service/UserService.java
package service;

import impl.UserDAOImpl;
import interfaces.IUserDAO; 
import model.User;
import java.util.Arrays; // Arrays.fill 사용을 위해 추가

public class UserService {

    // 💡 인터페이스 대신 구현체로 선언하여 findUserByIdAndPwd 메서드에 접근 용이하게 함
    private UserDAOImpl userDAO = new UserDAOImpl(); 

    public UserService() {
    }

    /**
     * 🛑 [수정됨] ID와 비밀번호를 동시에 DB에서 검증합니다.
     * @param userId 사용자 ID
     * @param password 사용자가 입력한 비밀번호 (char 배열)
     * @return 인증 성공 시 true, 실패 시 false
     */
    public boolean authenticateUser(String userId, char[] password) {
        if (userId == null || userId.trim().isEmpty() || password == null || password.length == 0) {
            return false;
        }
        
        String inputPassword = new String(password);
        
        // 1. 🛑 ID와 PW를 모두 DAO에 전달하여 DB에서 동시 검증
        //    (findUserByIdAndPwd는 ID와 PW가 모두 맞을 때만 User 객체를 반환합니다.)
        User user = userDAO.findUserByIdAndPwd(userId, inputPassword); 
        
        // 2. 비밀번호 정보를 메모리에서 지웁니다 (보안 강화)
        Arrays.fill(password, ' '); 
        
        // 3. 사용자가 DB에 존재하면 인증 성공 (user != null)
        return user != null;
    }
    
    /**
     * [유지] 로그인 성공 후 사용자 정보를 가져옵니다.
     * 이 정보는 LoginController에서 isAdmin 분기에 사용됩니다.
     */
    public User getUserInfo(String userId) {
        // ID만으로 조회하는 메서드를 사용 (findUserById)
        return userDAO.findUserById(userId); 
    }
}