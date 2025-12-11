package service;

import impl.ReservationDAOImpl;
import model.Reservation;
import java.time.LocalDateTime;

public class ReservationService {

    private ReservationDAOImpl reservationDAO = new ReservationDAOImpl();

    // 예약 처리 메서드
    public boolean makeReservation(int userId, int seatId, LocalDateTime startTime, int durationHours) {
        
        // 1. 예약 객체 생성
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setReservationTime(startTime); // 입실 시간
        reservation.setDurationMinutes(durationHours * 60); // 시간 -> 분 변환
        reservation.setStatus("R"); // ★ 요청하신 대로 Status 'R' 설정

        // 2. DAO를 통해 DB에 저장
        // (심화 기능: 여기서 해당 시간에 이미 예약이 있는지 체크하는 로직 추가 가능)
        
        return reservationDAO.insertReservation(reservation);
    }
}