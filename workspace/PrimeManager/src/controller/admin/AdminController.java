package controller.admin;

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
import javafx.scene.layout.GridPane; 
import javafx.scene.layout.StackPane; 
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color; 
import javafx.scene.shape.Rectangle; 
import javafx.scene.text.Font; 
import model.Seat;
import service.AdminService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; 
import java.util.stream.Collectors; // (★신규★)

public class AdminController {

    private AdminService adminService;
    private Seat selectedSeat = null; 

    // (★삭제★) 하드코딩 맵 삭제
    // private Map<String, List<String>> floorRoomMap; 

    // --- FXML 컴포넌트 연결 ---
    @FXML private ListView<String> floorListView; // (String "4층", "7층"을 담음)
    @FXML private ListView<String> roomListView; 
    @FXML private GridPane visualSeatGrid; 

    @FXML private TableView<Object> reportTable; 
    @FXML private TableColumn<Object, String> reportSeatIdCol;
    @FXML private TableColumn<Object, String> reportReasonCol;
    @FXML private TableColumn<Object, String> reportTimeCol;
    
    @FXML private ListView<String> overdueUserList; 

    @FXML private Label selectedSeatLabel;
    @FXML private TextField actionField;
    @FXML private Button penaltyButton;
    @FXML private Button ejectButton;
    @FXML private Button toggleBrokenButton;

    @FXML
    public void initialize() {
        this.adminService = new AdminService(); 
        setupFloorAndRoomListeners();
        loadFloorList(); // (★수정★) loadFloorAndRoomList -> loadFloorList
    }
    
    /**
     * (★수정★) DB에서 '층' 목록을 동적으로 불러옵니다.
     */
    private void loadFloorList() {
        // 1. DB에서 층 목록(숫자)을 가져옵니다 (e.g., [4, 7])
        List<Integer> floors = adminService.getFloors();
        
        // 2. "층" 텍스트를 붙여 String 리스트로 변환합니다 (e.g., ["4층", "7층"])
        List<String> floorNames = floors.stream()
                                      .map(f -> f + "층")
                                      .collect(Collectors.toList());
        
        // 3. 층 ListView에 채웁니다.
        floorListView.setItems(FXCollections.observableArrayList(floorNames));
    }

