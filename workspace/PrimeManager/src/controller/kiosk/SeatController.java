package controller.kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Seat;
import service.SeatService;
import javafx.collections.ObservableList;

public class SeatController {

    @FXML
    private TableView<Seat> seatTableView;

    @FXML
    private TableColumn<Seat, Integer> seatIdCol;

    @FXML
    private TableColumn<Seat, String> roomCol;

    @FXML
    private TableColumn<Seat, String> seatCol;

    @FXML
    private TableColumn<Seat, Boolean> availCol;

    private SeatService seatService = new SeatService();

    @FXML
    public void initialize() {
        seatIdCol.setCellValueFactory(new PropertyValueFactory<>("seatId"));
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        seatCol.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        availCol.setCellValueFactory(new PropertyValueFactory<>("available"));

        ObservableList<Seat> seats = seatService.getAllSeatsObservable();
        seatTableView.setItems(seats);
    }
}
