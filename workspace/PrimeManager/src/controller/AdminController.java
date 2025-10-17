package controller;

import java.util.List;

import dao.SeatDAO;
import dao.SeatDAO.SeatWithUserDTO;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class AdminController {

    @FXML
    private GridPane seatGridPane;

    private SeatDAO seatDAO;

    public AdminController() {
        this.seatDAO = new SeatDAO();
    }

    @FXML
    public void initialize() {
        loadSeatStatus();
    }

    private void loadSeatStatus() {
        List<SeatWithUserDTO> seats = seatDAO.findAllSeatsWithUser();
        int row = 0;
        int col = 0;
        final int maxCols = 10; // 한 줄에 표시할 최대 좌석 수

        for (SeatWithUserDTO seat : seats) {
            VBox seatBox = new VBox(5);
            seatBox.setAlignment(Pos.CENTER);
            seatBox.setPrefSize(100, 100);

            Text seatNumber = new Text(seat.getSeatNumber());
            seatBox.getChildren().add(seatNumber);

            if (seat.getUserId() != null) {
                // 사용 중인 좌석
                seatBox.setStyle("-fx-background-color: #FFCDD2; -fx-border-color: #E57373; -fx-border-width: 1;");
                Text studentId = new Text(seat.getStudentId());
                Text username = new Text(seat.getUsername());
                Text department = new Text(seat.getDepartment());
                seatBox.getChildren().addAll(studentId, username, department);
            } else {
                // 사용 가능한 좌석
                seatBox.setStyle("-fx-background-color: #C8E6C9; -fx-border-color: #81C784; -fx-border-width: 1;");
                Text availableText = new Text("사용 가능");
                seatBox.getChildren().add(availableText);
            }

            seatGridPane.add(seatBox, col, row);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }
}