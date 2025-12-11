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
     * [입실/퇴실하기] 버튼 클릭 시
     * - 예약 모드를 false로 설정하고 좌석 화면으로 이동
     */
    @FXML
    private void handleCheckInButtonAction(ActionEvent event) {
        // ★ 핵심: 입실 모드로 설정
        SeatController.setReservationMode(false); 
        
        loadScene(event, "/view/kiosk/SeatMapView4Fprivate.fxml", "좌석 입실");
    }

    /**
     * [예약하기] 버튼 클릭 시
     * - 예약 모드를 true로 설정하고 좌석 화면으로 이동
     */
    @FXML
    private void handleReserveButtonAction(ActionEvent event) {
        // ★ 핵심: 예약 모드로 설정
        SeatController.setReservationMode(true);
        
        // 입실과 동일한 FXML을 사용하지만, SeatController 내부에서 다르게 동작함
        loadScene(event, "/view/kiosk/SeatMapView4Fprivate.fxml", "좌석 예약");
    }

    /**
     * [연장하기] 버튼 클릭 시
     */
    @FXML
    private void handleExtendButtonAction(ActionEvent event) {
        // 연장은 별도의 로직이므로 기존 유지
        loadScene(event, "/view/kiosk/ExtendView.fxml", "연장하기");
    }

    /**
     * [로그아웃] 버튼 클릭 시
     */
    @FXML
    private void handleLogoutButtonAction(ActionEvent event) {
        loadScene(event, "/view/kiosk/LoginView.fxml", "키오스크");
    }

    /**
     * 화면 전환 헬퍼 메서드
     */
    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1400, 800);
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("FXML 로드 실패: " + fxmlPath); // 디버깅용 로그 추가
        }
    }
}