package controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent; 
import javafx.animation.KeyFrame; 
import javafx.animation.Timeline; 
import javafx.util.Duration;     
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional; 
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList; 
import javafx.concurrent.Task; 
import javafx.application.Platform; 
import java.net.URL;
import java.util.ResourceBundle;

import service.ReservationService;
import service.SeatService; 
import model.Seat;
import model.Reservation;
import model.Reservation.ReservationStatus; 


public class SeatController implements javafx.fxml.Initializable {

    // ------------------------------------------------
    // NEW: 최대 총 이용 시간 제한 (10시간 = 600분)
    // ------------------------------------------------
    private static final int MAX_TOTAL_DURATION_MINUTES = 600; 

    private final ReservationService reservationService = new ReservationService();
    private final SeatService seatService = new SeatService(); 
    
    @FXML private GridPane seatGrid;
    @FXML private Text selectedSeatNumber;
    @FXML private Text remainingTimeText;
    @FXML private Button checkInButton;
    @FXML private Button reserveButton;
    @FXML private Button checkOutButton;
    @FXML private Button extendButton;
    @FXML private Button extend60Button;

    // ------------------------------------------------
    // 상태 변수
    // ------------------------------------------------
    private int selectedSeatId = -1; 
    private final String currentUserId = "C_Tester001"; 
    private Timeline reservationTimeline;
    private final Map<Integer, Button> seatButtons = new HashMap<>(); 
    private int reserveDurationMinutes = 60; 
    
    // ------------------------------------------------
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSeatsAsynchronously(); 
        
        reservationTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRemainingTime()));
        reservationTimeline.setCycleCount(Timeline.INDEFINITE);
        reservationTimeline.play();
    }
    
    private void loadSeatsAsynchronously() {
        Task<List<Seat>> loadTask = new Task<>() {
            @Override
            protected List<Seat> call() throws Exception {
                return seatService.getAllSeats(); 
            }

            @Override
            protected void succeeded() {
                List<Seat> seats = getValue(); 
                
                Platform.runLater(() -> {
                    seatGrid.getChildren().clear(); 
                    for (Seat seat : seats) {
                        Button seatButton = createSeatButton(seat);
                        seatButtons.put(seat.getId(), seatButton);
                        seatGrid.add(seatButton, seat.getCol(), seat.getRow());
                    }
                    updateUIForUserStatus();
                });
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                System.err.println("ERROR: 좌석 데이터 로드 실패: " + e.getMessage());
                Platform.runLater(() -> showAlert("오류", "좌석 정보 로드에 실패했습니다: " + e.getMessage()));
            }
        };

        new Thread(loadTask).start();
    }
    
    private Button createSeatButton(Seat seat) {
        Button button = new Button(seat.getNumber());
        button.setUserData(seat.getId()); 
        
        button.setStyle("-fx-min-width: 60; -fx-min-height: 40; -fx-background-color: #90a4ae;");
        button.setOnAction(this::handleSeatSelection); 
        
        return button;
    }
    
    private void updateUIForUserStatus() {
        if (selectedSeatId != -1) {
            Button selectedButton = seatButtons.get(selectedSeatId);
            selectedSeatNumber.setText(selectedButton != null ? selectedButton.getText() : "선택됨");
        } else {
            selectedSeatNumber.setText("선택 전");
        }
        
        Reservation userActiveReservation = reservationService.findActiveReservationByUserId(currentUserId);

        // 버튼 초기 상태 설정
        checkInButton.setDisable(true);
        checkInButton.setText("입실하기"); 
        checkOutButton.setDisable(true);
        extendButton.setDisable(true);
        extend60Button.setDisable(true);
        reserveButton.setDisable(true);

        // 1. 사용자의 활성 예약/이용이 있는 경우
        if (userActiveReservation != null) {
            
            if (userActiveReservation.getStatus() == ReservationStatus.PENDING) {
                // Case A: PENDING 예약 상태 (기존 입실)
                checkInButton.setDisable(false); 
                reserveButton.setText("예약 취소");
                reserveButton.setDisable(false); 
                remainingTimeText.setText("입실 대기중");
                
            } else if (userActiveReservation.getStatus() == ReservationStatus.IN_USE) {
                // Case B: IN_USE 상태
                reserveButton.setText("좌석 예약"); 
                reserveButton.setDisable(true); 
                checkOutButton.setDisable(false);
                extendButton.setDisable(false);
                extend60Button.setDisable(false);
            }
        } 
        // 2. 사용자의 활성 예약/이용이 없는 경우
        else {
            reserveButton.setText("좌석 예약 (" + (reserveDurationMinutes / 60) + "시간)"); 
            remainingTimeText.setText("00:00:00");
            
            if (selectedSeatId != -1) {
                Reservation seatReservation = reservationService.getActiveReservationBySeatId(selectedSeatId);
                
                if (seatReservation == null) {
                    // Case C: 빈 좌석 선택 (초록색 좌석)
                    reserveButton.setDisable(false); 
                    checkInButton.setText("즉시 입실"); 
                    checkInButton.setDisable(false); 
                } else {
                    // 선택된 좌석이 이미 사용/예약 중 (빨강/노랑)
                    reserveButton.setDisable(true);
                    checkInButton.setDisable(true);
                }
            }
        }
        
        updateSeatColorsAsynchronously(); 
    }

    // ------------------------------------------------
    // 이벤트 핸들러 
    // ------------------------------------------------
    
    @FXML
    private void handleSeatSelection(ActionEvent event) { 
        Button selectedButton = (Button) event.getSource();
        
        try {
            selectedSeatId = (int) selectedButton.getUserData(); 
        } catch (Exception e) {
            showAlert("오류", "좌석 정보 로드 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        
        updateUIForUserStatus(); 
    }
    
    @FXML
    private void handleReservation(ActionEvent event) {
        Reservation userActiveReservation = reservationService.findActiveReservationByUserId(currentUserId);

        if (userActiveReservation != null && userActiveReservation.getStatus() == ReservationStatus.PENDING) {
            // 1. 예약 취소 로직
            if (showAlertConfirmation("예약 취소", "현재 예약된 좌석을 취소하시겠습니까?")) {
                if (reservationService.cancelReservation(currentUserId)) {
                    showAlert("성공", "예약이 취소되었습니다.");
                } else {
                    showAlert("실패", "예약 취소에 실패했습니다.");
                }
            }
        } else {
            // 2. 신규 예약 로직 (PENDING 예약 생성)
            if (selectedSeatId == -1) {
                showAlert("경고", "예약할 좌석을 선택해 주십시오.");
                return;
            }
            
            Reservation seatReservation = reservationService.getActiveReservationBySeatId(selectedSeatId);
            if (seatReservation != null) {
                showAlert("경고", selectedSeatNumber.getText() + "번 좌석은 현재 이용 중이거나 예약되어 있습니다.");
                return;
            }
            
            Optional<Integer> duration = showDurationSelectionDialog();
            if (duration.isEmpty()) {
                return;
            }
            
            int finalDuration = duration.get();
            reserveDurationMinutes = finalDuration; 

            if (showAlertConfirmation("좌석 예약", selectedSeatNumber.getText() + "번 좌석을 " + (finalDuration / 60) + "시간 예약하시겠습니까?")) {
                if (reservationService.reserveSeat(currentUserId, selectedSeatId, finalDuration)) {
                    showAlert("성공", selectedSeatNumber.getText() + "번 좌석 예약이 완료되었습니다. 10분 내에 입실해 주십시오.");
                } else {
                    showAlert("실패", "예약에 실패했습니다. (이미 활성 예약이 있거나 로직 오류)");
                }
            }
        }
        updateUIForUserStatus();
    }
    
    @FXML
    private void handleCheckIn(ActionEvent event) {
        Reservation userActiveReservation = reservationService.findActiveReservationByUserId(currentUserId);
        
        // Case 1: PENDING 예약이 있어 '입실하기' 버튼을 누른 경우 (기존 로직)
        if (userActiveReservation != null && userActiveReservation.getStatus() == ReservationStatus.PENDING) {
            if (reservationService.checkIn(currentUserId)) {
                showAlert("입실 완료", "좌석에 입실이 완료되었습니다. 즐거운 시간 되십시오.");
            } else {
                showAlert("입실 실패", "입실 처리에 실패했습니다. 다시 시도해 주십시오.");
            }
        } 
        // Case 2: 빈 좌석을 선택하고 '즉시 입실' 버튼을 누른 경우 (새로운 로직)
        else if (userActiveReservation == null && selectedSeatId != -1) {
            Reservation seatReservation = reservationService.getActiveReservationBySeatId(selectedSeatId);
            
            if (seatReservation != null) {
                showAlert("경고", "선택된 좌석은 이미 사용 중이거나 예약되어 있습니다.");
                return;
            }

            Optional<Integer> duration = showDurationSelectionDialog();
            if (duration.isEmpty()) {
                return;
            }
            
            int finalDuration = duration.get();
            
            // NEW: 즉시 입실 시에도 최대 시간 초과 체크
            if (finalDuration > MAX_TOTAL_DURATION_MINUTES) {
                showAlert("오류", "최대 이용 시간(" + (MAX_TOTAL_DURATION_MINUTES / 60) + "시간)을 초과하는 예약은 불가능합니다.");
                return;
            }

            if (showAlertConfirmation("즉시 입실 확인", selectedSeatNumber.getText() + "번 좌석을 " + (finalDuration / 60) + "시간 사용 시작하시겠습니까?")) {
                // 1. 예약 생성 (PENDING 상태로 생성됨)
                if (reservationService.reserveSeat(currentUserId, selectedSeatId, finalDuration)) {
                    // 2. 생성 즉시 IN_USE로 변경 (입실 처리)
                    if (reservationService.checkIn(currentUserId)) {
                        showAlert("즉시 입실 완료", selectedSeatNumber.getText() + "번 좌석 이용을 시작합니다.");
                    } else {
                        showAlert("입실 실패", "좌석 이용 시작에 실패했습니다.");
                    }
                } else {
                    showAlert("입실 실패", "좌석 예약에 실패했습니다.");
                }
            }
        }
        // Case 3: 예외적인 상태
        else {
            showAlert("경고", "입실 가능한 상태가 아닙니다. 좌석을 선택하거나 PENDING 예약을 확인하십시오.");
        }
        
        updateUIForUserStatus();
    }
    
    @FXML
    private void handleCheckOut(ActionEvent event) {
        Reservation userActiveReservation = reservationService.findActiveReservationByUserId(currentUserId);

        if (userActiveReservation == null || userActiveReservation.getStatus() != ReservationStatus.IN_USE) {
            showAlert("경고", "현재 이용 중인 좌석이 없습니다.");
            return;
        }
        
        if (showAlertConfirmation("퇴실 확인", "현재 이용을 종료하고 퇴실하시겠습니까?")) {
            if (reservationService.checkOut(userActiveReservation.getReservationId())) { 
                showAlert("퇴실 완료", "이용해 주셔서 감사합니다.");
                remainingTimeText.setText("00:00:00");
            } else {
                showAlert("퇴실 실패", "퇴실 처리에 실패했습니다. 관리자에게 문의하십시오.");
            }
        }
        updateUIForUserStatus();
    }
    
    @FXML
    private void handleExtension(ActionEvent event) {
        handleExtend(30); 
    }
    
    @FXML
    private void handleExtension60(ActionEvent event) {
        handleExtend(60); 
    }
    
    private void handleExtend(int minutes) {
        
        Reservation userActiveReservation = reservationService.findActiveReservationByUserId(currentUserId);
        
        if (userActiveReservation == null || userActiveReservation.getStatus() != ReservationStatus.IN_USE) {
            showAlert("경고", "연장할 수 있는 이용 중인 좌석이 없습니다.");
            return;
        }

        // 🚨 1. 개별 연장 시간 최대 1시간(60분) 제한 로직 (기존 유지)
        if (minutes > 60 || minutes <= 0) {
            showAlert("오류", "연장 시간은 1분 이상 60분 이하만 가능합니다.");
            return;
        }
        
        // 🚨 2. 총 이용 시간 최대 상한선(10시간) 체크 (NEW)
        int currentDuration = userActiveReservation.getDurationMinutes();
        if (currentDuration + minutes > MAX_TOTAL_DURATION_MINUTES) {
            showAlert("연장 오류", "최대 이용 시간(" + (MAX_TOTAL_DURATION_MINUTES / 60) + "시간)을 초과할 수 없습니다. 현재 이용 시간: " + (currentDuration / 60) + "시간");
            return;
        }
        
        int result = reservationService.extendReservation(userActiveReservation.getReservationId(), minutes);
        
        if (result == 1) {
            showAlert("연장 성공", minutes + "분 연장이 완료되었습니다.");
        } else {
            showAlert("연장 실패", "연장에 실패했습니다. 관리자에게 문의하십시오. (코드: " + result + ")");
        }
        updateUIForUserStatus();
    }


    // ------------------------------------------------
    // 타이머 및 UI 헬퍼
    // ------------------------------------------------

    /**
     * 예약 시간을 선택하는 다이얼로그를 표시합니다.
     * @return 선택된 시간(분) 또는 Optional.empty()
     */
    private Optional<Integer> showDurationSelectionDialog() {
        List<Integer> choices = List.of(60, 120, 180); // 1시간, 2시간, 3시간 (분 단위)
        
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(60, choices);
        dialog.setTitle("시간 선택");
        dialog.setHeaderText("좌석 사용 시간을 선택해 주십시오.");
        dialog.setContentText("시간 (분):");

        ComboBox<Integer> comboBox = (ComboBox<Integer>) dialog.getDialogPane().lookup(".combo-box");
        if (comboBox != null) {
            comboBox.setConverter(new javafx.util.StringConverter<Integer>() {
                @Override
                public String toString(Integer duration) {
                    return (duration / 60) + "시간";
                }

                @Override
                public Integer fromString(String string) {
                    return null;
                }
            });
        }
        
        Optional<Integer> result = dialog.showAndWait();
        return result.isPresent() ? result : Optional.empty();
    }

    private void updateRemainingTime() {
        Reservation userActiveReservation = reservationService.findActiveReservationByUserId(currentUserId);
        
        if (userActiveReservation != null && userActiveReservation.getStatus() == ReservationStatus.IN_USE) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expectedEnd = userActiveReservation.getExpectedEndTime();
            
            long secondsRemaining = ChronoUnit.SECONDS.between(now, expectedEnd);
            
            if (secondsRemaining <= 0) {
                remainingTimeText.setText("00:00:00");
                if (secondsRemaining < -5) { 
                    reservationService.checkOut(userActiveReservation.getReservationId());
                    updateUIForUserStatus();
                    showAlert("시간 만료", "이용 시간이 만료되어 자동 퇴실 처리되었습니다.");
                }
            } else {
                long hours = secondsRemaining / 3600;
                long minutes = (secondsRemaining % 3600) / 60;
                long seconds = secondsRemaining % 60;
                
                remainingTimeText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        } else {
             remainingTimeText.setText("00:00:00");
        }
        
        if (LocalDateTime.now().getSecond() % 5 == 0) { 
             updateSeatColorsAsynchronously();
        }
    }
    
    private void updateSeatColorsAsynchronously() {
        
        List<Integer> allSeatIds = new ArrayList<>(seatButtons.keySet()); 
        
        Task<Map<Integer, Reservation>> colorUpdateTask = new Task<>() {
            @Override
            protected Map<Integer, Reservation> call() throws Exception {
                Map<Integer, Reservation> seatStatusMap = new HashMap<>();
                for (Integer seatId : allSeatIds) {
                    Reservation status = reservationService.getActiveReservationBySeatId(seatId);
                    seatStatusMap.put(seatId, status);
                }
                return seatStatusMap;
            }

            @Override
            protected void succeeded() {
                Map<Integer, Reservation> seatStatusMap = getValue();
                
                for (Map.Entry<Integer, Button> entry : seatButtons.entrySet()) {
                    int seatId = entry.getKey();
                    Button btn = entry.getValue();
                    Reservation status = seatStatusMap.get(seatId);
                    
                    if (status == null) {
                        setButtonColor(btn, "#81c784"); // GREEN (빈 좌석)
                    } else if (status.getStatus() == ReservationStatus.IN_USE) {
                        setButtonColor(btn, "#e57373"); // RED (사용 중)
                    } else if (status.getStatus() == ReservationStatus.PENDING) {
                        setButtonColor(btn, "#ffb74d"); // YELLOW (예약)
                    } else {
                        setButtonColor(btn, "#90a4ae"); // 기타 (회색)
                    }
                }
            }
        };

        new Thread(colorUpdateTask).start();
    }
    
    private void setButtonColor(Button btn, String hexColor) {
        btn.setStyle("-fx-min-width: 60; -fx-min-height: 40; -fx-background-color: " + hexColor + ";");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Platform.runLater(alert::showAndWait);
    }
    
    private boolean showAlertConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}