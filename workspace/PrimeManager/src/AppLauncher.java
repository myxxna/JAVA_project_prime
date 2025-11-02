


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



/*import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // FXML 로드
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/kiosk/LoginView.fxml"));
        Parent root = loader.load();

        // Scene 세팅
        Scene scene = new Scene(root);

        // Stage 세팅
        primaryStage.setTitle("프라임 키오스크");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // JavaFX 앱 시작
    }
}*/
