package impl;

import model.Seat;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl {
    
    private static final List<Seat> ALL_SEATS_DUMMY;
    
    static {
        ALL_SEATS_DUMMY = new ArrayList<>();
        // 15개 더미 좌석 데이터
        ALL_SEATS_DUMMY.add(new Seat(1, "A1", 0, 0));
        ALL_SEATS_DUMMY.add(new Seat(2, "A2", 0, 1));
        ALL_SEATS_DUMMY.add(new Seat(3, "A3", 0, 2));
        ALL_SEATS_DUMMY.add(new Seat(4, "A4", 1, 0));
        ALL_SEATS_DUMMY.add(new Seat(5, "A5", 1, 1));
        
        ALL_SEATS_DUMMY.add(new Seat(6, "B1", 2, 0));
        ALL_SEATS_DUMMY.add(new Seat(7, "B2", 2, 1));
        ALL_SEATS_DUMMY.add(new Seat(8, "B3", 2, 2));
        ALL_SEATS_DUMMY.add(new Seat(9, "B4", 3, 0));
        ALL_SEATS_DUMMY.add(new Seat(10, "B5", 3, 1));
        
        ALL_SEATS_DUMMY.add(new Seat(11, "C1", 4, 0));
        ALL_SEATS_DUMMY.add(new Seat(12, "C2", 4, 1));
        ALL_SEATS_DUMMY.add(new Seat(13, "C3", 4, 2));
        ALL_SEATS_DUMMY.add(new Seat(14, "C4", 5, 0));
        ALL_SEATS_DUMMY.add(new Seat(15, "C5", 5, 1));
    }

    public List<Seat> getAllSeats() {
        return new ArrayList<>(ALL_SEATS_DUMMY);
    }
}