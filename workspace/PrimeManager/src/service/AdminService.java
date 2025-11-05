package service;

import impl.AdminDAOimpl; 
import model.Penalty;
import model.Seat;

import java.time.LocalDate;
import java.util.List;

public class AdminService {

    private AdminDAOimpl adminDAO; 

    public AdminService() {
        this.adminDAO = new AdminDAOimpl(); 
    }

    /**
     * (★수정★) DB에서 층 목록을 가져옵니다.
     * @return 층 번호 리스트
     */
    public List<Integer> getFloors() {
        return adminDAO.getUniqueFloors();
    }
    
    /**
     * (★수정★) 특정 층의 룸 목록을 가져옵니다.
     * @param floor 층 번호
     * @return 룸 이름 리스트
     */
    public List<String> getRoomsByFloor(int floor) {
        return adminDAO.getUniqueRoomsByFloor(floor);
    }
    
    // (이전의 getRoomNames()는 이제 사용되지 않습니다)
    
    public List<Seat> getSeatsByRoom(String roomName) {
        return adminDAO.getAllSeatStatusByRoom(roomName);
    }
    
    public boolean grantPenalty(String userId, String reason) {
        LocalDate today = LocalDate.now(); 
        Penalty penalty = new Penalty(userId, reason, today);
        return adminDAO.addPenalty(penalty);
    }
    
    public boolean forceEjectUser(int userId, String reason) {
        return adminDAO.ejectUserFromSeat(userId);
    }
    
    public boolean setSeatStatus(int seatId, String newStatus) {
        return adminDAO.setSeatStatus(seatId, newStatus);
    }
}