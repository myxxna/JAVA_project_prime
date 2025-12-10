
package interfaces;

import model.Seat;
import java.util.List;

public interface ISeatDAO {
    List<Seat> getAllSeats();
    Seat getSeatById(int id);
    boolean updateSeat(Seat seat);
}