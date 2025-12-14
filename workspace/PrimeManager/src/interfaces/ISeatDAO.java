package interfaces;

import model.Seat;
import java.util.List;

public interface ISeatDAO {
    
    // 모든 좌석 조회
    List<Seat> getAllSeats();
    
    // ID로 좌석 조회
    Seat getSeatById(int id);
    
    // 좌석 번호로 조회
    Seat getSeatBySeatNumber(String seatNumber);
    
    // 좌석 번호로 ID 조회
    int getSeatIdBySeatNumber(String seatNumber);
    
    // 유저 ID로 좌석 조회
    Seat getSeatByUserId(int userId);
    
    // ★ [핵심] 좌석 상태 업데이트 (Seat 객체를 받음)
    boolean updateSeatStatus(Seat seat);
}