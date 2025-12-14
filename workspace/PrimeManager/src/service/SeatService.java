package service;

import impl.SeatDAOImpl;
import model.Seat;
import java.time.LocalDateTime;
import java.util.List;

public class SeatService {
    
    private final SeatDAOImpl seatDAO = new SeatDAOImpl();
    
    public Seat getSeatById(int seatId) { return seatDAO.getSeatById(seatId); }
    public Seat getSeatBySeatNumber(String seatNumber) { return seatDAO.getSeatBySeatNumber(seatNumber); }
    public int getSeatIdBySeatNumber(String seatNumber) { return seatDAO.getSeatIdBySeatNumber(seatNumber); }
    public Seat getSeatByUserId(int userId) { return seatDAO.getSeatByUserId(userId); }
    public List<Seat> getSeatList(String floor) { return seatDAO.getAllSeats(); } 

    public boolean checkIn(int seatId, int userId, int durationMinutes) { 
        Seat seat = seatDAO.getSeatById(seatId);
        if (seat == null || "U".equals(seat.getStatus())) return false;
        
        seat.setCurrentUserId(userId);        
        seat.setStatus("U");                  
        LocalDateTime now = LocalDateTime.now();
        seat.setStartTime(now);              
        seat.setEndTime(now.plusMinutes(durationMinutes)); 
        return seatDAO.updateSeatStatus(seat);
    }
    
    public boolean checkOut(int userId) {
        Seat seat = seatDAO.getSeatByUserId(userId);
        if (seat == null) return false; 

        // [수정됨] int형 필드이므로 null 대신 0 사용!
        seat.setCurrentUserId(0);       
        
        seat.setCurrentUserName(null);      
        seat.setStatus("A");                
        seat.setStartTime(null);
        seat.setEndTime(null);

        return seatDAO.updateSeatStatus(seat);
    }
    public boolean extendTime(int seatId, int addMinutes) {
        // DAO에 있는 extendTime 메서드를 호출하여 DB 업데이트
        return seatDAO.extendTime(seatId, addMinutes);
    }
}