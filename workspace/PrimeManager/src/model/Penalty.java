package model;

import java.time.LocalDateTime;

// (★수정★) 새 penalty DB 스키마에 맞춘 모델
public class Penalty {

    private int num;
    private int stId;
    private String reason;
    private LocalDateTime reportTime;
    private int seatIndex;

    // 기본 생성자
    public Penalty() {}

    // --- Getter & Setter ---
    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }
    public int getStId() {
        return stId;
    }
    public void setStId(int stId) {
        this.stId = stId;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public LocalDateTime getReportTime() {
        return reportTime;
    }
    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }
    public int getSeatIndex() {
        return seatIndex;
    }
    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }
}