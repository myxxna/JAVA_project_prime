package interfaces;

import model.User;
import java.util.Optional;

public interface IUserDAO {
//    User login(String studentId);
	Optional<User> findById(int userId);
    User login(String studentId, String password);
}
