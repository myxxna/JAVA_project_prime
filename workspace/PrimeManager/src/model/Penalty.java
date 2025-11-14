package model;

import java.time.LocalDateTime;

/**
 * (★수정★)
 * 'penalty' 테이블의 정보를 담는 모델 클래스입니다.
 * JOIN 결과를 담기 위해 'studentName'과 'studentRealId' 필드를 추가합니다.
 */
public class Penalty {

    // DB 컬럼과 일치
    private int num;
    private int stId; // (users.id PK를 참조하는 외래 키)
    private String reason;
    private int seatIndex;
    private LocalDateTime reportTime;
    private String reporterType; // (USER 또는 ADMIN)

    // ★(신규)★ DB JOIN 결과를 담기 위한 추가 필드
    private String studentName;  // (users.name)
    private String studentRealId; // (users.st_id - 학번)

    // --- 생성자 ---
    public Penalty() {
    }

    // --- Getters and Setters ---
    
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

    public int getSeatIndex() {
        return seatIndex;
    }
    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }
    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }

    public String getReporterType() {
        return reporterType;
    }
    public void setReporterType(String reporterType) {
        this.reporterType = reporterType;
    }

    // --- ★(신규)★ JOIN 필드 Getter/Setter ---
    // AdminController.java (line 399)가 이 메서드를 필요로 합니다.
    
    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRealId() {
        return studentRealId;
    }
    public void setStudentRealId(String studentRealId) {
        this.studentRealId = studentRealId;
    }
}