package impl;

import config.DBConnection;
import model.Seat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl {

    public List<Seat> getAllSeats() {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Seat seat = new Seat();
                seat.setId(rs.getInt("id"));
                seat.setFloor(rs.getInt("floor"));
                seat.setRoomNumber(rs.getString("room_index"));
                seat.setSeatIndex(rs.getInt("seat_index"));
                seat.setSeatNumber(rs.getString("seat_number"));
                seat.setStatus(rs.getString("status"));

                applyHardcodedLayout(seat);

                seats.add(seat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SeatDAOImpl.getAllSeats() DB 오류: " + e.getMessage());
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }

        return seats;
    }

    private void applyHardcodedLayout(Seat seat) {
        switch (seat.getSeatNumber()) {
            case "A1": seat.setRow(0); seat.setCol(0); break;
            case "A2": seat.setRow(0); seat.setCol(1); break;
            case "A3": seat.setRow(0); seat.setCol(2); break;

            case "A4": seat.setRow(1); seat.setCol(0); break;
            case "A5": seat.setRow(1); seat.setCol(1); break;

            case "B1": seat.setRow(2); seat.setCol(0); break;
            case "B2": seat.setRow(2); seat.setCol(1); break;
            case "B3": seat.setRow(2); seat.setCol(2); break;

            case "B4": seat.setRow(3); seat.setCol(0); break;
            case "B5": seat.setRow(3); seat.setCol(1); break;

            case "C1": seat.setRow(4); seat.setCol(0); break;
            case "C2": seat.setRow(4); seat.setCol(1); break;
            case "C3": seat.setRow(4); seat.setCol(2); break;

            case "C4": seat.setRow(5); seat.setCol(0); break;
            case "C5": seat.setRow(5); seat.setCol(1); break;

            default: seat.setRow(0); seat.setCol(0); break;
        }
    }
}