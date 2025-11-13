package controller.kiosk;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainMenuController {

    @FXML private Button checkInButton;
    @FXML private Button reserveButton;
    @FXML private Button extendButton;
    @FXML private Button logoutButton;

    /**
     * [입실/퇴실하기] 버튼 클릭 시: SeatMapView.fxml (좌석 선택)로 이동합니다.
     */
    @FXML
    private void handleCheckInButtonAction(ActionEvent event) {
        // 이전에 로그인 성공 시 가던 '/view/kiosk/SeatMapView.fxml'로 연결합니다.
        loadScene(event, "/view/kiosk/SeatMapView.fxml", "좌석 예약 시스템");
    }

    /**
     * [예약하기] 버튼 클릭 시: (아직 구현되지 않음)
     */
    @FXML
    private void handleReserveButtonAction(ActionEvent event) {
        // TODO: "예약하기" FXML 파일 경로로 수정해야 합니다.
        // 예: loadScene(event, "/view/kiosk/ReservationView.fxml", "예약하기");
        System.out.println("예약하기 버튼 클릭됨 (구현 필요)");
    }

    /**
     * [연장하기] 버튼 클릭 시: (아직 구현되지 않음)
     */
    @FXML
    private void handleExtendButtonAction(ActionEvent event) {
        // TODO: "연장하기" FXML 파일 경로로 수정해야 합니다.
        // 예: loadScene(event, "/view/kiosk/ExtendView.fxml", "연장하기");
        System.out.println("연장하기 버튼 클릭됨 (구현 필요)");
    }

    /**
     * [로그아웃] 버튼 클릭 시: LoginView.fxml (로그인 화면)로 돌아갑니다.
     */
    @FXML
    private void handleLogoutButtonAction(ActionEvent event) {
        loadScene(event, "/view/kiosk/LoginView.fxml", "키오스크");
    }

    /**
     * 화면을 1920x1080 크기로 전환하고 style.css를 로드하는 공통 헬퍼 메서드
     */
    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1920, 1080);
            
            // Inter 폰트 등이 적용된 CSS 로드
            
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}