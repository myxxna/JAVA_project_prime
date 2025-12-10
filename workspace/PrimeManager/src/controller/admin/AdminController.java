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
import javafx.scene.control.ToggleButton; 
import javafx.scene.control.ToggleGroup;  
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell; 
import javafx.util.Callback; 
import javafx.scene.layout.BorderPane; 
import javafx.scene.layout.GridPane; 
import javafx.scene.layout.StackPane; 
import javafx.scene.layout.TilePane; 
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color; 
import javafx.scene.shape.Rectangle; 
import javafx.scene.text.Font; 
import javafx.stage.Stage; 
import model.Seat;
import model.Penalty; 
import model.User; 
import controller.kiosk.LoginController; 
import service.AdminService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 
import java.util.List;
import java.util.ArrayList; 
import java.util.Optional; 
import javafx.concurrent.Task; 

public class AdminController {

    private AdminService adminService; 
    private Seat selectedSeat = null; 

    private int currentSelectedFloor = 0; 
    private String currentSelectedRoom = null;

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private BorderPane adminRootPane; 
    @FXML private TilePane floorButtonContainer; 
    @FXML private VBox roomButtonContainer; 
    @FXML private GridPane visualSeatGrid; 
    @FXML private ListView<String> overdueUserList; 
    @FXML private Label selectedSeatLabel;
    @FXML private TextField actionField; 
    
    // 버튼 FXML 주입
    @FXML private Button penaltyButton;
    @FXML private Button ejectButton;
    @FXML private Button toggleBrokenButton;
    @FXML private Button viewReservationButton; // ★신규 버튼

    @FXML private TableView<Penalty> reportTable; 
    @FXML private TableColumn<Penalty, Integer> reportSeatIdCol; 
    @FXML private TableColumn<Penalty, String> reportStudentIdCol; 
    @FXML private TableColumn<Penalty, String> reportStudentNameCol; 
    @FXML private TableColumn<Penalty, String> reportReasonCol; 
    @FXML private TableColumn<Penalty, LocalDateTime> reportTimeCol;
    @FXML private TableColumn<Penalty, Void> reportActionCol; 
    
    @FXML private TableView<Penalty> adminPenaltyTable; 
    @FXML private TableColumn<Penalty, Integer> adminPenaltySeatIdCol; 
    @FXML private TableColumn<Penalty, String> adminPenaltyStudentIdCol; 
    @FXML private TableColumn<Penalty, String> adminPenaltyStudentNameCol; 
    @FXML private TableColumn<Penalty, String> adminPenaltyReasonCol;
    @FXML private TableColumn<Penalty, LocalDateTime> adminPenaltyTimeCol;

    private ToggleGroup floorGroup = new ToggleGroup();
    private ToggleGroup roomGroup = new ToggleGroup();

