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
import javafx.stage.Modality;
import javafx.scene.control.Label;
import javafx.scene.Node;

public class LoginController {

    private static final int INACTIVITY_TIMEOUT_MS = 300000;
    private static Timeline logoutTimer;
    private static Stage currentPrimaryStage;
    private static volatile boolean isLogoutInProgress = false;

    @FXML private TextField studentIdField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private UserService userService = new UserService();
    public static User currentUser;

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String userId = studentIdField.getText();
        char[] password = passwordField.getText().toCharArray();

        User authenticatedUser = userService.authenticate(userId, password);
        Arrays.fill(password, ' ');

        if (authenticatedUser != null) {

            if (authenticatedUser.getPenaltyCount() >= UserService.MAX_PENALTY_COUNT) {
                Alert penaltyAlert = new Alert(AlertType.ERROR);
                penaltyAlert.setTitle("로그인 실패");
                penaltyAlert.setHeaderText(null);
                penaltyAlert.setContentText("패널티 횟수(" + UserService.MAX_PENALTY_COUNT + "회 이상) 초과로 로그인이 제한되었습니다.");
                penaltyAlert.showAndWait();
            } else {
                currentUser = authenticatedUser;

                if (currentUser.isAdmin()) {
                    Alert adminAlert = new Alert(AlertType.INFORMATION);
                    adminAlert.setTitle("관리자 로그인 성공");
                    adminAlert.setHeaderText(null);
                    adminAlert.setContentText(currentUser.getName() + " 관리자님, 시스템으로 진입합니다.");
                    adminAlert.showAndWait();

                    loadNextScene(event, "/view/admin/AdminView.fxml", "관리자 시스템");
                } else {
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("로그인 성공");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText(currentUser.getName() + "님, 좌석 예약 시스템에 오신 것을 환영합니다!");
                    successAlert.showAndWait();

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

    private void loadNextScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene;

            if (fxmlPath.contains("SeatMapView.fxml")) {
                scene = new Scene(root, 900, 650);
                stage.setMaximized(false);
                stage.setResizable(false);
            } else if (fxmlPath.contains("/admin/")) {
                scene = new Scene(root);
                stage.setMaximized(true);
                stage.setResizable(true);
            } else {
                scene = new Scene(root);
            }

            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();

            setupAutoLogout(scene, stage);

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


    public static void setLoggedInUser(User user) {
        currentUser = user;
    }

    public static void setupAutoLogout(Scene scene, Stage stage) {
        currentPrimaryStage = stage;

        if (logoutTimer != null) {
            logoutTimer.stop();
        }

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

        if (isLogoutInProgress) {
            return;
        }
        isLogoutInProgress = true;

        if (logoutTimer != null) {
            logoutTimer.stop();
        }
        currentUser = null;

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

                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(e -> {
                    try {
                        popupStage.close();

                        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/view/kiosk/LoginView.fxml"));
                        Parent root = loader.load();

                        Scene newScene = new Scene(root);
                        currentPrimaryStage.setScene(newScene);
                        currentPrimaryStage.show();

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } finally {
                        isLogoutInProgress = false;
                    }
                });
                delay.play();

            } catch (Exception e) {
                e.printStackTrace();
                isLogoutInProgress = false;
            }
        });
    }
}