package service;

import impl.SeatDAOImpl;
import interfaces.ISeatDAO;
import model.Seat;

import java.util.List;

public class SeatService {
    private ISeatDAO seatDAO = new SeatDAOImpl();

    public List<Seat> getSeats(String roomNumber) {
        return seatDAO.getSeatsByRoom(roomNumber);
    }

    public boolean reserveSeat(int seatId) {
        return seatDAO.reserveSeat(seatId);
    }
    
}
