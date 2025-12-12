package service;

import impl.AdminDAOimpl;
import model.Penalty;
import model.Seat;
import model.User; 
import java.time.LocalDateTime;
import java.util.List;

public class AdminService {
    
    private AdminDAOimpl adminDao;
    
    public AdminService() {
        this.adminDao = new AdminDAOimpl();
    }
    
    // --- [신규] 로그로 범인 찾기 연결 ---
    public User getOffenderByLog(int seatIndex, LocalDateTime reportTime) {
        return adminDao.getOffenderByLog(seatIndex, reportTime);
    }
    public List<User> getSeatHistory(int seatIndex) {
        return adminDao.getSeatHistory(seatIndex);
    }
    
    // 기존 기능 연결
    public List<Integer> getFloors() { return adminDao.getFloors(); }
    public List<String> getRoomsByFloor(int floor) { return adminDao.getRoomsByFloor(floor); }
    public List<Seat> getSeatsByRoom(int floor, String roomName) { return adminDao.getAllSeatStatusByRoom(floor, roomName); }
    
    public boolean grantPenalty(int userId, String reason, int seatIndex) {
        return adminDao.insertPenalty(userId, reason, seatIndex);
    }
    
    public boolean forceEjectUser(int userId, String reason) { return adminDao.forceEjectUser(userId, reason); }
    public boolean setSeatStatus(int seatId, String status) { return adminDao.updateSeatStatus(seatId, status); }
//    public List<String> getReservations(int seatId) { return adminDao.getSeatReservations(seatId); }
    public void processNoShow() { adminDao.processNoShowReservations(); }
    public List<String> getOverdueUsers() { return adminDao.getOverdueReservations(); }
    public void checkPenaltyDone(int reservationId) { adminDao.updateReservationStatusToPenalized(reservationId); }
    
    public List<Penalty> getAllUserReports() { return adminDao.getAllUserReports(); }
    public List<Penalty> getAllAdminPenalties() { return adminDao.getAllAdminPenalties(); }
    public int getUserPk(String studentId) {
        // DAO에 있는 private 메서드를 public으로 바꾸거나, 
        // DAO에 public int getUserPkByStudentId(int stId) 메서드를 노출시켜야 합니다.
        
        // AdminDAOimpl에 있는 getUserPkByStudentId는 int를 받으므로 변환
        try {
            int stIdInt = Integer.parseInt(studentId);
            return adminDao.getUserPkByStudentId(stIdInt); // 이 메서드가 DAO에 private이면 public으로 고치세요!
        } catch (NumberFormatException e) {
            return 0;
        }
    }
 // [AdminService.java] 파일 안에 추가
    public List<String> getReservations(int seatId) {
        return adminDao.getSeatReservations(seatId);
    }
}