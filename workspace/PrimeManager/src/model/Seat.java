package model;

import javafx.beans.property.*;

public class Seat {
    private final IntegerProperty seatId;
    private final StringProperty roomNumber;
    private final StringProperty seatNumber;
    private final BooleanProperty isAvailable;

    public Seat(int seatId, String roomNumber, String seatNumber, boolean isAvailable) {
        this.seatId = new SimpleIntegerProperty(seatId);
        this.roomNumber = new SimpleStringProperty(roomNumber);
        this.seatNumber = new SimpleStringProperty(seatNumber);
        this.isAvailable = new SimpleBooleanProperty(isAvailable);
    }

    public int getSeatId() { return seatId.get(); }
    public String getRoomNumber() { return roomNumber.get(); }
    public String getSeatNumber() { return seatNumber.get(); }
    public boolean isAvailable() { return isAvailable.get(); }

    public IntegerProperty seatIdProperty() { return seatId; }
    public StringProperty roomNumberProperty() { return roomNumber; }
    public StringProperty seatNumberProperty() { return seatNumber; }
    public BooleanProperty isAvailableProperty() { return isAvailable; }

    @Override
    public String toString() {
        return "Seat{" +
                "roomNumber='" + roomNumber.get() + '\'' +
                ", seatNumber='" + seatNumber.get() + '\'' +
                ", isAvailable=" + isAvailable.get() +
                '}';
    }
}

