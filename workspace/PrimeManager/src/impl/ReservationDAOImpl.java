package impl; // [중요] service가 아니라 impl 입니다!

import interfaces.IReservationDAO;
import model.Reservation;
import config.DBConnection; // [중요] DBConnection Import

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDAOImpl implements IReservationDAO {

    @Override
    public Optional<Integer> createReservation(Reservation res) {
        String sql = "INSERT INTO reservations (user_id, seat_id, reservation_time, status, created_at, using_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, res.getUserId());
            pstmt.setInt(2, res.getSeatId());
            pstmt.setTimestamp(3, Timestamp.valueOf(res.getReservationTime()));
            pstmt.setString(4, res.getStatus());
            pstmt.setTimestamp(5, Timestamp.valueOf(res.getCreatedAt()));
            pstmt.setInt(6, res.getUsingTime());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return Optional.of(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Reservation> getFutureReservationsBySeatId(int seatId) {
        List<Reservation> list = new ArrayList<>();
        // 오늘 날짜의 예약(R) 조회
        String sql = "SELECT * FROM reservations WHERE seat_id = ? AND status = 'R' AND DATE(created_at) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Optional<Reservation> findActiveReservationByUserId(int userId) {
        String sql = "SELECT * FROM reservations WHERE user_id = ? AND status IN ('R', 'ACTIVE')";
        return findReservationByQuery(sql, stmt -> stmt.setInt(1, userId));
    }

    @Override
    public Optional<Reservation> findActiveReservationBySeatId(int seatId) {
        String sql = "SELECT * FROM reservations WHERE seat_id = ? AND status IN ('R', 'ACTIVE')";
        return findReservationByQuery(sql, stmt -> stmt.setInt(1, seatId));
    }

    @Override
    public void updateStatusToInUse(int reservationId) {
        executeUpdate("UPDATE reservations SET status = 'ACTIVE' WHERE id = ?", reservationId);
    }

    @Override
    public void cancelReservation(int reservationId) {
        executeUpdate("UPDATE reservations SET status = 'CANCELED' WHERE id = ?", reservationId);
    }

    @Override
    public void finishReservation(int reservationId, LocalDateTime actualEndTime) {
        String sql = "UPDATE reservations SET status = 'COMPLETED', actual_end_time = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, actualEndTime);
            pstmt.setInt(2, reservationId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Reservation> getAllActiveReservations() {
        return new ArrayList<>(); // 필요 시 구현
    }

    // --- Helper Methods ---
    private void executeUpdate(String sql, int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Optional<Reservation> findReservationByQuery(String sql, SqlConsumer<PreparedStatement> paramSetter) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            paramSetter.accept(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setId(rs.getInt("id"));
        res.setUserId(rs.getInt("user_id"));
        res.setSeatId(rs.getInt("seat_id"));
        
        Timestamp resTime = rs.getTimestamp("reservation_time");
        if (resTime != null) res.setReservationTime(resTime.toLocalDateTime());
        
        Timestamp createTime = rs.getTimestamp("created_at");
        if (createTime != null) res.setCreatedAt(createTime.toLocalDateTime());
        
        res.setUsingTime(rs.getInt("using_time"));
        res.setStatus(rs.getString("status"));
        
        // 예상 종료 시간 계산 (입실시간 + 이용시간)
        if (res.getCreatedAt() != null && res.getUsingTime() > 0) {
            res.setExpectedEndTime(res.getCreatedAt().plusHours(res.getUsingTime()));
        }
        return res;
    }

    @FunctionalInterface interface SqlConsumer<T> { void accept(T t) throws SQLException; }
}