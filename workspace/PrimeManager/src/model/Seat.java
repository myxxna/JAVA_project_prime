package model;

public class Seat {
    private int id;
    private String roomNumber;
    private String seatNumber;
    private boolean reserved;

    public Seat(int id, String roomNumber, String seatNumber, boolean reserved) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.seatNumber = seatNumber;
        this.reserved = reserved;
    }

    public int getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public String getSeatNumber() { return seatNumber; }
    public boolean isReserved() { return reserved; }
    public void setReserved(boolean reserved) { this.reserved = reserved; }
}
