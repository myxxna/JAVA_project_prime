package controller.kiosk;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;              // Alert import
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;    // TextInputDialog import
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

import model.User;                              // User import (오류 해결)
import service.PenaltyService;

public class ReportSeatController {

    private PenaltyService penaltyService = new PenaltyService();

    @FXML
    private void handleSeatClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String seatNumStr = clickedButton.getText();
        
        // 버튼 텍스트가 숫자인지 확인 (방어 코드)
        int seatIndex;
        try {
            seatIndex = Integer.parseInt(seatNumStr);
        } catch (NumberFormatException e) {
            return; // 숫자가 아니면 무시
        }

        // 1. 현재 로그인한 신고자 정보 가져오기
        User reporter = LoginController.getCurrentLoggedInUser();

        if (reporter == null) {
            showAlert(Alert.AlertType.WARNING, "알림", "신고하려면 먼저 로그인해야 합니다.");
            return;
        }

        // 2. 사유 입력 팝업
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("좌석 신고");
        dialog.setHeaderText(seatIndex + "번 좌석에 대해 신고하시겠습니까?");
        dialog.setContentText("신고 사유를 입력하세요:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "경고", "신고 사유를 입력해야 합니다.");
                return;
            }

            // 3. DB 저장 (결과를 String으로 받음)
            String resultMsg = penaltyService.insertPenalty(reporter.getId(), reason, seatIndex);

            if ("SUCCESS".equals(resultMsg)) {
                showAlert(Alert.AlertType.INFORMATION, "접수 완료", "신고가 정상적으로 접수되었습니다.");
            } else {
                showAlert(Alert.AlertType.ERROR, "접수 실패", "오류 내용:\n" + resultMsg);
            }
        });
    }
    
    // 뒤로가기 버튼
    @FXML
    private void handleBackAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/kiosk/MainMenuView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ★ [수정됨] showAlert 메서드를 3개의 인자를 받도록 수정했습니다.
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}