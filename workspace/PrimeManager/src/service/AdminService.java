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

    public List<Integer> getFloors() {
        return adminDAO.getUniqueFloors();
    }
    
    public List<String> getRoomsByFloor(int floor) {
        return adminDAO.getUniqueRoomsByFloor(floor);
    }
    
    public List<String> getRoomNames() {
        return adminDAO.getUniqueRoomNames();
    }
    
    public List<Seat> getSeatsByRoom(String roomName) {
        return adminDAO.getAllSeatStatusByRoom(roomName);
    }
    
    public boolean grantPenalty(String userId, String reason) {
        // (★주의★) 이 로직은 '이전' Penalty 모델을 사용할 수 있습니다.
        // LocalDate today = LocalDate.now(); 
        // Penalty penalty = new Penalty(userId, reason, today);
        // return adminDAO.addPenalty(penalty);
        return false; // 임시
    }
    
    public boolean forceEjectUser(int userId, String reason) {
        return adminDAO.ejectUserFromSeat(userId);
    }
    
    public boolean setSeatStatus(int seatId, String newStatus) {
        return adminDAO.setSeatStatus(seatId, newStatus);
    }

    /**
     * (★신규★) 신고 목록 조회를 DAO에 요청
     */
    public List<Penalty> getAllPenalties() {
        return adminDAO.getAllPenalties();
    }
}