package service;

import impl.AdminDAOimpl; 
import model.Penalty;
import model.Seat;

import java.time.LocalDateTime; 
import java.util.List;

/**
 * 관리자 기능을 위한 서비스 계층 클래스입니다.
 * (★수정★) '신고'와 '관리자 패널티' 로직 분리
 */
public class AdminService {

    private AdminDAOimpl adminDAO; 

    public AdminService() {
        this.adminDAO = new AdminDAOimpl(); 
    }

    // --- (DAO 단순 호출 메서드들) ---
    
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
    
    /**
     * (★핵심 수정★)
     * 관리자가 사용자에게 패널티를 부여합니다.
     * 'ADMIN' 타입으로 DAO에 전달합니다.
     * @param userId 패널티를 부여할 사용자의 ID (users.id PK)
     * @param reason 패널티 사유
     * @param seatIndex 패널티가 발생한 좌석 인덱스
     * @return 두 작업이 모두 성공하면 true
     */
    public boolean grantPenalty(int userId, String reason, int seatIndex) {
        try {
            int stId = userId; 
            
            Penalty penalty = new Penalty(); 
            penalty.setStId(stId);
            penalty.setReason(reason);
            penalty.setReportTime(LocalDateTime.now()); 
            penalty.setSeatIndex(seatIndex); 
            
            // ★(수정)★ 1. penalty 테이블에 'ADMIN' 타입으로 INSERT
            boolean step1 = adminDAO.addPenalty(penalty, "ADMIN");
            
            // ★(수정)★ 2. users 테이블에 UPDATE
            boolean step2 = adminDAO.incrementUserPenaltyCount(userId);
            
            return step1 && step2;
            
        } catch (Exception e) { 
            System.out.println("grantPenalty 서비스 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean forceEjectUser(int userId, String reason) {
        return adminDAO.ejectUserFromSeat(userId);
    }
    
    public boolean setSeatStatus(int seatId, String newStatus) {
        return adminDAO.setSeatStatus(seatId, newStatus);
    }

    /**
     * (★수정★)
     * DAO를 통해 '사용자 신고' 목록만 조회합니다.
     */
    public List<Penalty> getAllUserReports() {
        return adminDAO.getAllUserReports();
    }
    
    /**
     * (★신규 추가★)
     * DAO를 통해 '관리자 부여 패널티' 목록만 조회합니다.
     */
    public List<Penalty> getAllAdminPenalties() {
        return adminDAO.getAllAdminPenalties();
    }
}