package service;

import java.util.List;

import dao.SeatDAO;
import model.Seat;

public class SeatService {

    private final SeatDAO seatDAO;

    public SeatService() {
        this.seatDAO = new SeatDAO();
    }

    public List<Seat> getAllSeats() {
        return seatDAO.findAllSeats();
    }
}
