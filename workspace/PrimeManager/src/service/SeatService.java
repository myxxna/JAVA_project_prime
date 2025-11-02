package service;

import impl.SeatDAOImpl;
import model.Seat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class SeatService {

    private SeatDAOImpl seatDAO = new SeatDAOImpl();

    public ObservableList<Seat> getAllSeatsObservable() {
        List<Seat> seatList = seatDAO.getAllSeats();
        return FXCollections.observableArrayList(seatList);
    }
}
