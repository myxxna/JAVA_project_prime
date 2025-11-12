package model;

public class User {
    private int id;
    private String studentId;
    private String name;
    private String role;
    private String password;
    private int penaltyCount;// 👈 관리자 여부를 판단할 필드 추가

    // ✅ UserDAOImpl에서 호출할 새로운 생성자 (5개 필드)
    public User() {
    }
    // (★수정 2★) 생성자에서 email 파라미터 삭제
    public User(int id, int penaltyCount, String studentId, String name, /*String email,*/ String role, String password) {

        this.id = id;
        this.penaltyCount = penaltyCount; 
        this.studentId = studentId;
        this.name = name; 
        this.role = role;
        this.password = password;
        this.role = role;
        this.password = password;
    }


    /**
     * LoginController.java 오류 해결: 사용자가 관리자 권한을 가졌는지 확인하는 메서드
     */
    public boolean isAdmin() {
        // 'role' 필드의 값이 "ADMIN" (대소문자 무시)인지 확인하여 관리자 여부를 판단합니다.
        return this.role != null && this.role.toUpperCase().equals("ADMIN");
    }
    
    // --- Getters and Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    
    public String getRole() { // 👈 role 필드에 대한 Getter
        return role;
    }

    public void setRole(String role) { // 👈 role 필드에 대한 Setter
        this.role = role;
    }
    public String getPassword() { // 👈 password Getter 추가
        return password;
    }

    public void setPassword(String password) { // 👈 password Setter 추가
        this.password = password;
    }

        public int getPenaltyCount() {
        return penaltyCount;
    }

    // (★수정 4★) 'penaltyCounAt' 오타 수정 및 올바른 할당
    public void setPenaltyCount(int penaltyCount) {

        this.penaltyCount = penaltyCount;
    }
}