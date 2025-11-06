package impl;

import config.DBConnection;
import interfaces.IReservationDAO;
import model.Reservation;
import model.Reservation.ReservationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ReservationDAOImpl implements IReservationDAO {

    @Override
    public boolean createReservation(Reservation reservation) {

        String sqlInsertReservation = "INSERT INTO reservations (st_id, seat_index, reservation_start, reservation_time, created_at) " +
                "VALUES (?, ?, ?, ?, NOW())";

        String sqlUpdateSeat = "UPDATE seats SET status = ?, current_user_id = ?, " +
                "start_time = ?, end_time = ? " +
                "WHERE id = ? AND status = 'G'";

        Connection conn = null;
        PreparedStatement pstmtInsert = null;
        PreparedStatement pstmtUpdate = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            pstmtInsert = conn.prepareStatement(sqlInsertReservation);
            pstmtInsert.setInt(1, Integer.parseInt(reservation.getUserId().replace("C_Tester", "").replace("00", "")));
            pstmtInsert.setInt(2, reservation.getSeatId());
            pstmtInsert.setTimestamp(3, java.sql.Timestamp.valueOf(reservation.getStartTime()));
            pstmtInsert.setInt(4, reservation.getDurationMinutes());
            pstmtInsert.executeUpdate();

            pstmtUpdate = conn.prepareStatement(sqlUpdateSeat);
            pstmtUpdate.setString(1, ReservationStatus.PENDING.name());
            pstmtUpdate.setInt(2, Integer.parseInt(reservation.getUserId().replace("C_Tester", "").replace("00", "")));
            pstmtUpdate.setTimestamp(3, java.sql.Timestamp.valueOf(reservation.getStartTime()));
            pstmtUpdate.setTimestamp(4, java.sql.Timestamp.valueOf(reservation.getExpectedEndTime()));
            pstmtUpdate.setInt(5, reservation.getSeatId());

            int rowsAffected = pstmtUpdate.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            System.err.println("createReservation DB 오류: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmtInsert);
            try {
                if (pstmtUpdate != null) pstmtUpdate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Reservation findActiveReservationByUserId(String userId) {

        String sql = "SELECT s.*, r.reservation_time, r.id AS reservation_db_id " +
                "FROM seats s " +
                "LEFT JOIN reservations r ON s.current_user_id = r.st_id AND s.id = r.seat_index " +
                "WHERE s.current_user_id = ? AND (s.status = 'IN_USE' OR s.status = 'PENDING') " +
                "ORDER BY r.created_at DESC LIMIT 1";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(userId.replace("C_Tester", "").replace("00", "")));

            rs = pstmt.executeQuery();

            if (rs.next()) {

                int durationMinutes = 0;
                if(rs.getTimestamp("start_time") != null && rs.getTimestamp("end_time") != null) {
                    durationMinutes = (int) java.time.Duration.between(
                            rs.getTimestamp("start_time").toLocalDateTime(),
                            rs.getTimestamp("end_time").toLocalDateTime()
                    ).toMinutes();
                }

                Reservation r = new Reservation(
                        userId,
                        rs.getInt("id"),
                        rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : LocalDateTime.now(),
                        durationMinutes
                );

                r.setReservationId(rs.getLong("reservation_db_id"));
                r.setExpectedEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                r.setStatus(ReservationStatus.valueOf(rs.getString("status")));

                r.setInitialDurationMinutes(rs.getInt("reservation_time"));

                return r;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("findActiveReservationByUserId DB 오류: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return null;
    }

    @Override
    public boolean updateStatusToInUse(String userId) {
        String sql = "UPDATE seats SET status = 'IN_USE', start_time = NOW() " +
                "WHERE current_user_id = ? AND status = 'PENDING'";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(userId.replace("C_Tester", "").replace("00", "")));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("updateStatusToInUse DB 오류: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    @Override
    public boolean cancelReservation(String userId) {

        String sql = "UPDATE seats SET status = 'G', current_user_id = NULL, " +
                "start_time = NULL, end_time = NULL " +
                "WHERE current_user_id = ? AND status = 'PENDING'";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(userId.replace("C_Tester", "").replace("00", "")));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("cancelReservation DB 오류: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    @Override
    public Reservation findActiveReservationBySeatId(int seatId) {

        String sql = "SELECT * FROM seats WHERE id = ? AND (status = 'IN_USE' OR status = 'PENDING')";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, seatId);
            rs = pstmt.executeQuery();

            if (rs.next()) {

                int durationMinutes = 0;
                if(rs.getTimestamp("start_time") != null && rs.getTimestamp("end_time") != null) {
                    durationMinutes = (int) java.time.Duration.between(
                            rs.getTimestamp("start_time").toLocalDateTime(),
                            rs.getTimestamp("end_time").toLocalDateTime()
                    ).toMinutes();
                }

                Reservation r = new Reservation(
                        String.valueOf(rs.getInt("current_user_id")),
                        rs.getInt("id"),
                        rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : LocalDateTime.now(),
                        durationMinutes
                );

                r.setExpectedEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                r.setStatus(ReservationStatus.valueOf(rs.getString("status")));

                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("findActiveReservationBySeatId DB 오류: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public boolean updateExpectedEndTime(long reservationId, LocalDateTime newEndTime, int minutesToAdd) {

        String sql = "UPDATE seats SET end_time = DATE_ADD(end_time, INTERVAL ? MINUTE) " +
                "WHERE id = (SELECT seat_index FROM reservations WHERE id = ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, minutesToAdd);
            pstmt.setLong(2, reservationId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("updateExpectedEndTime DB 오류: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    @Override
    public boolean finishReservation(long reservationId, LocalDateTime actualEndTime) {

        String sql = "UPDATE seats SET status = 'G', current_user_id = NULL, " +
                "start_time = NULL, end_time = NULL " +
                "WHERE id = (SELECT seat_index FROM reservations WHERE id = ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, reservationId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("finishReservation DB 오류: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }
}