package model;

/**
 * 사용자 정보를 나타내는 모델 클래스.
 */
public class User {
    private String userId; // 사용자 ID
    private String username; // 사용자 이름
    private String password; // 비밀번호
    private boolean isAdmin; // 관리자 여부
    private String studentId; // 학번
    private String department; // 학과

    public User() {
    }

    public User(String userId, String username, String password, boolean isAdmin, String studentId, String department) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.studentId = studentId;
        this.department = department;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}