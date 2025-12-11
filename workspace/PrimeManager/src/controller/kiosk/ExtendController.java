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
import service.SeatService;     // 기존 서비스 재사용
import service.TimeLogService;  // 로그 저장 서비스 재사용

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class ExtendController {

    @FXML private Text seatInfoText;
    @FXML private Text remainTimeText;
    @FXML private Text totalTimeText;

    // ★ 기존에 잘 작동하는 서비스들을 가져옵니다.
    private SeatService seatService = new SeatService();
    private TimeLogService timeLogService = new TimeLogService();
    
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

        // 현재 내 좌석 정보 가져오기
        currentSeat = seatService.getSeatByUserId(currentUser.getId());

        if (currentSeat != null) {
            String seatInfo = String.format("%d층 %s번 좌석", currentSeat.getFloor(), currentSeat.getSeatNumber());
            updateStatusText(seatInfo, "계산 중...");
            
            calculateAndShowTotalTime();
            startRemainTimer();
        } else {
            updateStatusText("이용 좌석 없음", "00:00:00");
        }
    }

    // ================= 연장 버튼 핸들러 =================
    @FXML public void handleExtend30(ActionEvent event) { tryExtend(30); }
    @FXML public void handleExtend60(ActionEvent event) { tryExtend(60); }
    @FXML public void handleExtend90(ActionEvent event) { tryExtend(90); }
    @FXML public void handleExtend120(ActionEvent event) { tryExtend(120); }

    private void tryExtend(int addMinutes) {
        if (currentSeat == null) {
            showAlert(Alert.AlertType.ERROR, "오류", "좌석 정보가 없습니다.");
            return;
        }

        // 1. 시간 계산 (DB 데이터 기준)
        LocalDateTime start = currentSeat.getStartTime();
        LocalDateTime end = currentSeat.getEndTime();
        
        long currentTotalMinutes = ChronoUnit.MINUTES.between(start, end);
        long expectedTotalMinutes = currentTotalMinutes + addMinutes;

        // 2. 3시간 초과 체크
        if (expectedTotalMinutes > MAX_USAGE_MINUTES) {
            showAlert(Alert.AlertType.WARNING, "연장 불가", 
                    "총 이용 시간은 3시간을 초과할 수 없습니다.\n" +
                    "(현재: " + currentTotalMinutes + "분 + 추가: " + addMinutes + "분 = " + expectedTotalMinutes + "분)");
        } else {
            // 3. 연장 실행 (SeatService에 메서드 필요)
            boolean success = seatService.extendTime(currentSeat.getId(), addMinutes);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "연장 완료", addMinutes + "분 연장되었습니다.");
                // DB 정보 갱신을 위해 재조회
                currentSeat = seatService.getSeatById(currentSeat.getId());
                calculateAndShowTotalTime();
            } else {
                showAlert(Alert.AlertType.ERROR, "오류", "연장 처리에 실패했습니다.");
            }
        }
    }

    // ================= [핵심] 퇴실 버튼 핸들러 =================
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
            
            // 1. DB 좌석 반납 (Available로 변경)
            boolean success = seatService.checkOut(user.getId());

            if (success) {
                // ---------------------------------------------------------
                // ★ [DB 저장] 퇴실 로그 'E' 저장 (TimeLogService 사용)
                // ---------------------------------------------------------
                timeLogService.insertTimeLog(
                    user.getId(), 
                    user.getName(), 
                    "E", 
                    currentSeat.getSeatNumber()
                );

                showAlert(Alert.AlertType.INFORMATION, "퇴실 완료", "퇴실 처리가 완료되었습니다.\n안녕히 가세요.");
                handleGoHome(event); // 홈으로 이동
            } else {
                showAlert(Alert.AlertType.ERROR, "오류", "퇴실 처리 중 문제가 발생했습니다.");
            }
        }
    }

    // ================= 유틸리티 메서드 =================
    
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

    private void calculateAndShowTotalTime() {
        if (currentSeat != null && currentSeat.getStartTime() != null && currentSeat.getEndTime() != null) {
            long totalMinutes = ChronoUnit.MINUTES.between(currentSeat.getStartTime(), currentSeat.getEndTime());
            if (totalTimeText != null) {
                totalTimeText.setText("총 이용 예정 시간: " + totalMinutes + "분");
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