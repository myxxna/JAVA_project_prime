package controller.kiosk;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Seat;
import model.User;
import service.ExtendService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class ExtendController {

    @FXML private Text seatInfoText;
    @FXML private Text remainTimeText;
    @FXML private Text totalTimeText; // 화면에 총 이용시간 표시용

    private ExtendService extendService = new ExtendService();
    private Seat currentSeat;
    private Timeline timer;

    // 상수: 최대 이용 가능 시간 (3시간 = 180분)
    private static final int MAX_USAGE_MINUTES = 180;

    @FXML
    public void initialize() {
        User currentUser = LoginController.getCurrentLoggedInUser();
        if (currentUser == null) {
            updateStatusText("로그인 필요", "00:00:00");
            return;
        }

        currentSeat = extendService.getSeatByUserId(currentUser.getId());

        if (currentSeat != null) {
            String seatInfo = String.format("%d층 %s번 좌석", currentSeat.getFloor(), currentSeat.getSeatNumber());
            updateStatusText(seatInfo, "계산 중...");
            
            // 총 이용 시간 계산 및 표시
            calculateAndShowTotalTime();
            
            startRemainTimer();
        } else {
            updateStatusText("이용 좌석 없음", "00:00:00");
        }
    }

    // ================= 연장 버튼 핸들러 (4개) =================
    @FXML public void handleExtend30(ActionEvent event) { tryExtend(30); }
    @FXML public void handleExtend60(ActionEvent event) { tryExtend(60); }
    @FXML public void handleExtend90(ActionEvent event) { tryExtend(90); }
    @FXML public void handleExtend120(ActionEvent event) { tryExtend(120); }

    /**
     * [핵심 알고리즘] 연장 시도 로직
     */
    private void tryExtend(int addMinutes) {
        if (currentSeat == null || currentSeat.getStartTime() == null || currentSeat.getEndTime() == null) {
            showAlert(Alert.AlertType.ERROR, "오류", "좌석 정보가 올바르지 않습니다.");
            return;
        }

        // 1. 현재까지의 총 이용 예정 시간 계산 (시작시간 ~ 현재 종료시간)
        long currentTotalMinutes = ChronoUnit.MINUTES.between(currentSeat.getStartTime(), currentSeat.getEndTime());
        
        // 2. 연장 후 총 시간 계산
        long expectedTotalMinutes = currentTotalMinutes + addMinutes;

        // 3. 3시간(180분) 초과 여부 확인
        if (expectedTotalMinutes > MAX_USAGE_MINUTES) {
            // 초과 시 안내창
            showAlert(Alert.AlertType.WARNING, "연장 불가", 
                    "이용 시간은 최대 3시간을 초과할 수 없습니다.\n" +
                    "(현재: " + currentTotalMinutes + "분 + 연장: " + addMinutes + "분 = " + expectedTotalMinutes + "분)");
        } else {
            // 허용 시 연장 진행
            boolean success = extendService.extendSeat(currentSeat.getId(), addMinutes);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "연장 완료", addMinutes + "분 연장되었습니다.");
                initialize(); // 화면 갱신
            } else {
                showAlert(Alert.AlertType.ERROR, "오류", "연장 처리에 실패했습니다.");
            }
        }
    }

    // ================= 퇴실 버튼 핸들러 =================
    @FXML
    public void handleCheckOut(ActionEvent event) {
        if (currentSeat == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("퇴실 확인");
        alert.setHeaderText("정말 퇴실하시겠습니까?");
        alert.setContentText("퇴실 후에는 좌석 이용이 종료됩니다.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            User user = LoginController.getCurrentLoggedInUser();
            boolean success = extendService.checkOut(user.getId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "퇴실 완료", "퇴실 처리가 완료되었습니다.\n안녕히 가세요.");
                handleGoHome(event); // 퇴실 성공 시 홈으로 이동
            } else {
                showAlert(Alert.AlertType.ERROR, "오류", "퇴실 처리 중 문제가 발생했습니다.");
            }
        }
    }

    // ================= 유틸리티 메서드 =================
    
    // 홈으로 이동
    @FXML
    public void handleGoHome(ActionEvent event) {
        try {
            if (timer != null) timer.stop();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/kiosk/MainMenuView.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 1400, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 총 이용 시간 계산해서 텍스트로 보여주기
    private void calculateAndShowTotalTime() {
        if (currentSeat.getStartTime() != null && currentSeat.getEndTime() != null) {
            long totalMinutes = ChronoUnit.MINUTES.between(currentSeat.getStartTime(), currentSeat.getEndTime());
            if (totalTimeText != null) {
                totalTimeText.setText("현재 총 이용 시간: " + totalMinutes + "분");
            }
        }
    }

    private void startRemainTimer() {
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (currentSeat != null && currentSeat.getEndTime() != null) {
                long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), currentSeat.getEndTime());
                if (secondsLeft <= 0) {
                    if (remainTimeText != null) remainTimeText.setText("00:00:00 (종료됨)");
                } else {
                    long h = secondsLeft / 3600;
                    long m = (secondsLeft % 3600) / 60;
                    long s = secondsLeft % 60;
                    if (remainTimeText != null) remainTimeText.setText(String.format("%02d:%02d:%02d", h, m, s));
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateStatusText(String info, String time) {
        if (seatInfoText != null) seatInfoText.setText(info);
        if (remainTimeText != null) remainTimeText.setText(time);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}