package interfaces;

import model.Seat;
import java.util.List;

public interface ISeatDAO {
    List<Seat> getSeatsByRoom(String roomNumber);
    boolean reserveSeat(int seatId);
}
