package service;

import impl.AdminDAOimpl;
import model.Penalty;
import model.Seat;
import java.util.List;

public class AdminService {
    
    private AdminDAOimpl adminDao;
    
    public AdminService() {
        this.adminDao = new AdminDAOimpl();
    }
    
    public List<Integer> getFloors() {
        return adminDao.getFloors();
    }
    
    public List<String> getRoomsByFloor(int floor) {
        return adminDao.getRoomsByFloor(floor);
    }
    
    public List<Seat> getSeatsByRoom(int floor, String roomName) {
        return adminDao.getAllSeatStatusByRoom(floor, roomName);
    }
    
    public boolean grantPenalty(int userId, String reason, int seatIndex) {
        return adminDao.insertPenalty(userId, reason, seatIndex);
    }
    
    public boolean forceEjectUser(int userId, String reason) {
        return adminDao.forceEjectUser(userId, reason);
    }
    
    public boolean setSeatStatus(int seatId, String status) {
        return adminDao.updateSeatStatus(seatId, status);
    }
    
    // 예약자 명단 가져오기
    public List<String> getReservations(int seatId) {
        return adminDao.getSeatReservations(seatId);
    }
    
    // --- [신규 기능] ---
    
    // 1. 노쇼 처리 (30분 지난 것 상태 변경)
    public void processNoShow() {
        adminDao.processNoShowReservations();
    }
    
    // 2. 시간 초과자(NOSHOW) 목록 가져오기
    public List<String> getOverdueUsers() {
        return adminDao.getOverdueReservations();
    }
    
    // 3. 패널티 부여 후 예약 상태 변경 (PENALIZED)
    public void checkPenaltyDone(int reservationId) {
        adminDao.updateReservationStatusToPenalized(reservationId);
    }
    
    // 기타 메서드 연결
    public List<Penalty> getAllUserReports() { return adminDao.getAllUserReports(); }
    public List<Penalty> getAllAdminPenalties() { return adminDao.getAllAdminPenalties(); }
}