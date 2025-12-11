package service;

import impl.SeatDAOImpl;
import model.Seat;
import java.util.List;

public class SeatService {
    
    private final SeatDAOImpl seatDAO = new SeatDAOImpl();
    
    public List<Seat> getAllSeats() {
        return seatDAO.getAllSeats();
    }
}