package controller.kiosk;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import service.TimeLogService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SeatController implements Initializable {

    // --- FXML 연결 ---
    @FXML private Text pageTitle;
    @FXML private ComboBox<String> floorComboBox;
    @FXML private ComboBox<String> seatTypeComboBox;
    @FXML private Text currentTimeText;
    @FXML private GridPane seatGrid;

    // --- 서비스 객체 ---
    private final SeatService seatService = new SeatService();
    private final ReservationService reservationService = new ReservationService();
    private final PenaltyService penaltyService = new PenaltyService();
    private final TimeLogService timeLogService = new TimeLogService();

    // --- 스타일 상수 ---
    private static final String STYLE_AVAILABLE = "-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-font-size: 24px; -fx-font-weight: bold;";
    private static final String STYLE_IN_USE = "-fx-background-color: #d3d3d3; -fx-text-fill: #555555; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-font-size: 24px; -fx-font-weight: bold;";
    private static final String STYLE_MAINTENANCE = "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-font-size: 24px; -fx-font-weight: bold;";
    private static final String STYLE_SELECTED = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;";
    private static final String STYLE_DISABLED_RESERVED = "-fx-background-color: #ffcccc; -fx-text-fill: #aaaaaa; -fx-border-color: #ffaaaa; -fx-border-width: 1px;"; // 예약된 시간 스타일

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        
        // 좌석 정보 조회
        Seat seat = seatService.getSeatBySeatNumber(seatNumStr);
        if (seat == null) {
            System.out.println("좌석 정보 없음: " + seatNumStr);
            return;
        }
        String status = seat.getStatus();

        // 1. 신고 모드 처리
        if (isReportMode) {
            handleReportFlow(seat.getSeatIndex(), seatNumStr);
            return; 
        }

        // 2. 토글 로직: 이미 선택된 버튼을 다시 누르면 선택 해제
        if (selectedButton == clickedButton) {
            selectedButton = null;
            updateButtonColor(clickedButton, LoginController.getCurrentLoggedInUser() != null ? LoginController.getCurrentLoggedInUser().getId() : -1);
            return;
        }

        // 3. 다른 버튼을 눌렀을 때 이전 버튼 선택 해제
        if (selectedButton != null) {
            Button prevButton = selectedButton;
            selectedButton = null;
            updateButtonColor(prevButton, LoginController.getCurrentLoggedInUser() != null ? LoginController.getCurrentLoggedInUser().getId() : -1);
        }

        // 4. 상태 체크
        if ("U".equals(status)) { 
            if (isMySeat(seatNumStr)) {
                showAlert(Alert.AlertType.INFORMATION, "내 좌석", "현재 이용 중인 좌석입니다.\n퇴실은 하단 '퇴실' 버튼을 이용해주세요.");
                return;
            } 
            // 입실 모드일 때는 사용 중인 좌석 선택 불가
            else if (!isReservationMode) {
                showAlert(Alert.AlertType.WARNING, "선택 불가", "이미 사용 중인 좌석입니다.");
                return;
            }
            // 예약 모드일 때는 사용 중이어도 선택 가능 (이후 시간 예약)
        }
        
        if ("M".equals(status)) {
            showAlert(Alert.AlertType.ERROR, "점검 중", "현재 점검 중인 좌석입니다.");
            return;
        }

        // 5. 로그인 체크
        User currentUser = LoginController.getCurrentLoggedInUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "로그인 필요", "이용하시려면 먼저 로그인해주세요.");
            return;
        }

        // 6. 버튼 선택 스타일 적용 (UI 즉시 반영)
        clickedButton.setStyle(STYLE_SELECTED);
        selectedButton = clickedButton;

        // 7. 다이얼로그 및 로직 처리 (UI 렌더링 후 실행)
        Platform.runLater(() -> {
            boolean processCompleted = false;
            
            if (isReservationMode) {
                // 예약 모드: Seat 객체를 넘겨야 종료 시간을 확인할 수 있음
                processCompleted = handleReservationFlow(seat, currentUser);
            } else {
                // 입실 모드
                processCompleted = handleCheckInFlow(seatNumStr, currentUser);
            }

            // 취소하거나 실패했을 경우 스타일 복구
            if (!processCompleted) {
                selectedButton = null; 
                updateButtonColor(clickedButton, currentUser.getId()); 
            }
        });
    }

    // =========================================================
    // 예약 로직 (수정됨: 예약된 시간 비활성화)
    // =========================================================
    private boolean handleReservationFlow(Seat seat, User user) {
        int seatId = seat.getId(); 
        if (seatId == 0) seatId = seatService.getSeatIdBySeatNumber(seat.getSeatNumber());

        // 1. 현재 사용 중인 시간 계산 (입실 상태 'U')
        int disableUntilHour = -1; 
        if ("U".equals(seat.getStatus()) && seat.getEndTime() != null) {
            disableUntilHour = seat.getEndTime().getHour();
            // 분 단위가 남아있으면 해당 시간대도 사용 중으로 침
            if (seat.getEndTime().getMinute() == 0) {
                disableUntilHour--; 
            }
        } else {
            // 사용 중이 아니라면 현재 시간 이전은 예약 불가
            disableUntilHour = LocalDateTime.now().getHour() - 1; 
        }

        // 2. [핵심] 다른 사람의 예약 시간('R') 가져오기 (예: [9, 10, 11])
        List<Integer> blockedHours = reservationService.getReservedHours(seatId);

        // 3. 입실 시간 선택 (비활성화 로직 전달)
        Integer startHour = showReservationStartTimeDialog(disableUntilHour, blockedHours);
        if (startHour == null) return false;
        
        // 4. 이용 시간 선택
        Integer durationHours = showReservationDurationDialog();
        if (durationHours == null) return false;
        
        // 5. [추가 검증] 선택한 시간부터 이용 시간 사이에 예약된 시간이 끼어있는지 체크
        for (int i = 0; i < durationHours; i++) {
            if (blockedHours.contains(startHour + i)) {
                showAlert(Alert.AlertType.WARNING, "예약 불가", (startHour + i) + "시에 이미 다른 예약이 있습니다.\n시간을 다시 선택해주세요.");
                return false;
            }
        }

        LocalDateTime startTime = LocalDateTime.now().withHour(startHour).withMinute(0).withSecond(0).withNano(0);

        boolean confirmed = showReservationConfirmDialog(seat.getSeatNumber(), startTime, durationHours);
        if (confirmed) {
            // Service 호출 (DB 저장)
            boolean success = reservationService.makeReservation(user.getId(), seatId, startTime, durationHours);
            
            if (success) {
                timeLogService.insertTimeLog(user.getId(), user.getName(), "R", seat.getSeatNumber());
                showAlert(Alert.AlertType.INFORMATION, "예약 완료", "예약이 완료되었습니다.");
                refreshSeatMap();
                selectedButton = null;
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "예약 실패", "오류가 발생했습니다.");
                return false;
            }
        }
        return false;
    }

    // =========================================================
    // 입실 로직
    // =========================================================
    private boolean handleCheckInFlow(String seatNumStr, User user) { 
        Seat mySeat = seatService.getSeatByUserId(user.getId());
        if (mySeat != null) {
            showAlert(Alert.AlertType.WARNING, "입실 불가", "이미 이용 중인 좌석이 있습니다 (" + mySeat.getSeatNumber() + "번).");
            return false;
        }
        
        int seatId = seatService.getSeatIdBySeatNumber(seatNumStr); 
        if (seatId == 0) {
            showAlert(Alert.AlertType.ERROR, "오류", "유효한 좌석 ID를 찾을 수 없습니다.");
            return false;
        }
        
        // 입실은 현재 시간 기준이므로 예약 정보 조회 불필요 (null 전달)
        Integer durationMinutes = showCheckInTimeDialog();
        if (durationMinutes == null) return false;

        boolean confirmed = showCheckInConfirmDialog(seatNumStr, durationMinutes);
        if (confirmed) {
            boolean success = seatService.checkIn(seatId, user.getId(), durationMinutes); 
            if (success) {
                timeLogService.insertTimeLog(user.getId(), user.getName(), "I", seatNumStr);
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

    @FXML
    public void handleSeatExit(ActionEvent event) {
        if (isReportMode) {
            showAlert(Alert.AlertType.WARNING, "기능 제한", "신고 모드에서는 퇴실 기능을 사용할 수 없습니다.");
            return;
        }
        
        User currentUser = LoginController.getCurrentLoggedInUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "알림", "로그인이 필요합니다.");
            return;
        }
        
        Seat currentSeat = seatService.getSeatByUserId(currentUser.getId());
        if (currentSeat == null) {
            showAlert(Alert.AlertType.WARNING, "알림", "현재 이용 중인 좌석이 없습니다.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("퇴실 확인");
        alert.setHeaderText(currentSeat.getSeatNumber() + "번 좌석을 퇴실하시겠습니까?");
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (seatService.checkOut(currentUser.getId())) {
                timeLogService.insertTimeLog(currentUser.getId(), currentUser.getName(), "E", String.valueOf(currentSeat.getSeatNumber()));
                showAlert(Alert.AlertType.INFORMATION, "퇴실 완료", "안녕히 가세요.");
                refreshSeatMap();
            } else {
                showAlert(Alert.AlertType.ERROR, "오류", "퇴실 실패");
            }
        }
    }

    private void handleReportFlow(int seatId, String seatNumStr) {
        User reporter = LoginController.getCurrentLoggedInUser();
        if (reporter == null) {
            showAlert(Alert.AlertType.WARNING, "알림", "신고하려면 먼저 로그인해야 합니다.");
            return;
        }
        int reporterId = reporter.getId(); 
        if (reporterId == 0) {
            showAlert(Alert.AlertType.ERROR, "오류", "로그인 정보 오류");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("좌석 신고");
        dialog.setHeaderText(seatNumStr + "번 좌석 신고");
        dialog.setContentText("사유:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (!reason.trim().isEmpty()) {
                String resultMsg = penaltyService.insertPenalty(reporterId, reason, seatId);
                if ("SUCCESS".equals(resultMsg)) {
                     timeLogService.insertTimeLog(reporterId, reporter.getName(), "R", seatNumStr);
                    showAlert(Alert.AlertType.INFORMATION, "접수 완료", "신고가 접수되었습니다.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "접수 실패", "오류: " + resultMsg);
                }
            }
        });
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
            String seatNumber = btn.getText();
            Seat seat = seatService.getSeatBySeatNumber(seatNumber);
            
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
        return seat != null && String.valueOf(seat.getSeatNumber()).equals(seatNumStr); 
    }

    // --- 다이얼로그 메서드들 ---
    
    private Integer showCheckInTimeDialog() {
        return showGridDialog("시간 선택", "이용 시간을 선택하세요.", 30, 180, 30, "분", -1, null);
    }
    
    private Integer showReservationStartTimeDialog(int disableUntilHour, List<Integer> blockedHours) {
        return showGridDialog("입실 시간 선택", "입실할 시간을 선택하세요.", 9, 22, 1, "시", disableUntilHour, blockedHours);
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
    
    private Integer showGridDialog(String title, String header, int start, int end, int step, String suffix, int disableUntilHour, List<Integer> blockedHours) {
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
            
            boolean isBlocked = false;
            
            // 1. 과거 시간 차단
            if (i <= disableUntilHour && "시".equals(suffix)) {
                isBlocked = true;
            }
            // 2. 예약된 시간 차단
            if (blockedHours != null && blockedHours.contains(i) && "시".equals(suffix)) {
                isBlocked = true;
                btn.setStyle(STYLE_DISABLED_RESERVED); 
            }

            if (isBlocked) {
                btn.setDisable(true);
                if (!btn.getStyle().contains("#ffcccc")) {
                    btn.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #aaaaaa;");
                }
            } else {
                int val = i;
                btn.setOnAction(e -> { result[0] = val; dialog.setResult(val); dialog.close(); });
            }

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
            if(currentTimeText != null) {
                currentTimeText.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }
}