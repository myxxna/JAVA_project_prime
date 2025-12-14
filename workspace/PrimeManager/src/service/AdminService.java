package service;

import impl.AdminDAOimpl;
import model.Penalty;
import model.Seat;
import model.User; 
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class AdminService {
    
    private AdminDAOimpl adminDao;
    
    public AdminService() {
        this.adminDao = new AdminDAOimpl();
    }
    
    // =========================================================
    // [신규] 일일 초기화 (Controller에서 호출 가능)
    // =========================================================
    public void dailyCleanup() {
        adminDao.dailyCleanup();
    }

    // --- [범인 찾기 / 기록] ---
    public User getOffenderByLog(int seatIndex, LocalDateTime reportTime) {
        return adminDao.getOffenderByLog(seatIndex, reportTime);
    }
    public List<User> getSeatHistory(int seatIndex) {
        return adminDao.getSeatHistory(seatIndex);
    }
    
    // --- [기본 기능] ---
    public List<Integer> getFloors() { return adminDao.getFloors(); }
    public List<String> getRoomsByFloor(int floor) { return adminDao.getRoomsByFloor(floor); }
    public List<Seat> getSeatsByRoom(int floor, String roomName) { return adminDao.getAllSeatStatusByRoom(floor, roomName); }
    
    public boolean grantPenalty(int userId, String reason, int seatIndex) {
        return adminDao.insertPenalty(userId, reason, seatIndex);
    }
    
    public boolean forceEjectUser(int userId, String reason) { return adminDao.forceEjectUser(userId, reason); }
    public boolean setSeatStatus(int seatId, String status) { return adminDao.updateSeatStatus(seatId, status); }
    
    public void processNoShow() { adminDao.processNoShowReservations(); }
    public void checkPenaltyDone(int reservationId) { adminDao.updateReservationStatusToPenalized(reservationId); }
    
    public List<Penalty> getAllUserReports() { return adminDao.getAllUserReports(); }
    public List<Penalty> getAllAdminPenalties() { return adminDao.getAllAdminPenalties(); }
    
    public int getUserPk(String studentId) {
        try {
            int stIdInt = Integer.parseInt(studentId);
            return adminDao.getUserPkByStudentId(stIdInt); 
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // =========================================================
    // [수정] 통합 시간 초과자 목록 (미퇴실 + 노쇼)
    // =========================================================
    public List<String> getOverdueUsers() { 
        // 1. 미퇴실(Status='NOSHOW' 등) 목록
        List<String> overdue = adminDao.getOverdueReservations();
        
        // 2. 현재 10분 초과 노쇼 (Status='R') 목록
        List<String> noShowNow = adminDao.getNoShowUsers(); 
        
        if (noShowNow != null && !noShowNow.isEmpty()) {
            overdue.addAll(noShowNow);
        }
        return overdue; 
    }
    
    // =========================================================
    // [추가] 예약자 명단 조회
    // =========================================================
    public List<String> getReservations(int seatId) {
        return adminDao.getSeatReservations(seatId);
    }
}