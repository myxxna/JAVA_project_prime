package model;

// (★수정★) 새 DB 스키마에 맞춘 Seat 모델
public class Seat {
    private final int id;
    private final String number;
    private final int row;
    private final int col;

    public Seat(int id, String number, int row, int col) {
        this.id = id;
        this.number = number;
        this.row = row;
        this.col = col;
    }

    public int getId() { return id; }
    public String getNumber() { return number; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}