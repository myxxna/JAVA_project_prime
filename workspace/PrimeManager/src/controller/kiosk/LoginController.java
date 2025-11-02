package controller.kiosk;

import service.UserService; // ← 이 라인이 반드시 있어야 함
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;

public class LoginController {
    @FXML
    private TextField studentIdField;

    @FXML
    private PasswordField passwordField;

    private UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String studentId = studentIdField.getText();
        String password = passwordField.getText();

        if(userService.login(studentId, password)) {
            System.out.println("로그인 성공!");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("로그인 실패");
            alert.showAndWait();
        }
    }
}

