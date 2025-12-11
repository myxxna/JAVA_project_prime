package controller.kiosk;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // 초기화를 위해 추가
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;              // 오류 해결
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;    // 오류 해결
import javafx.stage.Stage;

import java.util.Optional;                      // 오류 해결
import model.User;                              // 오류 해결
import service.PenaltyService;                  // 오류 해결

public class MainMenuController implements Initializable {

    // FXML 버튼 연결
    @FXML private Button checkInButton;
    @FXML private Button reserveButton;
    @FXML private Button extendButton;
    @FXML private Button logoutButton;
    @FXML private Button reportButton; // 신고하기 버튼 추가

    // 서비스 객체 생성 (오류 해결)
    private PenaltyService penaltyService = new PenaltyService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 필요 시 초기화 로직 작성
    }

    /**
     * [입실/퇴실하기] 버튼 클릭 시
     * - 예약 모드 OFF, 신고 모드 OFF
     */
    @FXML
    private void handleCheckInButtonAction(ActionEvent event) {
        SeatController.setReservationMode(false); 
        SeatController.setReportMode(false); // 신고 모드 해제
        
        loadScene(event, "/view/kiosk/SeatMapView4Fprivate.fxml", "좌석 입실");
    }

    /**
     * [예약하기] 버튼 클릭 시
     * - 예약 모드 ON, 신고 모드 OFF
     */
    @FXML
    private void handleReserveButtonAction(ActionEvent event) {
        SeatController.setReservationMode(true);
        SeatController.setReportMode(false); // 신고 모드 해제
        
        loadScene(event, "/view/kiosk/SeatMapView4Fprivate.fxml", "좌석 예약");
    }

    /**
     * [신고하기] 버튼 클릭 시 (가장 중요)
     * - ★ 신고 모드 ON
     * - 좌석 배치도 화면으로 이동
     */
    @FXML
    private void handleReportButtonAction(ActionEvent event) {
        // 1. 화면을 로드하기 전에 '신고 모드'를 true로 설정
        SeatController.setReportMode(true);
        SeatController.setReservationMode(false);

        // 2. 좌석 배치도 화면으로 이동 (여기서 SeatController가 초기화됨)
        loadScene(event, "/view/kiosk/SeatMapView4Fprivate.fxml", "좌석 신고");
    }

    /**
     * [연장하기] 버튼 클릭 시
     */
    @FXML
    private void handleExtendButtonAction(ActionEvent event) {
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
            // 화면 전환 실패 시 알림
            showAlert(Alert.AlertType.ERROR, "오류", "화면을 불러오는 중 오류가 발생했습니다.\n" + fxmlPath);
        }
    }

    /**
     * [오류 해결] 알림창 표시 메서드
     * - showAlert 오류를 해결하기 위해 추가된 메서드입니다.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}