    /**
     * (★수정★) 층 및 룸 목록(ListView)의 클릭 리스너 (동적 로직)
     */
    private void setupFloorAndRoomListeners() {
        // 1. 층(Floor) 리스너
        floorListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldFloor, newFloor) -> {
                if (newFloor != null) {
                    // "4층" -> 4 (숫자)로 변환
                    try {
                        int floorNum = Integer.parseInt(newFloor.replace("층", ""));
                        
                        // DB에서 해당 층의 룸 목록을 가져옵니다.
                        List<String> rooms = adminService.getRoomsByFloor(floorNum);
                        
                        // 룸 목록(roomListView)을 갱신합니다.
                        roomListView.setItems(FXCollections.observableArrayList(rooms));
                        
                    } catch (NumberFormatException e) {
                        System.out.println("층 번호 파싱 오류: " + newFloor);
                    }
                    
                    // 좌석 뷰와 선택 상태를 초기화합니다.
                    visualSeatGrid.getChildren().clear();
                    setSelectedSeat(null);
                }
            }
        );

        // 2. 룸(Room) 리스너 (변경 없음)
        roomListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldRoom, newRoom) -> {
                if (newRoom != null) {
                    loadSeatsForRoom(newRoom);
                    setSelectedSeat(null);
                }
            }
        );
    }
    
    private void loadSeatsForRoom(String roomName) {
        List<Seat> seatList = adminService.getSeatsByRoom(roomName);
        renderVisualSeats(seatList, roomName); 
    }

    /**
     * 선택된 좌석 정보 업데이트 (변경 없음)
     */
    private void setSelectedSeat(Seat seat) {
        this.selectedSeat = seat;
        if (selectedSeat != null) {
            if (this.selectedSeat.getCurrentUserId() != null && this.selectedSeat.getCurrentUserId() != 0) {
                selectedSeatLabel.setText("선택된 좌석: " + selectedSeat.getSeatNumber() 
                                        + " (이용자 ID: " + selectedSeat.getCurrentUserId() + ")");
            } else {
                selectedSeatLabel.setText("선택된 좌석: " + selectedSeat.getSeatNumber() 
                                        + " (이용자 없음)");
            }
        } else {
            String selectedRoom = roomListView.getSelectionModel().getSelectedItem();
            if(selectedRoom != null) {
                selectedSeatLabel.setText(selectedRoom + " 룸의 좌석을 클릭하세요.");
            } else {
                selectedSeatLabel.setText("먼저 층과 룸을 선택한 후, 좌석을 클릭하세요.");
            }
        }
    }

    // --- 버튼 핸들러 (이하 모두 변경 없음) ---

    @FXML
    void handlePenalty(ActionEvent event) {
        String reason = actionField.getText();
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        if (selectedSeat.getCurrentUserId() == null || selectedSeat.getCurrentUserId() == 0) { showAlert(AlertType.WARNING, "알림", "선택한 좌석은 현재 이용자가 없습니다."); return; }
        if (reason == null || reason.trim().isEmpty()) { showAlert(AlertType.ERROR, "오류", "패널티 사유를 반드시 입력해야 합니다."); return; }

        String userIdStr = String.valueOf(selectedSeat.getCurrentUserId());
        boolean success = adminService.grantPenalty(userIdStr, reason);

        if (success) {
            showAlert(AlertType.INFORMATION, "성공", "ID: " + userIdStr + " 님에게 패널티를 부여했습니다.");
            actionField.clear(); 
            loadSeatsForRoom(roomListView.getSelectionModel().getSelectedItem()); 
        } else {
            showAlert(AlertType.ERROR, "실패", "DB 오류. 패널티 부여에 실패했습니다.");
        }
    }
    
    @FXML
    void handleEject(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        if (selectedSeat.getCurrentUserId() == null || selectedSeat.getCurrentUserId() == 0) { showAlert(AlertType.WARNING, "알림", "선택한 좌석은 현재 이용자가 없습니다."); return; }

        int userId = selectedSeat.getCurrentUserId();
        String seatNum = selectedSeat.getSeatNumber();
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("강제 퇴실 확인");
        confirmAlert.setHeaderText("좌석: " + seatNum + " (이용자 ID: " + userId + ")");
        confirmAlert.setContentText("정말로 이 사용자를 강제 퇴실시키겠습니까?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) { 
            boolean success = adminService.forceEjectUser(userId, actionField.getText()); 
            if (success) {
                showAlert(AlertType.INFORMATION, "성공", "ID: " + userId + " 님을 강제 퇴실시켰습니다.");
                actionField.clear(); 
                loadSeatsForRoom(roomListView.getSelectionModel().getSelectedItem());
            } else {
                showAlert(AlertType.ERROR, "실패", "DB 오류. 강제 퇴실에 실패했습니다.");
            }
        }
    }
    
    @FXML
    void handleToggleBroken(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        
        String currentStatus = selectedSeat.getStatus();
        Integer currentUserId = selectedSeat.getCurrentUserId();
        String newStatus = null;
        String confirmText = null;

        if ("G".equals(currentStatus)) {
            newStatus = "R"; 
            confirmText = "이 좌석을 '점검 중(R)' 상태로 변경하시겠습니까?";
        } 
        else if ("R".equals(currentStatus) && (currentUserId == null || currentUserId == 0)) {
            newStatus = "G"; 
            confirmText = "이 좌석을 '사용 가능(G)' 상태로 변경하시겠습니까?";
        } 
        else {
            showAlert(AlertType.WARNING, "변경 불가", "사용자가 이용 중('R')이거나 예약 중('Y')인 좌석은\n점검 상태로 변경할 수 없습니다.");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("좌석 상태 변경 확인");
        confirmAlert.setHeaderText("좌석: " + selectedSeat.getSeatNumber());
        confirmAlert.setContentText(confirmText);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = adminService.setSeatStatus(selectedSeat.getId(), newStatus); 
            
            if (success) {
                showAlert(AlertType.INFORMATION, "성공", "좌석 상태를 '" + newStatus + "'(으)로 변경했습니다.");
                loadSeatsForRoom(roomListView.getSelectionModel().getSelectedItem());
            } else {
                showAlert(AlertType.ERROR, "실패", "DB 오류. 상태 변경에 실패했습니다.");
            }
        }
    }

    // --- 데이터 로드 및 UI 렌더링 (이하 변경 없음) ---

    private void renderVisualSeats(List<Seat> seatList, String roomName) {
        visualSeatGrid.getChildren().clear(); 

        for (Seat seat : seatList) {
            StackPane seatPane = createSeatPane(seat);
            String seatNumber = seat.getSeatNumber(); 
            
            try {
                char rowChar = seatNumber.charAt(0);
                int rowIndex = rowChar - 'A'; 
                
                int colNum = Integer.parseInt(seatNumber.substring(1));
                int colIndex = colNum - 1; 
                
                visualSeatGrid.add(seatPane, colIndex, rowIndex);

            } catch (Exception e) {
                System.out.println("좌석 번호 파싱 오류: " + seatNumber + " (" + e.getMessage() + ")");
            }
        }
    }

    private StackPane createSeatPane(Seat seat) {
        Rectangle rect = new Rectangle(80, 60);
        rect.setStroke(Color.DARKGRAY);
        rect.setArcWidth(10);
        rect.setArcHeight(10);

        Label seatLabel = new Label(seat.getSeatNumber());
        seatLabel.setFont(new Font("Arial", 14));
        seatLabel.setStyle("-fx-font-weight: bold;");
        
        Label userLabel = new Label();
        userLabel.setFont(new Font("Arial", 10));

        Integer userId = seat.getCurrentUserId();

        switch (seat.getStatus()) {
            case "G": 
                rect.setFill(Color.LIGHTGREEN); 
                break;
            case "Y": 
                rect.setFill(Color.LIGHTYELLOW);
                userLabel.setText("예약됨");
                break;
            
            case "R": 
                rect.setFill(Color.INDIANRED); 
                if (userId != null && userId != 0) { 
                    userLabel.setText("ID: " + userId);
                } else { 
                    userLabel.setText("(점검 중)");
                    userLabel.setTextFill(Color.WHITE); 
                    seatLabel.setTextFill(Color.WHITE); 
                }
                break;
            default: 
                rect.setFill(Color.LIGHTGRAY); 
                break;
        }
        
        VBox content = new VBox(5, seatLabel, userLabel); 
        content.setAlignment(Pos.CENTER);
        content.setMouseTransparent(true);

        StackPane seatPane = new StackPane(rect, content); 
        seatPane.setUserData(seat);

        seatPane.setOnMouseClicked(event -> {
            setSelectedSeat((Seat) seatPane.getUserData()); 
            highlightSelectedSeat(seatPane);
        });

        return seatPane;
    }
    
    private void highlightSelectedSeat(StackPane clickedSeatPane) {
        for (javafx.scene.Node node : visualSeatGrid.getChildren()) {
            if (node instanceof StackPane) {
                Rectangle r = (Rectangle) ((StackPane)node).getChildren().get(0);
                r.setStroke(Color.DARKGRAY); 
                r.setStrokeWidth(1);
            }
        }
        
        Rectangle clickedRect = (Rectangle) clickedSeatPane.getChildren().get(0);
        clickedRect.setStroke(Color.BLUE); 
        clickedRect.setStrokeWidth(3); 
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}