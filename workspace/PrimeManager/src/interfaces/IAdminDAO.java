package interfaces;

import java.util.List;

import model.Penalty;
import model.Seat;

public interface IAdminDAO {
	boolean addPenalty(Penalty penalty);
	List<Seat> getAllSeatStatus();
	boolean forceEjectUser(int userId, String reason);
}
