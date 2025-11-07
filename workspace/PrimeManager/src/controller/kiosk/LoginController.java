package controller.kiosk;

import javafx.application.Platform;

import java.io.IOException;
import java.util.Arrays;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.User;
import service.UserService;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
<<<<<<< Updated upstream
<<<<<<< HEAD

import javafx.stage.Modality; 
=======
import javafx.stage.Modality;
>>>>>>> main
=======
import javafx.stage.Modality;
>>>>>>> Stashed changes
import javafx.scene.control.Label;
import javafx.scene.Node;

public class LoginController {
<<<<<<< Updated upstream
<<<<<<< HEAD
    

    private static final int INACTIVITY_TIMEOUT_MS = 300000; // 5분
=======

    private static final int INACTIVITY_TIMEOUT_MS = 300000;
>>>>>>> main
=======

    private static final int INACTIVITY_TIMEOUT_MS = 300000;
>>>>>>> Stashed changes
    private static Timeline logoutTimer;
    private static Stage currentPrimaryStage;
    private static volatile boolean isLogoutInProgress = false;

<<<<<<< Updated upstream
<<<<<<< HEAD

=======
>>>>>>> main
=======
>>>>>>> Stashed changes
    @FXML private TextField studentIdField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private UserService userService = new UserService();

    public static User currentUser; 
    
    public static User getCurrentLoggedInUser() {
        return currentUser;
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String userId = studentIdField.getText();
        char[] password = passwordField.getText().toCharArray();

        User authenticatedUser = userService.authenticate(userId, password);
        Arrays.fill(password, ' ');

        if (authenticatedUser != null) {

<<<<<<< Updated upstream
<<<<<<< HEAD
            
            // (★수정★) DB 스키마에 맞게 'role'이 아닌 'penalty_count'를 직접 확인
=======
>>>>>>> main
=======
>>>>>>> Stashed changes
            if (authenticatedUser.getPenaltyCount() >= UserService.MAX_PENALTY_COUNT) {
                Alert penaltyAlert = new Alert(AlertType.ERROR);
                penaltyAlert.setTitle("로그인 실패");
                penaltyAlert.setHeaderText(null);
                penaltyAlert.setContentText("패널티 횟수(" + UserService.MAX_PENALTY_COUNT + "회 이상) 초과로 로그인이 제한되었습니다.");
                penaltyAlert.showAndWait();
            } else {
<<<<<<< Updated upstream
<<<<<<< HEAD

                currentUser = authenticatedUser; 
                
=======
                currentUser = authenticatedUser;

>>>>>>> main
=======
                currentUser = authenticatedUser;

>>>>>>> Stashed changes
                if (currentUser.isAdmin()) {
                    Alert adminAlert = new Alert(AlertType.INFORMATION);
                    adminAlert.setTitle("관리자 로그인 성공");
                    adminAlert.setHeaderText(null);
                    adminAlert.setContentText(currentUser.getName() + " 관리자님, 시스템으로 진입합니다.");
                    adminAlert.showAndWait();
<<<<<<< Updated upstream
<<<<<<< HEAD
                    

                    // (★수정★) event를 loadNextScene으로 전달
                    loadNextScene(event, "/view/admin/AdminView.fxml", "관리자 시스템"); 
=======

                    loadNextScene(event, "/view/admin/AdminView.fxml", "관리자 시스템");
>>>>>>> main
=======

                    loadNextScene(event, "/view/admin/AdminView.fxml", "관리자 시스템");
>>>>>>> Stashed changes
                } else {
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("로그인 성공");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText(currentUser.getName() + "님, 좌석 예약 시스템에 오신 것을 환영합니다!");
                    successAlert.showAndWait();
<<<<<<< Updated upstream
<<<<<<< HEAD
                    

                    // (★수정★) event를 loadNextScene으로 전달
=======

>>>>>>> main
=======

>>>>>>> Stashed changes
                    loadNextScene(event, "/view/kiosk/SeatMapView.fxml", "좌석 예약 시스템");
                }
            }
        } else {
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("로그인 실패");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("학번 또는 비밀번호가 올바르지 않습니다.");
            errorAlert.showAndWait();
            passwordField.setText("");
        }
    }
<<<<<<< Updated upstream
<<<<<<< HEAD
    
    /**

     * (★수정★) FXML 로드 및 Scene 전환, 창 최대화 로직 추가
     */
=======

>>>>>>> main
=======

>>>>>>> Stashed changes
    private void loadNextScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();

<<<<<<< Updated upstream
<<<<<<< HEAD
            
            // 2. FXML 로드
=======
>>>>>>> Stashed changes
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

<<<<<<< Updated upstream
            
