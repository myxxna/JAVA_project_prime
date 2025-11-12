//src/service/UserService.java
package service;

import impl.UserDAOImpl;
import interfaces.IUserDAO; 
import model.User;
import java.util.Arrays; // Arrays.fill 사용을 위해 추가

public class UserService {
	
    // 💡 인터페이스 대신 구현체로 선언하여 findUserByIdAndPwd 메서드에 접근 용이하게 함
    private UserDAOImpl userDAO = new UserDAOImpl(); 
    public static final int MAX_PENALTY_COUNT = 3;
    public UserService() {
    }
    
    /**
     * 🛑 [수정됨] ID와 비밀번호를 동시에 DB에서 검증합니다.
     * @param userId 사용자 ID
     * @param password 사용자가 입력한 비밀번호 (char 배열)
     * @return 인증 성공 시 true, 실패 시 false
     */
    public User authenticate(String userId, char[] password) {
        if (userId == null || userId.trim().isEmpty() || password == null || password.length == 0) {
            return null;
        }
        
        String inputPassword = new String(password);
        
        // 1. ID와 PW를 모두 DAO에 전달하여 DB에서 동시 검증 및 User 정보 획득
        // (findUserByIdAndPwd는 ID와 PW가 모두 맞을 때만 User 객체를 반환합니다.)
        User user = userDAO.login(userId, inputPassword);
        
        // 2. 비밀번호 정보를 메모리에서 지웁니다 (보안 강화)
        Arrays.fill(password, ' '); 
        
        if (user == null) {
            // 3. ID/PW 불일치 (인증 실패)
            return null;
        }

        // 4. 패널티 횟수 확인 (인증은 성공했으나, 패널티로 인해 접근 거부인지 확인)
        if (user.getPenaltyCount() >= MAX_PENALTY_COUNT) {
            // 🚨 접근 거부 상태를 Controller에 알리기 위해, 
            //    실제 DB값 대신 임시로 'BLOCKED' 상태를 설정하여 반환합니다.
            user.setRole("BLOCKED"); // 👈 Controller에서 이 값을 확인하도록 유도
            return user;
        }
        
        // 5. 인증 성공 및 접근 허용
        return user;
        
    }
   
   public boolean isStudentIdExists(String studentId) {
       // userDAOImpl에 학번 존재 여부를 확인하는 메서드가 필요합니다.
       // 예를 들어, userDAO.isIdExists(studentId)를 호출합니다.
       return userDAO.isIdExists(studentId); 
   }

   /**
    * 💡 [추가] 새로운 사용자를 등록하고 초기값을 설정합니다.
    * @param studentId 학번
    * @param name 이름
    * @param password 비밀번호 (평문)
    * @return 등록 성공 시 true, 실패 시 false
    */
   public boolean registerUser(String studentId, String name, String password) {
       // 1. 새로운 User 객체 생성 및 초기값 설정
       User newUser = new User();
       newUser.setStudentId(studentId);
       newUser.setName(name);
       newUser.setPassword(password); // ★DAO에서 DB 저장 전에 해시 처리해야 합니다★
       newUser.setRole("USER"); // 기본 역할은 USER
       newUser.setPenaltyCount(0); // 기본 패널티는 0
       
       // 2. DAO를 통해 DB에 저장
       // userDAOImpl에 User 객체를 받아 저장하는 save 메서드가 필요합니다.
       boolean success = userDAO.save(newUser);
       
       // 3. 비밀번호 정보 메모리에서 지우기 (보안)
       // (참고: 입력된 비밀번호는 Controller에서 String으로 처리되었으므로, 
       //  Controller나 이 메서드 외부에서 메모리 관리가 필요할 수 있습니다.)

       return success;
   }
}