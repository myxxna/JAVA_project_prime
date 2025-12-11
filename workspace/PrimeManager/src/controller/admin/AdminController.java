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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn; 
import javafx.scene.control.TableView; 
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton; 
import javafx.scene.control.ToggleGroup;  
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell; 
import javafx.scene.layout.HBox;
import javafx.util.Callback; 
import javafx.scene.layout.BorderPane; 
import javafx.scene.layout.GridPane; 
import javafx.scene.layout.StackPane; 
import javafx.scene.layout.TilePane; 
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color; 
import javafx.scene.text.Font; 
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment; 
import javafx.stage.Stage; 
import model.Seat;
import model.Penalty; 
import model.User; 
import service.AdminService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 
import java.time.temporal.ChronoUnit; 
import java.util.List;
import java.util.ArrayList; 
import java.util.Optional; 
import javafx.concurrent.Task; 
import javafx.scene.effect.DropShadow; 

public class AdminController {

    private AdminService adminService; 
    private Seat selectedSeat = null; 

    private int currentSelectedFloor = 0; 
    private String currentSelectedRoom = null;

    @FXML private BorderPane adminRootPane; 
    @FXML private TilePane floorButtonContainer; 
    @FXML private VBox roomButtonContainer; 
    @FXML private GridPane visualSeatGrid; 
    @FXML private ListView<String> overdueUserList; 
    @FXML private Label selectedSeatLabel;
    @FXML private TextField actionField; 
    
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
        
        // 1. 노쇼 자동 처리
        adminService.processNoShow(); 

        // 2. 화면 구성 요소 로드 (버튼, 테이블 등)
        createFloorButtons(); 
        setupReportTableColumns(); 
        setupAdminPenaltyTableColumns(); 
        loadUserReportsInBackground(); 
        loadAdminPenaltiesInBackground(); 
        
        // 3. 우측 시간 초과자 목록 로드
        setupOverdueList(); 
        loadOverdueUsers(); 
        
        // 4. 창 크기 조절 시 화면 중앙 정렬 리스너
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

