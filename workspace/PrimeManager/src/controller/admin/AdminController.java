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
import service.AdminService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 
import java.util.List;
import java.util.stream.Collectors; 
import java.util.ArrayList; 
import java.util.Optional; 

public class AdminController {

    private AdminService adminService;
    private Seat selectedSeat = null; 

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // --- FXML 컴포넌트 연결 ---
    @FXML private BorderPane adminRootPane; 
    @FXML private ListView<String> floorListView; 
    @FXML private ListView<String> roomListView; 
    @FXML private GridPane visualSeatGrid; 

    @FXML private TableView<Penalty> reportTable; 
    @FXML private TableColumn<Penalty, Integer> reportSeatIdCol; 
    @FXML private TableColumn<Penalty, String> reportReasonCol;
    @FXML private TableColumn<Penalty, LocalDateTime> reportTimeCol;
    
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
        loadFloorList();
        
        setupReportTableColumns();
        loadReportList();
        
        // (창 최대화 코드)
        /*
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) adminRootPane.getScene().getWindow();
                if (stage != null && !stage.isMaximized()) {
                    stage.setMaximized(true); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        */
    }
    
    private void loadFloorList() {
        List<Integer> floors = adminService.getFloors();
        List<String> floorNames = floors.stream()
                                      .map(f -> f + "층")
                                      .collect(Collectors.toList());
        floorListView.setItems(FXCollections.observableArrayList(floorNames));
    }

    private void setupFloorAndRoomListeners() {
        // 1. 층(Floor) 리스너
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
                    visualSeatGrid.getChildren().clear();
                    setSelectedSeat(null);
                }
            }
        );

        // 2. 룸(Room) 리스너
        roomListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldRoom, newRoom) -> {
                if (newRoom != null) {
                    loadSeatsForRoom(newRoom);
                    setSelectedSeat(null);
                }
            }
        );
    }
    
    // --- 신고 목록 탭 관련 메서드 ---
    
    private void setupReportTableColumns() {
        reportSeatIdCol.setCellValueFactory(new PropertyValueFactory<>("seatIndex")); 
        reportReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reportTimeCol.setCellValueFactory(new PropertyValueFactory<>("reportTime"));
    }
    
    private void loadReportList() {
        List<Penalty> penalties = adminService.getAllPenalties();
        reportTable.setItems(FXCollections.observableArrayList(penalties));
    }
    
    // --- (이하 좌석 관련 코드) ---
    
    private void loadSeatsForRoom(String roomName) {
        List<Seat> seatList = adminService.getSeatsByRoom(roomName);
        renderVisualSeats(seatList, roomName); 
    }

    private void setSelectedSeat(Seat seat) {
        this.selectedSeat = seat;
        if (selectedSeat != null) {
            String seatNum = seat.getSeatNumber();
            String status = seat.getStatus(); 
            Integer userId = seat.getCurrentUserId();
            LocalDateTime startTime = seat.getStartTime();
            String startTimeStr = (startTime != null) ? startTime.format(TIME_FORMATTER) : "N/A";
            
            String userName = seat.getCurrentUserName();
            
            // ★수정: 이름과 ID를 모두 표시하도록 로직 변경 (가장 많은 정보를 제공)
            String userDisplay;
            if (userName != null && !userName.isEmpty() && userId != null && userId != 0) {
                // 이름과 ID가 모두 있을 때: "이름 (ID: 12345)" 형식
                userDisplay = userName + " (ID: " + userId + ")";
            } else if (userId != null && userId != 0) {
                // 이름은 없지만 ID는 있을 때: "ID: 12345" 형식
                userDisplay = "ID: " + userId; 
            } else {
                // 정보가 없을 때
                userDisplay = "정보 없음";
            }

            // ★수정: 반각/전각 문자 모두 처리하도록 수정
            switch (status) {
                case "U": 
                case "Ｕ": // 사용 중
                    // userDisplay에는 이제 이름과 ID가 함께 포함됩니다.
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
        // ... (이전 코드)
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
        String newStatus = null;
        String confirmText = null;

        // ★수정: 반각/전각 문자 모두 확인
        if ("E".equals(currentStatus) || "Ｅ".equals(currentStatus)) { 
            newStatus = "C"; 
            confirmText = "이 좌석을 '점검 중(Ｃ)' 상태로 변경하시겠습니까?";
        } 
        else if ("C".equals(currentStatus) || "Ｃ".equals(currentStatus)) { 
            newStatus = "E"; 
            confirmText = "이 좌석을 '사용 가능(Ｅ)' 상태로 변경하시겠습니까?";
        } 
        else {
            showAlert(AlertType.WARNING, "변경 불가", "사용 중('U'/'Ｕ')이거나 예약 중('R'/'Ｒ')인 좌석은\n점검 상태로 변경할 수 없습니다.");
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
        Rectangle rect = new Rectangle(70, 50); 
        rect.setStroke(Color.DARKGRAY);
        rect.setArcWidth(10);
        rect.setArcHeight(10);

        Label seatLabel = new Label(seat.getSeatNumber());
        seatLabel.setFont(new Font("Arial", 12)); 
        seatLabel.setStyle("-fx-font-weight: bold;");
        
        Label userLabel = new Label();
        userLabel.setFont(new Font("Arial", 9)); 
        
        Label timeLabel = new Label(); 
        timeLabel.setFont(new Font("Arial", 9)); 

        Integer userId = seat.getCurrentUserId();
        LocalDateTime startTime = seat.getStartTime();
        String userName = seat.getCurrentUserName(); 

        // ★수정: 반각/전각 문자 모두 확인하여 UI 색상/텍스트 설정
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
                
                // ★수정: 이름과 ID를 모두 표시하도록 로직 변경
                if (userName != null && !userName.isEmpty() && userId != null && userId != 0) {
                    userLabel.setText(userName + " (" + userId + ")"); // 이름 (ID) 형식
                } else if (userId != null && userId != 0) { 
                    userLabel.setText("ID: " + userId); // 이름 없이 ID만 표시
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
        
        VBox content = new VBox(2, seatLabel, userLabel, timeLabel); 
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