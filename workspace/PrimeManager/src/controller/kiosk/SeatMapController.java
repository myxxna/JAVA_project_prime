package controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import model.Seat;
import service.SeatService;

import java.util.List;

public class SeatMapController {

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private GridPane seatGrid;

    private SeatService seatService = new SeatService();
    private List<Seat> seatList;
    private ToggleGroup toggleGroup = new ToggleGroup();

    public void initialize() {
        // 방 리스트 초기화
        roomComboBox.getItems().addAll("PRIME-1","PRIME-2","PRIME-3","PRIME-4");
        roomComboBox.getSelectionModel().selectFirst();
        roomComboBox.setOnAction(e -> loadSeats(roomComboBox.getValue()));
        loadSeats(roomComboBox.getValue());
    }

    private void loadSeats(String roomNumber) {
        seatGrid.getChildren().clear();
        seatList = seatService.getSeats(roomNumber);

        int col = 0, row = 0;
        for (Seat seat : seatList) {
            ToggleButton btn = new ToggleButton(seat.getSeatNumber());
            btn.setUserData(seat);
            btn.setPrefWidth(80);
            btn.setPrefHeight(40);
            btn.setToggleGroup(toggleGroup);
            if (seat.isReserved()) {
                btn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                btn.setDisable(false); // 예약된 좌석도 토글 가능, 클릭 시 취소
            } else {
                btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            }

            btn.setOnAction(event -> {
                Seat s = (Seat) btn.getUserData();
                if (!s.isReserved()) {
                    boolean success = seatService.reserveSeat(s.getId());
                    if (success) {
                        s.setReserved(true);
                        btn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                        showAlert("예약 완료", s.getSeatNumber() + " 좌석이 예약되었습니다.");
                    } else {
                        showAlert("예약 실패", s.getSeatNumber() + " 이미 예약됨");
                    }
                } else {
                    // 예약 취소 처리
                    boolean success = seatService.reserveSeat(s.getId()) == false; // 단순 토글 예제
                    if (success) {
                        s.setReserved(false);
                        btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                        showAlert("예약 취소", s.getSeatNumber() + " 좌석 예약이 취소되었습니다.");
                    }
                }
            });

            seatGrid.add(btn, col, row);
            col++;
            if (col >= 4) { // 한 행에 4좌석
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void refreshSeats() {
        loadSeats(roomComboBox.getValue());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