            // (★이것이 해결책입니다★)
            if (fxmlPath.contains("/admin/")) {
                stage.setMaximized(true); // 창 최대화
                stage.setResizable(true);  // 크기 조절 가능하게 (필수)
=======
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

=======
>>>>>>> Stashed changes
            Scene scene;

            if (fxmlPath.contains("SeatMapView.fxml")) {
                scene = new Scene(root, 900, 650);
                stage.setMaximized(false);
                stage.setResizable(false);
            } else if (fxmlPath.contains("/admin/")) {
                scene = new Scene(root);
                stage.setMaximized(true);
                stage.setResizable(true);
<<<<<<< Updated upstream
>>>>>>> main
=======
>>>>>>> Stashed changes
            } else {
                scene = new Scene(root);
            }

            stage.setTitle(title);
            stage.setScene(scene);
<<<<<<< Updated upstream
<<<<<<< HEAD
            

            setupAutoLogout(scene, stage); 
=======
            stage.centerOnScreen();

            setupAutoLogout(scene, stage);
>>>>>>> main
=======
            stage.centerOnScreen();

            setupAutoLogout(scene, stage);
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
<<<<<<< HEAD
    
    // ----------------------------------------------------

    // ----------------------------------------------------
=======

>>>>>>> main
=======

>>>>>>> Stashed changes

    public static void setLoggedInUser(User user) {
        currentUser = user;
    }

    public static void setupAutoLogout(Scene scene, Stage stage) {
        currentPrimaryStage = stage;
<<<<<<< Updated upstream
<<<<<<< HEAD
        

=======
>>>>>>> main
=======
>>>>>>> Stashed changes

        if (logoutTimer != null) {
            logoutTimer.stop();
        }
<<<<<<< Updated upstream
<<<<<<< HEAD
        
=======
>>>>>>> main
=======
>>>>>>> Stashed changes

        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(INACTIVITY_TIMEOUT_MS),
                event -> performLogout()
        );

        logoutTimer = new Timeline(keyFrame);
        logoutTimer.setCycleCount(1);
        logoutTimer.play();

        EventHandler<Event> activityHandler = event -> {
            if (logoutTimer != null) {
                logoutTimer.stop();
                logoutTimer.playFromStart();
            }
        };

        scene.addEventFilter(MouseEvent.ANY, activityHandler);
        // ▼▼▼▼▼ [★ 여기가 수정된 부분 ★] ▼▼▼▼▼
        scene.addEventFilter(KeyEvent.ANY, activityHandler);
        // ▲▲▲▲▲ [★ 수정 완료 ★] ▲▲▲▲▲
    }


    private static void performLogout() {

<<<<<<< Updated upstream
<<<<<<< HEAD
        
=======
>>>>>>> main
=======
>>>>>>> Stashed changes
        if (isLogoutInProgress) {
            return;
        }
        isLogoutInProgress = true;
<<<<<<< Updated upstream
<<<<<<< HEAD
        

=======
>>>>>>> main
=======
>>>>>>> Stashed changes

        if (logoutTimer != null) {
            logoutTimer.stop();
        }
        currentUser = null;
<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes

        Platform.runLater(() -> {
            try {
                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle("자동 로그아웃");

                VBox layout = new VBox(10);
                layout.setAlignment(Pos.CENTER);
                layout.setPadding(new Insets(20));
                layout.getChildren().addAll(
                        new Label("비활성화로 인해 자동 로그아웃되었습니다."),
                        new Label("5초 후 로그인 화면으로 돌아갑니다.")
                );

                Scene popupScene = new Scene(layout, 350, 150);
                popupStage.setScene(popupScene);
                popupStage.show();
<<<<<<< Updated upstream
<<<<<<< HEAD
                
=======
>>>>>>> main
=======
>>>>>>> Stashed changes

                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(e -> {
                    try {
                        popupStage.close();
<<<<<<< Updated upstream
<<<<<<< HEAD
                        
=======
>>>>>>> main
=======
>>>>>>> Stashed changes

                        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/view/kiosk/LoginView.fxml"));
                        Parent root = loader.load();

                        Scene newScene = new Scene(root);
                        currentPrimaryStage.setScene(newScene);
                        currentPrimaryStage.show();
<<<<<<< Updated upstream

                    } catch (IOException ex) {
=======
>>>>>>> Stashed changes

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } finally {
<<<<<<< Updated upstream
<<<<<<< HEAD
                        isLogoutInProgress = false; 

=======
                        isLogoutInProgress = false;
>>>>>>> Stashed changes
                    }
                });
                delay.play();

<<<<<<< Updated upstream
            } catch (Exception e) { 
=======
                        isLogoutInProgress = false;
                    }
                });
                delay.play();

            } catch (Exception e) {
>>>>>>> main
=======
            } catch (Exception e) {
>>>>>>> Stashed changes
                e.printStackTrace();
                isLogoutInProgress = false;
            }
        });
    }
}