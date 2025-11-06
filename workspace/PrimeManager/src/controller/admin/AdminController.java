package controller.admin;

import javafx.application.Platform; 
import javafx.collections.FXCollections; 
import javafx.collections.ObservableList; 
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn; 
import javafx.scene.control.TableView; 
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane; 
import javafx.scene.layout.GridPane; 
import javafx.scene.layout.StackPane; 
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color; 
import javafx.scene.shape.Rectangle; 
import javafx.scene.text.Font; 
import javafx.stage.Stage; 
import model.Seat;
import model.Penalty; 
import controller.kiosk.LoginController;
import service.AdminService;
import model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 
import java.util.List;
import java.util.stream.Collectors; 
import java.util.ArrayList; 
import java.util.Optional; 

/**
 * 관리자 페이지의 FXML 컨트롤러 클래스입니다.
 * 좌석 현황 표시, 층/룸 선택, 좌석 상태 관리, 신고 목록 조회 등의 UI 이벤트를 처리합니다.
 */
public class AdminController {

    private AdminService adminService; // 비즈니스 로직 처리를 위한 서비스 객체
    private Seat selectedSeat = null; // 현재 사용자가 UI에서 선택한 좌석 객체

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm"); // 시간 표시 형식

    // --- FXML 컴포넌트 연결 ---
    @FXML private BorderPane adminRootPane; 
    @FXML private ListView<String> floorListView; // 층 목록 표시
    @FXML private ListView<String> roomListView; // 선택된 층의 룸 목록 표시
    @FXML private GridPane visualSeatGrid; // 좌석 현황을 시각적으로 표시하는 그리드

    // 신고 목록 테이블 관련 컴포넌트
    @FXML private TableView<Penalty> reportTable; 
    @FXML private TableColumn<Penalty, Integer> reportSeatIdCol; // 신고 좌석 ID 컬럼
    @FXML private TableColumn<Penalty, String> reportReasonCol; // 신고 사유 컬럼
    @FXML private TableColumn<Penalty, LocalDateTime> reportTimeCol; // 신고 시각 컬럼
    
    @FXML private ListView<String> overdueUserList; // 시간 초과자 목록 (미구현 또는 Placeholder)

    // 하단 관리 패널 컴포넌트
    @FXML private Label selectedSeatLabel; // 선택된 좌석의 상세 정보 표시 레이블
    @FXML private TextField actionField; // 패널티/퇴실 사유 입력 필드
    @FXML private Button penaltyButton; // 패널티 부여 버튼
    @FXML private Button ejectButton; // 강제 퇴실 버튼
    @FXML private Button toggleBrokenButton; // 점검 상태 토글 버튼

    /**
     * FXML 로드 후 자동으로 호출되는 초기화 메서드입니다.
     */
    @FXML
    public void initialize() {
        this.adminService = new AdminService(); 
        setupFloorAndRoomListeners(); // 층/룸 리스너 설정
        loadFloorList(); // 초기 층 목록 로드
        
        setupReportTableColumns(); // 신고 테이블 컬럼 매핑 설정
        loadReportList(); // 신고 목록 데이터 로드
        
        // (창 최대화 코드 - 필요시 주석 해제하여 사용)
    }
    
    /**
     * AdminService를 통해 층(Floor) 목록을 불러와 ListView에 표시합니다.
     */
    private void loadFloorList() {
        List<Integer> floors = adminService.getFloors();
        List<String> floorNames = floors.stream()
                                      .map(f -> f + "층")
                                      .collect(Collectors.toList());
        floorListView.setItems(FXCollections.observableArrayList(floorNames));
    }