    // --- [핵심 기능] 신고 목록 패널티 부여 (로그 추적 후 팝업) ---
    private void handleGrantPenaltyFromReport(Penalty report) {
        Task<User> findOffenderTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // DAO를 통해 그 시간대 실제 사용자(범인)를 찾습니다.
                return adminService.getOffenderByLog(report.getSeatIndex(), report.getReportTime());
            }
        };

        findOffenderTask.setOnSucceeded(e -> {
            User offender = findOffenderTask.getValue();
            
            if (offender != null) {
                // 범인을 찾았으면 팝업을 띄워 관리자에게 확인시킵니다.
                Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
                confirmAlert.setTitle("사용자 확인 및 패널티 부여");
                confirmAlert.setHeaderText("신고 시간대 실제 사용자 발견");
                confirmAlert.setContentText("좌석: " + report.getSeatIndex() + "번\n" +
                                            "발견된 사용자: " + offender.getName() + " (" + offender.getStudentId() + ")\n\n" +
                                            "이 사용자에게 패널티를 부여하시겠습니까?");
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // 확인 누르면 범인의 PK(id)로 패널티 부여
                    executePenalty(offender.getId(), report.getReason(), report.getSeatIndex());
                }
            } else {
                showAlert(AlertType.WARNING, "사용자 확인 불가", "해당 시간대의 입실 로그를 찾을 수 없습니다.\n정확한 사용자를 특정하지 못했습니다.");
            }
        });
        
        findOffenderTask.setOnFailed(e -> {
            showAlert(AlertType.ERROR, "오류", "로그 조회 중 오류 발생");
            e.getSource().getException().printStackTrace();
        });

        new Thread(findOffenderTask).start();
    }
    
    // 실제 패널티 부여 실행 (재사용 메서드)
    private void executePenalty(int userId, String reason, int seatIndex) {
        Task<Boolean> penaltyTask = new Task<>() {
            @Override protected Boolean call() throws Exception { 
                return adminService.grantPenalty(userId, reason, seatIndex); 
            }
        };
        penaltyTask.setOnSucceeded(e -> {
            if(penaltyTask.getValue()) { 
                showAlert(AlertType.INFORMATION, "성공", "패널티 부여 완료"); 
                loadUserReportsInBackground(); 
                loadAdminPenaltiesInBackground();
            } else {
                showAlert(AlertType.ERROR, "실패", "패널티 부여 실패 (DB 오류)");
            }
        });
        new Thread(penaltyTask).start();
    }

    // --- [UI 및 테이블 설정] ---

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
    
    // --- [우측 시간 초과자 목록 관리] ---

    private void setupOverdueList() {
        overdueUserList.setCellFactory(param -> new ListCell<String>() {
            private final Button btn = new Button("패널티");
            private final HBox pane = new HBox(10);
            private final Label label = new Label();
            {
                btn.setStyle("-fx-background-color: #ff5c5c; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                pane.setAlignment(Pos.CENTER_LEFT); 
                pane.getChildren().addAll(label, btn);
                
                btn.setOnAction(event -> {
                    String item = getItem();
                    if (item != null) {
                        String[] parts = item.split(",");
                        if(parts.length >= 5) {
                            handleAutoPenalty(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), "시간초과(30분)", Integer.parseInt(parts[4]));
                        }
                    }
                });
            }
            @Override 
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setGraphic(null); setText(null); 
                } else {
                    try {
                        String[] parts = item.split(",");
                        if(parts.length >= 5) {
                            String timeShort = parts[3].length() > 16 ? parts[3].substring(11, 16) : parts[3];
                            String displayText = parts[2] + " (좌석:" + parts[1] + ") - " + timeShort;
                            label.setText(displayText); 
                            setGraphic(pane);
                        } else { 
                            setText(item); setGraphic(null); 
                        }
                    } catch (Exception e) { setText(item); setGraphic(null); }
                }
            }
        });
    }

    // 시간 초과자 패널티 부여 (자동 처리)
    private void handleAutoPenalty(int userId, int seatIdx, String reason, int resId) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                boolean success = adminService.grantPenalty(userId, reason, seatIdx);
                if (success) adminService.checkPenaltyDone(resId);
                return success;
            }
        };
        
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showAlert(AlertType.INFORMATION, "처리 완료", "패널티 부여 완료.");
                loadOverdueUsers(); 
                loadAdminPenaltiesInBackground(); 
            } else {
                showAlert(AlertType.ERROR, "오류", "패널티 부여 실패");
            }
        });
        
        new Thread(task).start();
    }
    
    // 시간 초과자 목록 불러오기
    private void loadOverdueUsers() {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return adminService.getOverdueUsers();
            }
        };

        task.setOnSucceeded(e -> {
            List<String> list = task.getValue();
            if (list == null || list.isEmpty()) {
                overdueUserList.setItems(FXCollections.observableArrayList());
                overdueUserList.setPlaceholder(new Label("시간 초과자 없음"));
            } else {
                overdueUserList.setItems(FXCollections.observableArrayList(list));
            }
        });

        task.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }
    
    // --- [층/룸 버튼 생성 메서드] --- (이 부분이 빠져서 오류가 났었습니다)

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
    
    // --- [좌석 시각화 및 정보 표시] ---

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
            String seatNum = seat.getSeatNumber();
            String status = seat.getStatus(); 
            String nameValue = seat.getCurrentUserName(); 
            
            String infoText = "좌석: " + seatNum;

            if ("U".equals(status)) {
                infoText += "  |  사용자: " + (nameValue != null ? nameValue.replace("\n", " ") : "없음");
                if (seat.getStartTime() != null && seat.getEndTime() != null) {
                    String start = seat.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    String end = seat.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    infoText += "  |  이용 시간: " + start + " ~ " + end;
                }
            } else if ("R".equals(status)) {
                infoText += "  |  예약자: " + (nameValue != null ? nameValue.replace("\n", " ") : "");
            } else if ("M".equals(status)) {
                infoText += "  |  상태: 점검중";
            } else {
                infoText += "  |  상태: 빈 좌석";
            }
            selectedSeatLabel.setText(infoText);
        } else {
            selectedSeatLabel.setText(currentSelectedRoom != null ? currentSelectedRoom + "의 좌석을 클릭하세요." : "먼저 층과 룸을 선택하세요.");
        }
    }

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
                Label label = (Label)((VBox)seatPane.getChildren().get(0)).getChildren().get(0);
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

    private StackPane createSeatPane(Seat seat) {
        StackPane seatPane = new StackPane();
        seatPane.setPrefSize(90, 70); 
        
        Label seatLabel = new Label(seat.getSeatNumber());
        seatLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18)); 
        
        Label userLabel = new Label();
        userLabel.setFont(Font.font("Arial", 9)); 
        userLabel.setTextAlignment(TextAlignment.CENTER); 
        
        String status = seat.getStatus();
        String nameValue = seat.getCurrentUserName(); 
        
        String COLOR_WHITE = "-fx-background-color: white; -fx-border-color: #dcdcdc; -fx-text-fill: black;";
        String COLOR_RED   = "-fx-background-color: #e04f5f; -fx-border-color: #e04f5f; -fx-text-fill: white;";
        String COLOR_GRAY  = "-fx-background-color: #d3d3d3; -fx-border-color: #d3d3d3; -fx-text-fill: #777777;";
        String COLOR_BLUE  = "-fx-background-color: #4facfe; -fx-border-color: #4facfe; -fx-text-fill: white;"; 

        String commonStyle = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-width: 1.5; " +
                             "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";

        boolean isReservedSoon = false;
        if (seat.getStartTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), seat.getStartTime());
            if (minutes >= 0 && minutes <= 30) isReservedSoon = true;
        }

        if ("U".equals(status)) {
            seatPane.setStyle(commonStyle + COLOR_BLUE);
            seatLabel.setStyle("-fx-text-fill: white;");
            userLabel.setStyle("-fx-text-fill: white;");
            userLabel.setText(nameValue != null ? nameValue : "사용중"); 
        } else if ("M".equals(status)) {
            seatPane.setStyle(commonStyle + COLOR_GRAY);
            seatLabel.setStyle("-fx-text-fill: #666666;");
        } else if ("R".equals(status) || isReservedSoon) {
            seatPane.setStyle(commonStyle + "-fx-background-color: #fffacd; -fx-border-color: #f0e68c; -fx-text-fill: black;");
            userLabel.setText("예약중");
        } else {
            seatPane.setStyle(commonStyle + COLOR_WHITE);
        }

        VBox content = new VBox(2); 
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(seatLabel, userLabel); 
        seatPane.getChildren().add(content);
        seatPane.setUserData(seat); 

        seatPane.setOnMouseClicked(event -> {
            setSelectedSeat((Seat) seatPane.getUserData()); 
            highlightSelectedSeat(seatPane); 
        });
        
        seatPane.setOnMouseEntered(e -> seatPane.setOpacity(0.8));
        seatPane.setOnMouseExited(e -> seatPane.setOpacity(1.0));

        return seatPane;
    }
    
    private void highlightSelectedSeat(StackPane clickedSeatPane) {
        for (javafx.scene.Node node : visualSeatGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane pane = (StackPane) node;
                pane.setEffect(new DropShadow(5, javafx.scene.paint.Color.rgb(0,0,0,0.1))); 
                pane.setScaleX(1.0);
                pane.setScaleY(1.0);
            }
        }
        clickedSeatPane.setEffect(new DropShadow(15, javafx.scene.paint.Color.DODGERBLUE));
        clickedSeatPane.setScaleX(1.05); 
        clickedSeatPane.setScaleY(1.05);
    }

    // --- [나머지 버튼 이벤트 핸들러] ---

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
            executePenalty(userId, reason, seatIndex);
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
        String newStatus = null;
        if ("A".equals(currentStatus)) { newStatus = "M"; } else if ("M".equals(currentStatus)) { newStatus = "A"; }

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

    @FXML
    void handleViewReservations(ActionEvent event) {
        if (selectedSeat == null) {
            showAlert(AlertType.WARNING, "알림", "예약 명단을 확인할 좌석을 먼저 선택하세요.");
            return;
        }
        
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