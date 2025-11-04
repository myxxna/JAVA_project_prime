// src/controller/kiosk/LoginController.java (재확인)
package controller.kiosk;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import java.io.IOException;


public class LoginController {

 // ... (기존 필드 및 UserService, currentUser 선언)
 
 // FXML에서 지정한 fx:id와 일치해야 합니다.
 @FXML private TextField userIdField;
 @FXML private PasswordField passwordField;
 @FXML private Button loginButton; 

 private UserService userService = new UserService();
 public static User currentUser; 

 @FXML
 private void handleLoginButtonAction(ActionEvent event) {
     String userId = userIdField.getText();
     char[] password = passwordField.getText().toCharArray(); 

     boolean isAuthenticated = userService.authenticateUser(userId, password);

     if (isAuthenticated) {
         
         // 1. 로그인한 사용자 정보 저장 (isAdmin 정보를 가져옴)
         currentUser = userService.getUserInfo(userId);
         
         // 2. ✨ 관리자 여부에 따라 화면 분기 ✨
         if (currentUser != null && currentUser.isAdmin()) {
             // 관리자 로그인 성공
             Alert adminAlert = new Alert(AlertType.INFORMATION);
             adminAlert.setTitle("관리자 로그인 성공");
             adminAlert.setHeaderText(null);
             adminAlert.setContentText(userId + "님, 관리자 시스템으로 진입합니다.");
             adminAlert.showAndWait();
             
             // 관리자 화면으로 전환
             loadNextScene("/view/admin/AdminView.fxml", "관리자 시스템"); // 🛑 경로와 파일명을 실제 환경에 맞게 수정하세요.
             
         } else if (currentUser != null) {
             // 일반 사용자 로그인 성공
             Alert successAlert = new Alert(AlertType.INFORMATION);
             successAlert.setTitle("로그인 성공");
             successAlert.setHeaderText(null);
             successAlert.setContentText(userId + "님, 좌석 예약 시스템에 환영합니다!");
             successAlert.showAndWait();
             
             // 일반 사용자 (키오스크) 화면으로 전환
             loadNextScene("/view/kiosk/SeatMapView.fxml", "좌석 예약 시스템"); // 🛑 경로와 파일명을 실제 환경에 맞게 수정하세요.

         } else {
             // 사용자 정보 로드 실패 (DB 문제)
             Alert errorAlert = new Alert(AlertType.ERROR);
             errorAlert.setContentText("로그인은 성공했으나 사용자 정보를 불러올 수 없습니다.");
             errorAlert.showAndWait();
         }
         
     } else {
         // 인증 실패 (기존 코드 유지)
         Alert errorAlert = new Alert(AlertType.ERROR);
         errorAlert.setTitle("로그인 실패");
         errorAlert.setHeaderText(null);
         errorAlert.setContentText("아이디 또는 비밀번호가 올바르지 않습니다.");
         errorAlert.showAndWait();
         
         passwordField.setText("");
     }
 }
    /**
     * FXML 로드 및 Scene 전환을 처리하는 헬퍼 메서드
     */
    private void loadNextScene(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert fatalError = new Alert(AlertType.ERROR);
            fatalError.setTitle("화면 전환 오류");
            fatalError.setHeaderText("프로그램 오류");
            fatalError.setContentText("다음 화면을 로드하는 데 실패했습니다. FXML 경로를 확인하세요.");
            fatalError.showAndWait();
        }
    }
}