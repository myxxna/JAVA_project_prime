package service;

import impl.UserDAOImpl;
import model.User;
import java.util.Arrays; 

public class UserService {
    
    private UserDAOImpl userDAO = new UserDAOImpl(); 
    public static final int MAX_PENALTY_COUNT = 3;
    
    public UserService() {
    }
    
    public User authenticate(String userId, char[] password) {
        if (userId == null || userId.trim().isEmpty() || password == null || password.length == 0) {
            return null;
        }
        
        String inputPassword = new String(password);
        User user = userDAO.login(userId, inputPassword);
        
        Arrays.fill(password, ' '); 
        
        if (user == null) {
            return null; 
        }

        // 패널티 확인
        if (user.getPenaltyCount() >= MAX_PENALTY_COUNT) {
            user.setRole("BLOCKED"); 
            return user;
        }
        
        // ★ [수정됨] 로그인 기록('L') 저장 코드 삭제함
        // 이제 로그인만 성공하면 바로 유저 정보를 리턴합니다.
        
        return user;
    }
   
    public boolean isStudentIdExists(String studentId) {
        return userDAO.isIdExists(studentId); 
    }

    public boolean registerUser(String studentId, String name, String password) {
        User newUser = new User();
        newUser.setStudentId(studentId);
        newUser.setName(name);
        newUser.setPassword(password); 
        newUser.setRole("USER"); 
        newUser.setPenaltyCount(0); 
        
        boolean success = userDAO.save(newUser);
        return success;
    }
}