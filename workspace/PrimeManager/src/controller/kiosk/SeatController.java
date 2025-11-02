
package controller.kiosk;

import impl.SeatDAOImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.GridPane;
import javafx.scene.control.Label;
import model.Seat;
import service.SeatService;

import java.util.List;

public class SeatController {

    @FXML
    private GridPane seatGrid;

    @FXML
    private Label infoLabel;

    private SeatService seatService;
    private List<Seat> seats;
    private Seat selectedSeat;

    private int loggedUserId = 1; // 예시: 로그인 후 세팅

    public void initialize() {
        seatService = new SeatService(new SeatDAOImpl());
        loadSeats();
    }

    private void loadSeats() {
        seatGrid.getChildren().clear();
        seats = seatService.getAllSeats();
        int row = 0, col = 0;

        for (Seat seat : seats) {
            Button seatBtn = new Button(seat.getSeatNumber());
            seatBtn.setPrefWidth(60);
            seatBtn.setStyle("-fx-background-color: " + getSeatColor(seat) + ";");

            seatBtn.setOnAction(e -> selectedSeat = seat);
            seatGrid.add(seatBtn, col, row);

            col++;
            if (col >= 4) { // 4개 좌석마다 줄 바꿈
                col = 0;
                row++;
            }
        }
    }

    private String getSeatColor(Seat seat) {
        switch (seat.getStatus()) {
            case "G": return "green";   // 이용 가능
            case "R": return "red";     // 이용 중
            case "Y": return "yellow";  // 예약됨
            default: return "gray";
        }
    }

    @FXML
    private void handleEnter() {
        if (selectedSeat != null) {
            int duration = 60; // 예시: 1시간
            boolean success = seatService.enterSeat(selectedSeat.getId(), loggedUserId, duration);
            infoLabel.setText(success ? "입실 완료" : "입실 실패");
            loadSeats();
        }
    }

    @FXML
    private void handleExtend() {
        if (selectedSeat != null) {
            int extendMinutes = 30; // 예시: 30분 연장
            boolean success = seatService.extendSeat(selectedSeat.getId(), loggedUserId, extendMinutes);
            infoLabel.setText(success ? "연장 완료" : "연장 실패");
            loadSeats();
        }
    }

    @FXML
    private void handleExit() {
        if (selectedSeat != null) {
            boolean success = seatService.exitSeat(selectedSeat.getId(), loggedUserId);
            infoLabel.setText(success ? "퇴실 완료" : "퇴실 실패");
            loadSeats();
        }
    }

    @FXML
    private void handleReserve() {
        if (selectedSeat != null) {
            int reserveMinutes = 60; // 예시: 1시간 예약
            boolean success = seatService.reserveSeat(selectedSeat.getId(), loggedUserId, reserveMinutes);
            infoLabel.setText(success ? "예약 완료" : "예약 실패");
            loadSeats();
        }
    }
}
