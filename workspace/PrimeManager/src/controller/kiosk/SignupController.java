package controller.kiosk;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserService;
import javafx.scene.input.MouseEvent;

public class SignupController {
    
    @FXML private TextField studentIdField;
    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Button backButton;

    private UserService userService = new UserService();

    @FXML
    private void handleSignupButtonAction(ActionEvent event) {
        String studentId = studentIdField.getText().trim();
        String name = nameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 1. 입력 유효성 검사
        if (studentId.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.WARNING, "경고", "모든 정보를 입력해 주세요.");
            return;
        }

        // 2. 비밀번호 일치 확인
        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "오류", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            passwordField.setText("");
            confirmPasswordField.setText("");
            return;
        }
        
        // 3. 학번 중복 확인
        if (userService.isStudentIdExists(studentId)) {
             showAlert(AlertType.ERROR, "오류", "이미 등록된 학번입니다. 로그인하거나 다른 학번을 사용하세요.");
             return;
        }

        // 4. 사용자 등록 시도
        if (userService.registerUser(studentId, name, password)) {
            showAlert(AlertType.INFORMATION, "성공", "회원가입이 완료되었습니다! 로그인 화면으로 돌아갑니다.");
            handleBackButtonAction(event); // 회원가입 성공 후 로그인 화면으로 이동
        } else {
            showAlert(AlertType.ERROR, "오류", "회원가입에 실패했습니다. 데이터베이스 연결을 확인하세요.");
        }
    }
    
    @FXML
    private void handleBackButtonAction(ActionEvent event) {
        // 로그인 화면으로 돌아가는 로직
        try {
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            // LoginView.fxml 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/kiosk/LoginView.fxml"));
            Parent root = loader.load();
            
            // 👇 [수정 1] Scene 생성 시 크기를 1400,800으로 변경
            Scene scene = new Scene(root, 1400,800); 
            
            stage.setTitle("키오스크"); 
            stage.setScene(scene);
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- 👇 [수정 2] FXML과 동일한 195px, 55px, 20px 크기로 스타일 상수 변경 ---
    private final String SIGNUP_BUTTON_STYLE_DEFAULT = "-fx-background-color: #3366FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-pref-width: 195px; -fx-pref-height: 45px; -fx-background-radius: 10px; -fx-cursor: hand;";
    private final String SIGNUP_BUTTON_STYLE_HOVER = "-fx-background-color: #2851CC; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-pref-width: 195px; -fx-pref-height: 45px; -fx-background-radius: 10px; -fx-cursor: hand;";
    private final String CANCEL_BUTTON_STYLE_DEFAULT = "-fx-background-color: #ADB5BD; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-pref-width: 195px; -fx-pref-height: 45px; -fx-background-radius: 10px; -fx-cursor: hand;";
    private final String CANCEL_BUTTON_STYLE_HOVER = "-fx-background-color: #868E96; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-pref-width: 195px; -fx-pref-height: 45px; -fx-background-radius: 10px; -fx-cursor: hand;";

    @FXML
    private void handleSignupEnter(MouseEvent event) {
        signupButton.setStyle(SIGNUP_BUTTON_STYLE_HOVER);
    }
    
    @FXML
    private void handleSignupExit(MouseEvent event) {
        signupButton.setStyle(SIGNUP_BUTTON_STYLE_DEFAULT);
    }
    
    @FXML
    private void handleCancelEnter(MouseEvent event) {
        backButton.setStyle(CANCEL_BUTTON_STYLE_HOVER);
    }
    
    @FXML
    private void handleCancelExit(MouseEvent event) {
        backButton.setStyle(CANCEL_BUTTON_STYLE_DEFAULT);
    }
}