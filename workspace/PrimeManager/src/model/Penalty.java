package model;

import java.time.LocalDate; // 날짜만 필요하므로 LocalDate 사용

public class Penalty {

    private String userId;  // 패널티를 받은 사용자 ID
    private String reason;  // 사유
    private LocalDate date;    // 부과 날짜

    // 기본 생성자
    public Penalty() {
    }

    // 모든 필드를 받는 생성자
    public Penalty(String userId, String reason, LocalDate date) {
        this.userId = userId;
        this.reason = reason;
        this.date = date;
    }

    // --- Getter와 Setter ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // 디버깅용 toString()
    @Override
    public String toString() {
        return "Penalty{" +
                "userId='" + userId + '\'' +
                ", reason='" + reason + '\'' +
                ", date=" + date +
                '}';
    }
}