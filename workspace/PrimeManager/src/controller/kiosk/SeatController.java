package controller.kiosk;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.Seat;
import model.User;
import service.SeatService;
import service.ReservationService;
import service.PenaltyService;
import service.TimeLogService; // ★ TimeLogService 필요

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SeatController {

    // --- FXML 연결 ---
    @FXML private Text pageTitle;
    @FXML private ComboBox<String> floorComboBox;
    @FXML private ComboBox<String> seatTypeComboBox;
    @FXML private Text currentTimeText;
    @FXML private GridPane seatGrid;

    // --- 서비스 객체 ---
    private SeatService seatService = new SeatService();
    private ReservationService reservationService = new ReservationService();
    private PenaltyService penaltyService = new PenaltyService();
    
    // ★ DB 기록용 서비스
    private TimeLogService timeLogService = new TimeLogService(); 

    // --- 스타일 상수 ---
    private static final String STYLE_AVAILABLE = "-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-font-size: 30px; -fx-font-weight: bold;";
    private static final String STYLE_IN_USE = "-fx-background-color: #d3d3d3; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-font-size: 30px; -fx-font-weight: bold;";
    private static final String STYLE_MAINTENANCE = "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-font-size: 30px; -fx-font-weight: bold;";
    private static final String STYLE_SELECTED = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;";

    // --- 모드 관리 ---
    private static boolean isReservationMode = false;
    private static boolean isReportMode = false;

    private Button selectedButton = null; 
    private Timeline clock; 

    public static void setReservationMode(boolean mode) {
        isReservationMode = mode;
        if (mode) isReportMode = false;
    }

    public static void setReportMode(boolean mode) {
        isReportMode = mode;
        if (mode) isReservationMode = false;
    }

    @FXML
    public void initialize() {
        startClock();

        if (pageTitle != null) {
            if (isReportMode) {
                pageTitle.setText("좌석 신고");
            } else {
                pageTitle.setText(isReservationMode ? "좌석 예약" : "좌석 입실");
            }
        }

        if (floorComboBox != null) {
            floorComboBox.getItems().setAll("4층", "7층");
            floorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) updateSeatTypeComboBox(newVal);
            });
            floorComboBox.getSelectionModel().select("4층");
        }
        
        refreshSeatMap();
    }

    private void updateSeatTypeComboBox(String floor) {
        if (seatTypeComboBox == null) return;
        seatTypeComboBox.getItems().clear();

        if ("4층".equals(floor)) {
            seatTypeComboBox.getItems().addAll("개인좌석", "단체좌석");
        } else if ("7층".equals(floor)) {
            seatTypeComboBox.getItems().addAll("단체좌석");
        }
        seatTypeComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleGoHome(ActionEvent event) {
        try {
            if (clock != null) clock.stop();
            isReportMode = false;
            isReservationMode = false;

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/kiosk/MainMenuView.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1400, 800);
            stage.setTitle("좌석 예약 시스템");
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "오류", "메인 메뉴로 이동할 수 없습니다.");
        }
    }

    @FXML
    public void handleSeatEnter(ActionEvent event) {
        String floor = floorComboBox.getValue();
        String type = seatTypeComboBox.getValue();

        if (floor == null || type == null) {
            showAlert(Alert.AlertType.WARNING, "선택 필요", "층과 좌석 종류를 선택해주세요.");
            return;
        }

        String fxmlPath = "";
        if ("4층".equals(floor)) {
            if ("개인좌석".equals(type)) fxmlPath = "/view/kiosk/SeatMapView4Fprivate.fxml";
            else if ("단체좌석".equals(type)) fxmlPath = "/view/kiosk/SeatMapView4Fgroup.fxml";
        } else if ("7층".equals(floor)) {
            if ("단체좌석".equals(type)) fxmlPath = "/view/kiosk/SeatMapView7Fgroup.fxml";
        }

        if (!fxmlPath.isEmpty()) {
            try {
                if (clock != null) clock.stop();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1400, 800);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "이동 실패", "화면 이동 중 오류 발생: " + fxmlPath);
            }
        }
    }

    @FXML
    public void handleSeatSelection(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String seatNumStr = clickedButton.getText();
        int seatId;
        try {
            seatId = Integer.parseInt(seatNumStr);
        } catch (NumberFormatException e) {
            return;
        }

        Seat seat = seatService.getSeatById(seatId);
        if (seat == null) return;
        String status = seat.getStatus();

        // [CASE 1] 신고 모드
        if (isReportMode) {
            handleReportFlow(seatId, seatNumStr);
            return; 
        }

        // [CASE 2] 일반 모드
        if (selectedButton == clickedButton) {
            clickedButton.setStyle(STYLE_AVAILABLE);
            selectedButton = null;
            return;
        }
        if (selectedButton != null) {
            selectedButton.setStyle(STYLE_AVAILABLE);
            selectedButton = null;
        }

        if ("U".equals(status)) { 
            if (isMySeat(seatNumStr)) {
                showAlert(Alert.AlertType.INFORMATION, "내 좌석", "현재 이용 중인 좌석입니다.\n퇴실은 하단 '퇴실' 버튼을 이용해주세요.");
            } else {
                showAlert(Alert.AlertType.WARNING, "선택 불가", "이미 사용 중인 좌석입니다.");
            }
            return;
        }
        if ("M".equals(status)) {
            showAlert(Alert.AlertType.ERROR, "점검 중", "현재 점검 중인 좌석입니다.");
            return;
        }

        User currentUser = LoginController.getCurrentLoggedInUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "로그인 필요", "이용하시려면 먼저 로그인해주세요.");
            return;
        }
        if (currentUser.getPenaltyCount() >= 3) {
            showAlert(Alert.AlertType.ERROR, "이용 제한", "벌점 누적으로 인해 이용이 제한되었습니다.");
            return;
        }

        clickedButton.setStyle(STYLE_SELECTED);
        selectedButton = clickedButton;

        boolean processCompleted = false;
        if (isReservationMode) {
            processCompleted = handleReservationFlow(seatNumStr, currentUser);
        } else {
            processCompleted = handleCheckInFlow(seatNumStr, currentUser);
        }

        if (!processCompleted) {
            clickedButton.setStyle(STYLE_AVAILABLE);
            selectedButton = null;
        }
    }

    private void handleReportFlow(int seatId, String seatNumStr) {
        User reporter = LoginController.getCurrentLoggedInUser();
        
        if (reporter == null) {
            showAlert(Alert.AlertType.WARNING, "알림", "신고하려면 먼저 로그인해야 합니다.");
            return;
        }
        
        if (reporter.getId() == 0) {
            showAlert(Alert.AlertType.ERROR, "오류", "로그인 정보 오류: 학번이 0입니다.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("좌석 신고");
        dialog.setHeaderText(seatNumStr + "번 좌석에 대해 신고하시겠습니까?");
        dialog.setContentText("신고 사유를 입력하세요:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "경고", "신고 사유를 입력해야 합니다.");
                return;
            }

            String resultMsg = penaltyService.insertPenalty(reporter.getId(), reason, seatId);
            
            if ("SUCCESS".equals(resultMsg)) {
                showAlert(Alert.AlertType.INFORMATION, "접수 완료", "신고가 정상적으로 접수되었습니다.");
            } else {
                showAlert(Alert.AlertType.ERROR, "접수 실패", "시스템 오류가 발생했습니다.\n\n" + resultMsg);
            }
        });
    }

    // =========================================================
    // ★ [핵심] 입실 로직: 성공 시 "I" 저장
    // =========================================================
    private boolean handleCheckInFlow(String seatNumStr, User user) {
        Seat mySeat = seatService.getSeatByUserId(user.getId());
        if (mySeat != null) {
            showAlert(Alert.AlertType.WARNING, "입실 불가", "이미 이용 중인 좌석이 있습니다 (" + mySeat.getSeatNumber() + "번).");
            return false;
        }
        int seatId = Integer.parseInt(seatNumStr); 
        Integer durationMinutes = showCheckInTimeDialog();
        if (durationMinutes == null) return false;

        boolean confirmed = showCheckInConfirmDialog(seatNumStr, durationMinutes);
        if (confirmed) {
            boolean success = seatService.checkIn(seatId, user.getId(), durationMinutes);
            if (success) {
                // -------------------------------------------------------------
                // ★ [저장] 입실(I)
                // -------------------------------------------------------------
                timeLogService.insertTimeLog(
                    user.getId(),     
                    user.getName(),   
                    "I",              // 타입: I
                    seatNumStr        
                );

                showAlert(Alert.AlertType.INFORMATION, "입실 완료", seatNumStr + "번 좌석에 입실되었습니다.");
                refreshSeatMap();
                selectedButton = null;
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "오류", "입실 처리에 실패했습니다.");
                return false;
            }
        }
        return false;
    }

    // =========================================================
    // ★ [핵심] 퇴실 로직: 성공 시 "E" 저장
    // =========================================================
    @FXML
    public void handleSeatExit(ActionEvent event) {
        // ★ [진단 1] 이 줄이 콘솔에 안 뜨면 버튼 연결이 끊긴 것입니다.
        System.out.println("🚨 [진단] 퇴실 버튼이 클릭되었습니다! 코드가 시작됩니다."); 

        if (isReportMode) {
            showAlert(Alert.AlertType.WARNING, "기능 제한", "신고 모드에서는 퇴실 기능을 사용할 수 없습니다.");
            return;
        }
        
        User currentUser = LoginController.getCurrentLoggedInUser();
        if (currentUser == null) {
            System.out.println("❌ [오류] 로그인 유저 없음");
            showAlert(Alert.AlertType.WARNING, "알림", "로그인이 필요합니다.");
            return;
        }
        
        Seat currentSeat = seatService.getSeatByUserId(currentUser.getId());
        if (currentSeat == null) {
            System.out.println("❌ [오류] 현재 이용 중인 좌석 없음");
            showAlert(Alert.AlertType.WARNING, "알림", "현재 이용 중인 좌석이 없습니다.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("퇴실 확인");
        alert.setHeaderText(currentSeat.getSeatNumber() + "번 좌석을 퇴실하시겠습니까?");
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("👉 [진행] 팝업 확인 누름. 반납 시도...");
            
            if (seatService.checkOut(currentUser.getId())) {
                System.out.println("👉 [성공] 좌석 반납 성공! DB 로그 저장 시작...");
                
                // ★ DB 저장
                timeLogService.insertTimeLog(
                    currentUser.getId(),               
                    currentUser.getName(),             
                    "E",                               
                    String.valueOf(currentSeat.getSeatNumber()) 
                );
                
                System.out.println("👉 [완료] DB 로그 저장 코드 통과함");

                showAlert(Alert.AlertType.INFORMATION, "퇴실 완료", "안녕히 가세요.");
                refreshSeatMap();
            } else {
                System.out.println("❌ [실패] 반납 로직(checkOut) 실패");
                showAlert(Alert.AlertType.ERROR, "오류", "퇴실 실패");
            }
        }
    }

    private boolean handleReservationFlow(String seatNumStr, User user) {
        int seatId = Integer.parseInt(seatNumStr);
        Integer startHour = showReservationStartTimeDialog();
        if (startHour == null) return false;
        Integer durationHours = showReservationDurationDialog();
        if (durationHours == null) return false;
        LocalDateTime startTime = LocalDateTime.now().withHour(startHour).withMinute(0).withSecond(0).withNano(0);

        boolean confirmed = showReservationConfirmDialog(seatNumStr, startTime, durationHours);
        if (confirmed) {
            boolean success = reservationService.makeReservation(user.getId(), seatId, startTime, durationHours);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "예약 완료", "예약이 완료되었습니다.");
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "예약 실패", "예약 중 오류가 발생했습니다.");
                return false;
            }
        }
        return false;
    }

    private void refreshSeatMap() {
        User user = LoginController.getCurrentLoggedInUser();
        int currentUserId = (user != null) ? user.getId() : -1;
        if (seatGrid != null) {
            for (Node node : seatGrid.getChildren()) {
                if (node instanceof HBox) {
                    for (Node child : ((HBox) node).getChildren()) {
                        if (child instanceof Button) updateButtonColor((Button) child, currentUserId);
                    }
                } else if (node instanceof Button) {
                    updateButtonColor((Button) node, currentUserId);
                }
            }
        }
    }

    private void updateButtonColor(Button btn, int myUserId) {
        try {
            int seatId = Integer.parseInt(btn.getText());
            Seat seat = seatService.getSeatById(seatId); 
            if (seat == null) return;
            if ("M".equals(seat.getStatus())) {
                btn.setStyle(STYLE_MAINTENANCE);
            } else if ("U".equals(seat.getStatus())) {
                btn.setStyle(STYLE_IN_USE); 
            } else {
                btn.setStyle(STYLE_AVAILABLE);
            }
            if (selectedButton == btn) {
                btn.setStyle(STYLE_SELECTED);
            }
        } catch (NumberFormatException ignored) {}
    }

    private boolean isMySeat(String seatNumStr) {
        User user = LoginController.getCurrentLoggedInUser();
        if (user == null) return false;
        Seat seat = seatService.getSeatByUserId(user.getId());
        return seat != null && String.valueOf(seat.getId()).equals(seatNumStr);
    }

    private Integer showCheckInTimeDialog() {
        return showGridDialog("시간 선택", "이용 시간을 선택하세요.", 30, 180, 30, "분");
    }
    private Integer showReservationStartTimeDialog() {
        return showGridDialog("입실 시간 선택", "입실할 시간을 선택하세요.", 9, 16, 1, "시");
    }
    private Integer showReservationDurationDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("이용 시간 선택");
        dialog.setHeaderText("이용할 시간을 선택하세요.");
        dialog.initStyle(StageStyle.UTILITY);
        HBox hbox = new HBox(15);
        hbox.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: white;");
        final Integer[] result = {null};
        for (int i = 1; i <= 3; i++) {
            Button btn = new Button(i + "시간");
            btn.setPrefSize(80, 50);
            int val = i;
            btn.setOnAction(e -> { result[0] = val; dialog.setResult(val); dialog.close(); });
            hbox.getChildren().add(btn);
        }
        dialog.getDialogPane().setContent(hbox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
        return result[0];
    }
    private Integer showGridDialog(String title, String header, int start, int end, int step, String suffix) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.initStyle(StageStyle.UTILITY);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20; -fx-background-color: white;");
        final Integer[] result = {null};
        int col = 0, row = 0;
        for (int i = start; i <= end; i += step) {
            Button btn = new Button(i + suffix);
            btn.setPrefSize(70, 40);
            int val = i;
            btn.setOnAction(e -> { result[0] = val; dialog.setResult(val); dialog.close(); });
            grid.add(btn, col, row);
            col++;
            if (col > 3) { col = 0; row++; }
        }
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
        return result[0];
    }
    private boolean showCheckInConfirmDialog(String seatNum, int minutes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("입실 확인");
        alert.setHeaderText(null);
        alert.setContentText(seatNum + "번 좌석에 입실하시겠습니까?\n이용 시간: " + minutes + "분");
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }
    private boolean showReservationConfirmDialog(String seatNum, LocalDateTime startTime, int duration) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("예약 확인");
        alert.setHeaderText(null);
        String timeStr = startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        String content = "선택한 좌석: " + seatNum + "\n입실 시간: " + timeStr + "\n이용시간 :" + duration + "시간\n\n입실하시겠습니까?";
        alert.setContentText(content);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void startClock() {
        if (clock != null) clock.stop();
        clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            currentTimeText.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }
}