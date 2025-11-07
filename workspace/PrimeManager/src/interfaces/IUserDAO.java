package interfaces;

import model.User;

public interface IUserDAO {
//    User login(String studentId);
    User login(String studentId, String password);
}
