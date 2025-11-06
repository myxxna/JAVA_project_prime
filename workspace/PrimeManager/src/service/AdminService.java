package service;

import impl.AdminDAOimpl; 
import model.Penalty;
import model.Seat;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 기능을 위한 서비스 계층 클래스입니다.
 * Controller와 DAO 사이의 중간 역할을 하며, 비즈니스 로직(현재는 대부분 DAO 호출)을 담당합니다.
 */
public class AdminService {

    private AdminDAOimpl adminDAO; // 데이터베이스 접근을 위한 DAO 객체

    /**
     * AdminService 생성자. AdminDAOimpl 인스턴스를 초기화합니다.
     */
    public AdminService() {
        this.adminDAO = new AdminDAOimpl(); 
    }

    /**
     * DAO를 통해 모든 층(Floor) 목록을 조회합니다.
     * @return 층 번호 리스트
     */
    public List<Integer> getFloors() {
        return adminDAO.getUniqueFloors();
    }
    
    /**
     * DAO를 통해 특정 층에 속하는 룸(Room) 목록을 조회합니다.
     * @param floor 조회할 층 번호
     * @return 룸 이름 리스트
     */
    public List<String> getRoomsByFloor(int floor) {
        return adminDAO.getUniqueRoomsByFloor(floor);
    }
    
    /**
     * DAO를 통해 전체 룸 목록을 조회합니다.
     * @return 룸 이름 리스트
     */
    public List<String> getRoomNames() {
        return adminDAO.getUniqueRoomNames();
    }
    
    /**
     * DAO를 통해 특정 룸의 모든 좌석 상태 및 사용자 정보를 조회합니다.
     * @param roomName 조회할 룸 이름
     * @return Seat 객체 리스트 (사용자 정보가 JOIN된 상태)
     */
    public List<Seat> getSeatsByRoom(String roomName) {
        return adminDAO.getAllSeatStatusByRoom(roomName);
    }
    
    /**
     * (임시 로직) 사용자에게 패널티를 부여하는 로직입니다.
     * 현재는 Penalty 모델 및 DAO 로직이 주석 처리되어 있어 임시로 false를 반환합니다.
     * @param userId 패널티를 부여할 사용자 ID
     * @param reason 패널티 사유
     * @return 성공 여부 (현재는 항상 false)
     */
    public boolean grantPenalty(String userId, String reason) {
        // (★주의★) 실제 로직 구현 시 주석을 해제하고 사용해야 합니다.
        /*
        LocalDate today = LocalDate.now(); 
        Penalty penalty = new Penalty(userId, reason, today);
        return adminDAO.addPenalty(penalty);
        */
        return false; // 임시
    }
    
    /**
     * 특정 사용자를 좌석에서 강제 퇴실 처리하도록 DAO에 요청합니다.
     * @param userId 퇴실시킬 사용자 ID
     * @param reason 퇴실 사유 (현재 DAO에서는 사유를 저장하지 않고 퇴실 처리만 수행)
     * @return 성공 여부
     */
    public boolean forceEjectUser(int userId, String reason) {
        // reason은 이 서비스 계층에서 추가적인 로그 기록 등의 비즈니스 로직을 수행할 수 있지만,
        // 현재는 DAO의 ejectUserFromSeat 메서드만 호출합니다.
        return adminDAO.ejectUserFromSeat(userId);
    }
    
    /**
     * 특정 좌석의 상태(status)를 변경하도록 DAO에 요청합니다.
     * @param seatId 상태를 변경할 좌석 ID
     * @param newStatus 새로운 상태 값 (예: "E", "C")
     * @return 성공 여부
     */
    public boolean setSeatStatus(int seatId, String newStatus) {
        return adminDAO.setSeatStatus(seatId, newStatus);
    }

    /**
     * DAO를 통해 모든 신고 목록(Penalty) 데이터를 조회합니다.
     * @return Penalty 객체 리스트
     */
    public List<Penalty> getAllPenalties() {
        return adminDAO.getAllPenalties();
    }
}