    @FXML
    public void initialize() {
        this.adminService = new AdminService(); 
        
        createFloorButtons(); 
        setupReportTableColumns(); 
        setupAdminPenaltyTableColumns(); 
        loadUserReportsInBackground(); 
        loadAdminPenaltiesInBackground(); 
        
        adminRootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
                    if (newWindow instanceof Stage) {
                        Stage stage = (Stage) newWindow;
                        stage.widthProperty().addListener((o, oldVal, newVal) -> Platform.runLater(stage::centerOnScreen));
                        stage.setOnShown(e -> Platform.runLater(stage::centerOnScreen));
                        if (stage.isShowing()) Platform.runLater(stage::centerOnScreen);
                    }
                });
            }
        });
    }
    
    private void createFloorButtons() {
        List<Integer> floors = adminService.getFloors();
        floorButtonContainer.getChildren().clear();
        for (Integer floor : floors) {
            ToggleButton btn = new ToggleButton(floor + "층");
            btn.setToggleGroup(floorGroup);
            btn.setPrefWidth(100);
            btn.setPrefHeight(40);
            btn.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-font-weight: bold;");
            btn.setOnAction(e -> {
                if (btn.isSelected()) {
                    updateButtonStyles(floorGroup);
                    createRoomButtons(floor);
                }
            });
            floorButtonContainer.getChildren().add(btn);
        }
    }

    private void createRoomButtons(int floor) {
        currentSelectedFloor = floor; 
        List<String> rooms = adminService.getRoomsByFloor(floor);
        roomButtonContainer.getChildren().clear();
        visualSeatGrid.getChildren().clear(); 
        for (String room : rooms) {
            ToggleButton btn = new ToggleButton(room);
            btn.setToggleGroup(roomGroup);
            btn.setMaxWidth(Double.MAX_VALUE); 
            btn.setPrefHeight(40);
            btn.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-font-weight: bold;");
            btn.setOnAction(e -> {
                if (btn.isSelected()) {
                    updateButtonStyles(roomGroup);
                    currentSelectedRoom = room;
                    loadSeatsForRoomInBackground(floor, room);
                    setSelectedSeat(null);
                }
            });
            roomButtonContainer.getChildren().add(btn);
        }
    }
    
    private void updateButtonStyles(ToggleGroup group) {
        group.getToggles().forEach(toggle -> {
            ToggleButton btn = (ToggleButton) toggle;
            if (btn.isSelected()) {
                btn.setStyle("-fx-background-color: #5c9aff; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
            } else {
                btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-background-radius: 5; -fx-border-color: #cccccc; -fx-border-radius: 5;");
            }
        });
    }
    
    private void setupReportTableColumns() {
        reportSeatIdCol.setCellValueFactory(new PropertyValueFactory<>("seatIndex")); 
        reportStudentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentRealId")); 
        reportStudentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName")); 
        reportReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reportTimeCol.setCellValueFactory(new PropertyValueFactory<>("reportTime"));
        Callback<TableColumn<Penalty, Void>, TableCell<Penalty, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Penalty, Void> call(final TableColumn<Penalty, Void> param) {
                final TableCell<Penalty, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("패널티 부여");
                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Penalty penaltyReport = getTableView().getItems().get(getIndex());
                            handleGrantPenaltyFromReport(penaltyReport);
                        });
                        btn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null); else setGraphic(btn);
                    }
                };
                return cell;
            }
        };
        reportActionCol.setCellFactory(cellFactory);
    }
    
    private void setupAdminPenaltyTableColumns() {
        adminPenaltySeatIdCol.setCellValueFactory(new PropertyValueFactory<>("seatIndex")); 
        adminPenaltyStudentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentRealId")); 
        adminPenaltyStudentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName")); 
        adminPenaltyReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        adminPenaltyTimeCol.setCellValueFactory(new PropertyValueFactory<>("reportTime"));
    }
    
    private void loadUserReportsInBackground() {
        Task<List<Penalty>> loadPenaltiesTask = new Task<>() {
            @Override protected List<Penalty> call() throws Exception { return adminService.getAllUserReports(); }
        };
        loadPenaltiesTask.setOnSucceeded(e -> reportTable.setItems(FXCollections.observableArrayList(loadPenaltiesTask.getValue())));
        new Thread(loadPenaltiesTask).start();
    }
    
    private void loadAdminPenaltiesInBackground() {
        Task<List<Penalty>> loadAdminPenaltiesTask = new Task<>() {
            @Override protected List<Penalty> call() throws Exception { return adminService.getAllAdminPenalties(); }
        };
        loadAdminPenaltiesTask.setOnSucceeded(e -> adminPenaltyTable.setItems(FXCollections.observableArrayList(loadAdminPenaltiesTask.getValue())));
        new Thread(loadAdminPenaltiesTask).start();
    }
    
    private void loadSeatsForRoomInBackground(int floor, String roomName) {
        visualSeatGrid.getChildren().clear(); 
        Task<List<Seat>> loadSeatsTask = new Task<>() {
            @Override protected List<Seat> call() throws Exception { return adminService.getSeatsByRoom(floor, roomName); }
        };
        loadSeatsTask.setOnSucceeded(e -> renderVisualSeats(loadSeatsTask.getValue(), roomName));
        loadSeatsTask.setOnFailed(e -> {
            showAlert(AlertType.ERROR, "오류", "좌석 로드 실패: " + loadSeatsTask.getException().getMessage());
            loadSeatsTask.getException().printStackTrace();
        });
        new Thread(loadSeatsTask).start();
    }

    private void setSelectedSeat(Seat seat) {
        this.selectedSeat = seat;
        if (selectedSeat != null) {
            String seatNum = selectedSeat.getSeatNumber();
            String status = selectedSeat.getStatus(); 
            String nameValue = selectedSeat.getCurrentUserName(); // DAO에서 넣어준 값
            
            String infoText = "좌석: " + seatNum;
            
            if ("U".equals(status)) {
                // 사용 중일 때
                infoText += " (사용자: " + (nameValue != null ? nameValue : "없음") + ")";
            } else {
                // 미사용일 때 (예약자 있는지 확인)
                if (nameValue != null && !nameValue.isEmpty()) {
                    infoText += " (예약자: " + nameValue + ")";
                } else {
                    infoText += " (빈 좌석)";
                }
            }
            selectedSeatLabel.setText(infoText);
        } else {
            selectedSeatLabel.setText(currentSelectedRoom != null ? currentSelectedRoom + "의 좌석을 클릭하세요." : "먼저 층과 룸을 선택하세요.");
        }
    }

    @FXML
    void handlePenalty(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        Integer userId = selectedSeat.getCurrentUserId();
        int seatIndex = selectedSeat.getSeatIndex(); 
        String reason = actionField.getText();
        if (userId == null || userId == 0) { showAlert(AlertType.WARNING, "알림", "선택한 좌석은 현재 이용자가 없습니다."); return; }
        if (reason == null || reason.trim().isEmpty()) { showAlert(AlertType.ERROR, "오류", "사유를 입력하세요."); return; }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("패널티 부여 확인");
        confirmAlert.setHeaderText("좌석: " + selectedSeat.getSeatNumber());
        confirmAlert.setContentText("정말로 이 사용자에게 패널티를 부여하시겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> penaltyTask = new Task<>() {
                @Override protected Boolean call() throws Exception { return adminService.grantPenalty(userId, reason, seatIndex); }
            };
            penaltyTask.setOnSucceeded(e -> {
                if (penaltyTask.getValue()) {
                    showAlert(AlertType.INFORMATION, "성공", "패널티 부여 성공");
                    actionField.clear(); 
                    loadAdminPenaltiesInBackground(); 
                } else showAlert(AlertType.ERROR, "실패", "패널티 부여 실패");
            });
            new Thread(penaltyTask).start();
        }
    }
    
    private void handleGrantPenaltyFromReport(Penalty report) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("패널티 부여 확인");
        confirmAlert.setContentText("신고된 사용자에게 패널티를 부여하시겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> penaltyTask = new Task<>() {
                @Override protected Boolean call() throws Exception { return adminService.grantPenalty(report.getStId(), report.getReason(), report.getSeatIndex()); }
            };
            penaltyTask.setOnSucceeded(e -> {
                if(penaltyTask.getValue()) { showAlert(AlertType.INFORMATION,"성공","패널티 부여 완료"); loadUserReportsInBackground(); loadAdminPenaltiesInBackground();}
                else showAlert(AlertType.ERROR,"실패","DB 오류");
            });
            new Thread(penaltyTask).start();
        }
    }
    
    @FXML
    void handleEject(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        Integer userId = selectedSeat.getCurrentUserId();
        if (userId == null || userId == 0) { showAlert(AlertType.WARNING, "알림", "이용자가 없습니다."); return; }
        String reason = actionField.getText();
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("강제 퇴실 확인");
        confirmAlert.setContentText("정말로 강제 퇴실시키겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) { 
            Task<Boolean> ejectTask = new Task<>() {
                @Override protected Boolean call() throws Exception { return adminService.forceEjectUser(userId, reason); }
            };
            ejectTask.setOnSucceeded(e -> {
                if (ejectTask.getValue()) {
                    showAlert(AlertType.INFORMATION, "성공", "퇴실 처리 완료");
                    actionField.clear(); 
                    if (currentSelectedRoom != null) loadSeatsForRoomInBackground(currentSelectedFloor, currentSelectedRoom); 
                } else showAlert(AlertType.ERROR, "실패", "퇴실 실패");
            });
            new Thread(ejectTask).start();
        }
    }
    
    @FXML
    void handleToggleBroken(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        String currentStatus = selectedSeat.getStatus(); 
        String newStatus = ("E".equals(currentStatus) || "G".equals(currentStatus)) ? "C" : ("C".equals(currentStatus) ? "E" : null);
        if (newStatus == null) { showAlert(AlertType.WARNING, "변경 불가", "사용 중이거나 예약 중인 좌석은 변경할 수 없습니다."); return; }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("상태 변경 확인");
        confirmAlert.setContentText("상태를 변경하시겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            final String finalNewStatus = newStatus;
            final int finalSeatId = selectedSeat.getId();
            Task<Boolean> toggleTask = new Task<>() {
                @Override protected Boolean call() throws Exception { return adminService.setSeatStatus(finalSeatId, finalNewStatus); }
            };
            toggleTask.setOnSucceeded(e -> {
                if(toggleTask.getValue()) {
                    showAlert(AlertType.INFORMATION, "성공", "상태 변경 완료");
                    if (currentSelectedRoom != null) loadSeatsForRoomInBackground(currentSelectedFloor, currentSelectedRoom);
                } else showAlert(AlertType.ERROR, "실패", "상태 변경 실패");
            });
            new Thread(toggleTask).start();
        }
    }

    // ★(신규) 예약 명단 확인 버튼 클릭 시 실행
    @FXML
    void handleViewReservations(ActionEvent event) {
        if (selectedSeat == null) {
            showAlert(AlertType.WARNING, "알림", "예약 명단을 확인할 좌석을 먼저 선택하세요.");
            return;
        }
        
        // DAO에서 문자열 리스트로 변환된 예약 명단을 받아옴
        List<String> list = adminService.getReservations(selectedSeat.getId());
        
        StringBuilder msg = new StringBuilder();
        if (list.isEmpty()) {
            msg.append("현재 예약 대기자가 없습니다.");
        } else {
            for (int i = 0; i < list.size(); i++) {
                msg.append(i + 1).append(". ").append(list.get(i)).append("\n");
            }
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("예약 대기 명단");
        alert.setHeaderText("좌석 " + selectedSeat.getSeatNumber() + " 예약 현황");
        alert.setContentText(msg.toString());
        alert.showAndWait();
    }

    // 좌석 시각화
    private void renderVisualSeats(List<Seat> seatList, String roomName) {
        visualSeatGrid.getChildren().clear(); 
        int autoIndex = 0; 
        int columnsPerRow = 6; 

        for (Seat seat : seatList) {
            StackPane seatPane = createSeatPane(seat); 
            String seatNumber = seat.getSeatNumber(); 
            int colIndex = 0;
            int rowIndex = 0;

            if (seatNumber == null || seatNumber.trim().isEmpty()) {
                colIndex = autoIndex % columnsPerRow;
                rowIndex = autoIndex / columnsPerRow;
                Label label = (Label)((VBox)seatPane.getChildren().get(1)).getChildren().get(0);
                label.setText("NO." + seat.getId()); 
                label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                autoIndex++; 
            } else {
                try {
                    char rowChar = seatNumber.toUpperCase().charAt(0);
                    if (Character.isDigit(rowChar)) {
                        int num = Integer.parseInt(seatNumber);
                        rowIndex = (num - 1) / columnsPerRow;
                        colIndex = (num - 1) % columnsPerRow;
                    } else {
                        rowIndex = rowChar - 'A'; 
                        String colStr = seatNumber.substring(1);
                        colIndex = (colStr.isEmpty()) ? 0 : Integer.parseInt(colStr) - 1;
                    }
                } catch (Exception e) {
                    colIndex = autoIndex % columnsPerRow;
                    rowIndex = autoIndex / columnsPerRow;
                    autoIndex++;
                }
            }
            visualSeatGrid.add(seatPane, colIndex, rowIndex);
        }
    }

    // 좌석 UI 박스 생성
    private StackPane createSeatPane(Seat seat) {
        Rectangle rect = new Rectangle(100, 70); 
        rect.setStroke(Color.DARKGRAY);
        rect.setArcWidth(10);
        rect.setArcHeight(10);

        Label seatLabel = new Label(seat.getSeatNumber());
        seatLabel.setFont(new Font("Arial", 14)); 
        seatLabel.setStyle("-fx-font-weight: bold;");
        
        Label infoLabel = new Label();
        infoLabel.setFont(new Font("Arial", 11)); 
        
        // ★예약확인 버튼 (좌석 안에 삽입)
        Button reserveListBtn = new Button("예약확인");
        reserveListBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 5 2 5;");
        reserveListBtn.setOnAction(e -> {
            selectedSeat = seat; // 버튼 눌러도 선택된 것으로 처리
            handleViewReservations(null);
        });

        String status = seat.getStatus();
        // DAO가 채워 넣은 값을 가져옴 (사용자 이름 또는 예약자 이름)
        String nameValue = seat.getCurrentUserName(); 
        
        if ("U".equals(status)) {
            // [사용 중] 파란색
            rect.setFill(Color.GRAY);
            infoLabel.setText(nameValue != null ? nameValue : "사용자");
        } else {
            // [미사용]
            if (nameValue != null && !nameValue.isEmpty()) {
                // 이름이 있으면 예약자임 -> 노란색
                rect.setFill(Color.LIGHTYELLOW);
                infoLabel.setText("예약: " + nameValue);
            } else {
                // 이름 없으면 빈 좌석 -> 회색
                rect.setFill(Color.LIGHTGRAY);
                infoLabel.setText("빈 좌석");
            }
        }
        
        VBox content = new VBox(3, seatLabel, infoLabel, reserveListBtn); 
        content.setAlignment(Pos.CENTER);
        
        StackPane seatPane = new StackPane(rect, content); 
        seatPane.setUserData(seat); 

        seatPane.setOnMouseClicked(event -> {
            // 버튼 클릭이 아닐 때만 좌석 선택 이벤트 발생
            if (!event.getTarget().equals(reserveListBtn)) {
                setSelectedSeat((Seat) seatPane.getUserData()); 
                highlightSelectedSeat(seatPane); 
            }
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
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}