    /**
     * 층(floor) 및 룸(room) 목록 선택 시 발생하는 이벤트를 처리하는 리스너를 설정합니다.
     */
    private void setupFloorAndRoomListeners() {
        // 1. 층(Floor) 리스너: 층 선택 시 해당 층의 룸 목록을 로드합니다.
        floorListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldFloor, newFloor) -> {
                if (newFloor != null) {
                    try {
                        int floorNum = Integer.parseInt(newFloor.replace("층", ""));
                        List<String> rooms = adminService.getRoomsByFloor(floorNum);
                        roomListView.setItems(FXCollections.observableArrayList(rooms));
                    } catch (NumberFormatException e) {
                        System.out.println("층 번호 파싱 오류: " + newFloor);
                    }
                    visualSeatGrid.getChildren().clear(); // 새로운 층 선택 시 좌석 그리드 초기화
                    setSelectedSeat(null); // 좌석 선택 해제
                }
            }
        );

        // 2. 룸(Room) 리스너: 룸 선택 시 해당 룸의 좌석 현황을 로드하고 시각화합니다.
        roomListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldRoom, newRoom) -> {
                if (newRoom != null) {
                    loadSeatsForRoom(newRoom);
                    setSelectedSeat(null); // 좌석 선택 해제
                }
            }
        );
    }
    
    // --- 신고 목록 탭 관련 메서드 ---
    
    /**
     * 신고 목록 테이블(TableView)의 컬럼과 Penalty 모델의 필드를 매핑합니다.
     */
    private void setupReportTableColumns() {
        reportSeatIdCol.setCellValueFactory(new PropertyValueFactory<>("seatIndex")); 
        reportReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reportTimeCol.setCellValueFactory(new PropertyValueFactory<>("reportTime"));
    }
    
    /**
     * AdminService를 통해 DB에서 신고 목록을 불러와 TableView에 채웁니다.
     */
    private void loadReportList() {
        List<Penalty> penalties = adminService.getAllPenalties();
        reportTable.setItems(FXCollections.observableArrayList(penalties));
    }
    
    // --- 좌석 현황 및 관리 메서드 ---
    
    /**
     * 선택된 룸의 좌석 데이터를 로드하고 렌더링을 요청합니다.
     */
    private void loadSeatsForRoom(String roomName) {
        List<Seat> seatList = adminService.getSeatsByRoom(roomName);
        renderVisualSeats(seatList, roomName); 
    }

    /**
     * UI에서 특정 좌석을 선택했을 때 하단 상세 정보를 업데이트합니다.
     * @param seat 선택된 좌석 객체
     */
    private void setSelectedSeat(Seat seat) {
        this.selectedSeat = seat;
        if (selectedSeat != null) {
            String seatNum = seat.getSeatNumber();
            String status = seat.getStatus(); 
            Integer userId = seat.getCurrentUserId();
            LocalDateTime startTime = seat.getStartTime();
            String startTimeStr = (startTime != null) ? startTime.format(TIME_FORMATTER) : "N/A";
            
            String userName = seat.getCurrentUserName();
            
            // 이름과 ID를 모두 포함한 표시 문자열을 생성합니다. (예: "홍길동 (ID: 1001)")
            String userDisplay;
            if (userName != null && !userName.isEmpty() && userId != null && userId != 0) {
                userDisplay = userName + " (ID: " + userId + ")";
            } else if (userId != null && userId != 0) {
                userDisplay = "ID: " + userId; 
            } else {
                userDisplay = "정보 없음";
            }

            // 좌석 상태에 따라 레이블 텍스트를 구성합니다. (반각/전각 문자 모두 처리)
            switch (status) {
                case "U": 
                case "Ｕ": // 사용 중
                    selectedSeatLabel.setText("좌석: " + seatNum + " (사용중, " + userDisplay + ", 시작: " + startTimeStr + ")");
                    break;
                case "R": 
                case "Ｒ": // 예약됨
                    selectedSeatLabel.setText("좌석: " + seatNum + " (예약됨, " + userDisplay + ")");
                    break;
                case "E": 
                case "Ｅ": // 사용 가능
                    selectedSeatLabel.setText("좌석: " + seatNum + " (사용 가능)");
                    break;
                case "C": 
                case "Ｃ": // 점검 중
                    selectedSeatLabel.setText("좌석: " + seatNum + " (점검 중)");
                    break;
                default:
                    selectedSeatLabel.setText("좌석: " + seatNum + " (알 수 없음 - Status: " + status + ")");
                    break;
            }
        } else {
            // 좌석이 선택되지 않았을 때 기본 메시지 표시
            String selectedRoom = roomListView.getSelectionModel().getSelectedItem();
            if(selectedRoom != null) {
                selectedSeatLabel.setText(selectedRoom + " 룸의 좌석을 클릭하세요.");
            } else {
                selectedSeatLabel.setText("먼저 층과 룸을 선택한 후, 좌석을 클릭하세요.");
            }
        }
    }

    @FXML
    void handlePenalty(ActionEvent event) {
        // 패널티 부여 로직 (현재는 임시 처리되어 있을 수 있음)
    }
    
    /**
     * 강제 퇴실 버튼 클릭 시 처리 로직입니다.
     */
    @FXML
    void handleEject(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        if (selectedSeat.getCurrentUserId() == null || selectedSeat.getCurrentUserId() == 0) { showAlert(AlertType.WARNING, "알림", "선택한 좌석은 현재 이용자가 없습니다."); return; }
        
        int userId = selectedSeat.getCurrentUserId();
        String seatNum = selectedSeat.getSeatNumber();
        
        // 강제 퇴실 확인 대화상자
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("강제 퇴실 확인");
        confirmAlert.setHeaderText("좌석: " + seatNum + " (이용자 ID: " + userId + ")");
        confirmAlert.setContentText("정말로 이 사용자를 강제 퇴실시키겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) { 
            // 서비스 계층을 통해 퇴실 처리 요청
            boolean success = adminService.forceEjectUser(userId, actionField.getText()); 
            if (success) {
                showAlert(AlertType.INFORMATION, "성공", "ID: " + userId + " 님을 강제 퇴실시켰습니다.");
                actionField.clear(); 
                // 좌석 현황 새로고침
                loadSeatsForRoom(roomListView.getSelectionModel().getSelectedItem());
            } else {
                showAlert(AlertType.ERROR, "실패", "DB 오류. 강제 퇴실에 실패했습니다.");
            }
        }
    }
    
    /**
     * 좌석 점검 상태를 토글하는 로직입니다. (점검 중 <-> 사용 가능)
     */
    @FXML
    void handleToggleBroken(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        
        String currentStatus = selectedSeat.getStatus();
        String newStatus = null;
        String confirmText = null;

        // 현재 상태 확인 및 변경할 상태 결정 (반각/전각 문자 모두 확인)
        if ("E".equals(currentStatus) || "Ｅ".equals(currentStatus)) { // 현재 사용 가능 -> 점검 중
            newStatus = "C"; 
            confirmText = "이 좌석을 '점검 중(Ｃ)' 상태로 변경하시겠습니까?";
        } 
        else if ("C".equals(currentStatus) || "Ｃ".equals(currentStatus)) { // 현재 점검 중 -> 사용 가능
            newStatus = "E"; 
            confirmText = "이 좌석을 '사용 가능(Ｅ)' 상태로 변경하시겠습니까?";
        } 
        else {
            showAlert(AlertType.WARNING, "변경 불가", "사용 중('U'/'Ｕ')이거나 예약 중('R'/'Ｒ')인 좌석은\n점검 상태로 변경할 수 없습니다.");
            return;
        }
        
        // 상태 변경 확인 대화상자
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("좌석 상태 변경 확인");
        confirmAlert.setHeaderText("좌석: " + selectedSeat.getSeatNumber());
        confirmAlert.setContentText(confirmText);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 서비스 계층을 통해 상태 변경 요청
            boolean success = adminService.setSeatStatus(selectedSeat.getId(), newStatus); 
            
            if (success) {
                showAlert(AlertType.INFORMATION, "성공", "좌석 상태를 '" + newStatus + "'(으)로 변경했습니다.");
                // 좌석 현황 새로고침
                loadSeatsForRoom(roomListView.getSelectionModel().getSelectedItem());
            } else {
                showAlert(AlertType.ERROR, "실패", "DB 오류. 상태 변경에 실패했습니다.");
            }
        }
    }

    /**
     * 좌석 목록을 받아 GridPane에 시각적으로 렌더링합니다.
     * @param seatList 렌더링할 좌석 목록
     * @param roomName 룸 이름 (현재 사용되지 않지만 시그니처 유지)
     */
    private void renderVisualSeats(List<Seat> seatList, String roomName) {
        visualSeatGrid.getChildren().clear(); 
        for (Seat seat : seatList) {
            StackPane seatPane = createSeatPane(seat); // 개별 좌석 UI 생성
            String seatNumber = seat.getSeatNumber(); 
            try {
                // 좌석 번호(예: A1)를 파싱하여 그리드 위치(행/열)를 계산합니다.
                char rowChar = seatNumber.charAt(0);
                int rowIndex = rowChar - 'A'; // 'A' -> 0, 'B' -> 1
                int colNum = Integer.parseInt(seatNumber.substring(1));
                int colIndex = colNum - 1; 
                visualSeatGrid.add(seatPane, colIndex, rowIndex);
            } catch (Exception e) {
                System.out.println("좌석 번호 파싱 오류: " + seatNumber + " (" + e.getMessage() + ")");
            }
        }
    }

    /**
     * 개별 좌석 객체로부터 시각적인 StackPane UI 요소를 생성합니다.
     * @param seat 데이터베이스에서 로드된 Seat 객체
     * @return 렌더링된 StackPane (좌석 박스)
     */
    private StackPane createSeatPane(Seat seat) {
        // ★UI 가독성 개선: 좌석 박스 크기 확대 (90, 60)
        Rectangle rect = new Rectangle(90, 60); 
        rect.setStroke(Color.DARKGRAY);
        rect.setArcWidth(10);
        rect.setArcHeight(10);

        // ★UI 가독성 개선: 폰트 크기 확대 (14)
        Label seatLabel = new Label(seat.getSeatNumber());
        seatLabel.setFont(new Font("Arial", 14)); 
        seatLabel.setStyle("-fx-font-weight: bold;");
        
        // ★UI 가독성 개선: 폰트 크기 확대 (10)
        Label userLabel = new Label();
        userLabel.setFont(new Font("Arial", 10)); 
        
        // ★UI 가독성 개선: 폰트 크기 확대 (10)
        Label timeLabel = new Label(); 
        timeLabel.setFont(new Font("Arial", 10)); 

        Integer userId = seat.getCurrentUserId();
        LocalDateTime startTime = seat.getStartTime();
        String userName = seat.getCurrentUserName(); 

        // 좌석 상태에 따른 색상 및 텍스트 설정 (반각/전각 문자 모두 처리)
        switch (seat.getStatus()) {
            case "E": 
            case "Ｅ": // 사용 가능
                rect.setFill(Color.LIGHTGREEN); 
                break;
                
            case "R": 
            case "Ｒ": // 예약됨
                rect.setFill(Color.LIGHTYELLOW);
                userLabel.setText("(예약됨)");
                break;
            
            case "U": 
            case "Ｕ": // 사용 중
                rect.setFill(Color.DARKGRAY); 
                
                // ★좌석 박스 내 이름 및 ID 표시 로직 (예: 홍길동 (1001))
                if (userName != null && !userName.isEmpty() && userId != null && userId != 0) {
                    userLabel.setText(userName + " (" + userId + ")"); 
                } else if (userId != null && userId != 0) { 
                    userLabel.setText("ID: " + userId); 
                }

                if (startTime != null) {
                    timeLabel.setText(startTime.format(TIME_FORMATTER) + " 부터");
                }
                userLabel.setTextFill(Color.WHITE); 
                seatLabel.setTextFill(Color.WHITE); 
                timeLabel.setTextFill(Color.WHITE);
                break;
                
            case "C": 
            case "Ｃ": // 점검 중
                rect.setFill(Color.INDIANRED); 
                userLabel.setText("(점검 중)");
                userLabel.setTextFill(Color.WHITE); 
                seatLabel.setTextFill(Color.WHITE); 
                break;
                
            default: 
                rect.setFill(Color.LIGHTGRAY); 
                break;
        }
        
        // ★UI 가독성 개선: VBox 간격 확대 (3)
        VBox content = new VBox(3, seatLabel, userLabel, timeLabel); 
        content.setAlignment(Pos.CENTER);
        content.setMouseTransparent(true); // 마우스 클릭 이벤트가 하위 요소가 아닌 StackPane 전체로 전달되도록 설정

        StackPane seatPane = new StackPane(rect, content); 
        seatPane.setUserData(seat); // StackPane에 Seat 객체 저장

        // 마우스 클릭 이벤트 리스너 설정
        seatPane.setOnMouseClicked(event -> {
            setSelectedSeat((Seat) seatPane.getUserData()); // 선택된 좌석 설정
            highlightSelectedSeat(seatPane); // 하이라이트 효과 적용
        });

        return seatPane;
    }
    
    /**
     * 선택된 좌석에 하이라이트 효과를 적용하고 기존 좌석의 하이라이트를 제거합니다.
     */
    private void highlightSelectedSeat(StackPane clickedSeatPane) {
        // 모든 좌석의 테두리 초기화
        for (javafx.scene.Node node : visualSeatGrid.getChildren()) {
            if (node instanceof StackPane) {
                Rectangle r = (Rectangle) ((StackPane)node).getChildren().get(0);
                r.setStroke(Color.DARKGRAY); 
                r.setStrokeWidth(1);
            }
        }
        // 선택된 좌석에 파란색 테두리 적용
        Rectangle clickedRect = (Rectangle) clickedSeatPane.getChildren().get(0);
        clickedRect.setStroke(Color.BLUE); 
        clickedRect.setStrokeWidth(3); 
    }

    /**
     * 사용자에게 알림 메시지를 표시하는 헬퍼 메서드입니다.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void handlePenaltyAction(ActionEvent event) {
        // 1. 🛑 [핵심 활용] LoginController에서 User 객체를 가져옵니다.
    	User loggedInUser = LoginController.getCurrentLoggedInUser();

        if (loggedInUser != null) {
            // 2. 학번(User ID)을 가져옵니다. (User 모델에 getUserId()가 있다고 가정)
            String studentIdToAssignPenalty = loggedInUser.getStudentId(); 

            // 3. 패널티 부여 서비스 호출 (DAO를 통해 DB의 penalty_count 증가)
            // boolean success = userService.addPenalty(studentIdToAssignPenalty, "무단 이탈");
            
            System.out.println("관리자 ID: " + loggedInUser.getStudentId() + "가 사용자 ID: " + studentIdToAssignPenalty + "에게 패널티 부여 시도.");
        }
    }
}