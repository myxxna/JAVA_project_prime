package service;

import impl.AdminDAOimpl; 
import model.Penalty;
import model.Seat;
import config.DBConnection; 

import java.sql.Connection; 
import java.sql.SQLException; 
import java.time.LocalDateTime; 
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
    
    public List<Seat> getSeatsByRoom(int floor, String roomName) {
        return adminDAO.getAllSeatStatusByRoom(floor, roomName);
    }
    
    // ★(신규) 예약 명단을 문자열 리스트로 반환
    public List<String> getReservations(int seatId) {
        return adminDAO.getSeatReservations(seatId);
    }
    
    // 트랜잭션 적용된 패널티 부여
    public boolean grantPenalty(int userId, String reason, int seatIndex) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); 
            try {
                Penalty penalty = new Penalty(); 
                penalty.setStId(userId); 
                penalty.setReason(reason);
                penalty.setReportTime(LocalDateTime.now()); 
                penalty.setSeatIndex(seatIndex); 
                
                boolean step1 = adminDAO.addPenalty(conn, penalty, "ADMIN");
                boolean step2 = adminDAO.incrementUserPenaltyCount(conn, userId);
                
                if (step1 && step2) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("grantPenalty 서비스 트랜잭션 오류: " + e.getMessage());
                e.printStackTrace();
                conn.rollback(); 
                return false;
            }
        } catch (SQLException e) {
            System.out.println("grantPenalty 서비스 Connection 획득 오류: " + e.getMessage());
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

    public List<Penalty> getAllUserReports() {
        return adminDAO.getAllUserReports();
    }
    
    public List<Penalty> getAllAdminPenalties() {
        return adminDAO.getAllAdminPenalties();
    }
}