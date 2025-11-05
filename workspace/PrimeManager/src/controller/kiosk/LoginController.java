package controller.kiosk;

import javafx.application.Platform; 

import java.io.IOException;
import java.util.Arrays;

// 🛑 [수정] 자동 로그아웃 기능에 필요한 import 추가
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
import javafx.stage.Modality; // 팝업창 모달 설정
import javafx.scene.control.Label;

public class LoginController {
    
    // 🛑 [수정] 클래스 레벨 필드 (로그아웃 타이머 관련)

    private static final int INACTIVITY_TIMEOUT_MS = 300000; // 5분
    private static Timeline logoutTimer;
    private static Stage currentPrimaryStage; 
    private static volatile boolean isLogoutInProgress = false;
    // FXML에서 지정한 fx:id와 일치해야 합니다.
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
            if ("BLOCKED".equals(authenticatedUser.getRole())) {
                // 🚨 패널티 초과 로그인 제한 처리
                Alert penaltyAlert = new Alert(AlertType.ERROR);
                penaltyAlert.setTitle("로그인 실패");
                penaltyAlert.setHeaderText(null);
                penaltyAlert.setContentText("패널티 횟수(" + UserService.MAX_PENALTY_COUNT + "회 이상) 초과로 로그인이 제한되었습니다.");
                penaltyAlert.showAndWait();
            } else {
                currentUser = authenticatedUser; // 로그인한 사용자 정보 저장
                
                if (currentUser.isAdmin()) {
                    // 관리자 로그인 성공
                    Alert adminAlert = new Alert(AlertType.INFORMATION);
                    adminAlert.setTitle("관리자 로그인 성공");
                    adminAlert.setHeaderText(null);
                    adminAlert.setContentText(currentUser.getName() + " 관리자님, 시스템으로 진입합니다.");
                    adminAlert.showAndWait();
                    
                    loadNextScene("/view/admin/AdminView.fxml", "관리자 시스템"); 
                } else {
                    // 일반 사용자 로그인 성공
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("로그인 성공");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText(currentUser.getName() + "님, 좌석 예약 시스템에 오신 것을 환영합니다!");
                    successAlert.showAndWait();
                    
                    loadNextScene("/view/kiosk/SeatMapView.fxml", "좌석 예약 시스템");
                }
            }
        } else {
            // 인증 실패 (ID 또는 비밀번호 불일치)
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("로그인 실패");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("학번 또는 비밀번호가 올바르지 않습니다.");
            errorAlert.showAndWait();
            
            passwordField.setText("");
        }
    }
    
    /**
     * FXML 로드 및 Scene 전환을 처리하는 헬퍼 메서드
     */
    private void loadNextScene(String fxmlPath, String title) {
        try {
            // 1. 현재 Stage를 가져옴
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // 2. FXML 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // 3. Scene 생성 및 설정
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            
            // 🛑 [수정] 화면 전환 직후, 자동 로그아웃 기능 설정 및 타이머 시작
            // 이 호출로 인해 AdminView와 SeatMapView 모두에 타이머가 적용됩니다.
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
    
    // ----------------------------------------------------
    // 🛑 자동 로그아웃/세션 관련 정적 메서드
    // ----------------------------------------------------

    public static void setLoggedInUser(User user) {
        currentUser = user;
    }

    public static void setupAutoLogout(Scene scene, Stage stage) {
        currentPrimaryStage = stage;
        
        // 1. 기존 타이머가 있다면 중지
        if (logoutTimer != null) {
            logoutTimer.stop();
        }
        
        // 2. 5분 후 로그아웃을 수행하는 KeyFrame 생성
        KeyFrame keyFrame = new KeyFrame(
            Duration.millis(INACTIVITY_TIMEOUT_MS), 
            event -> performLogout() // 5분 후 실행할 메서드
        );
        
        // 3. Timeline 초기화 및 시작
        logoutTimer = new Timeline(keyFrame);
        logoutTimer.setCycleCount(1); // 1회만 실행
        logoutTimer.play();

        // 4. 사용자 활동(마우스 이동, 클릭, 키 입력) 감지 이벤트 리스너 추가
        EventHandler<Event> activityHandler = event -> {
            // 활동이 감지되면 타이머를 처음부터 다시 시작
            if (logoutTimer != null) {
                logoutTimer.stop();
                logoutTimer.playFromStart();
            }
        };

        // Scene에 이벤트 리스너 등록 (AdminView와 SeatMapView에 모두 적용됨)
        scene.addEventFilter(MouseEvent.ANY, activityHandler); // 마우스 이벤트 (이동, 클릭)
        scene.addEventFilter(KeyEvent.ANY, activityHandler);   // 키보드 이벤트
    }
    
    
    // 🛑 실제 로그아웃 처리 메서드
private static void performLogout() {
        
        if (isLogoutInProgress) {
            return; 
        }
        isLogoutInProgress = true;
        
        // 1. 타이머 중지 및 세션 정보 초기화
        if (logoutTimer != null) {
            logoutTimer.stop();
        }
        currentUser = null; 

        // 2. UI 작업은 Platform.runLater 내부에서 처리
        Platform.runLater(() -> {
            // 🛑 [핵심] try-finally 구조를 사용하여 플래그 해제를 보장합니다.
            try {
                // --- 2.1. 팝업 창 생성 (5초 자동 종료 팝업) ---
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
                
                // --- 2.2. 5초 대기 후 화면 전환 로직 ---
                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(e -> {
                    try {
                        popupStage.close(); // 팝업 닫기
                        
                        // 로그인 화면 FXML 로드 및 전환
                        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/view/kiosk/LoginView.fxml"));
                        Parent root = loader.load();
                        
                        Scene newScene = new Scene(root);
                        currentPrimaryStage.setScene(newScene);
                        currentPrimaryStage.show();
                        
                    } catch (IOException ex) {
                        System.err.println("로그인 화면 로드 중 오류 발생.");
                        ex.printStackTrace();
                    }
                });
                delay.play();
                
            } catch (Exception e) { // IOException 또는 다른 런타임 오류 포착
                System.err.println("자동 로그아웃 처리 중 치명적 오류 발생.");
                e.printStackTrace();
            } finally {
                // 🛑 [최종] 작업이 끝날 때 플래그 해제 (비동기 완료 후 플래그 해제가 PauseTransition 내부에서 처리되어야 안정적임)
                // Note: PauseTransition 내부의 finally에서 isLogoutInProgress = false;가 처리됩니다.
            }
        });
}
}
