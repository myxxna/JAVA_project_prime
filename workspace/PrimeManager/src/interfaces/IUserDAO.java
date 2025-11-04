package interfaces;

import model.User;

public interface IUserDAO {
    // 메서드 이름을 'Id'로 정확히 정의
    User findUserById(String userId); 
}