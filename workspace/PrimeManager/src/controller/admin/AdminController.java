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
import javafx.scene.control.TableCell; // 버튼 셀을 위해 임포트
import javafx.util.Callback; // 버튼 셀을 위해 임포트
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

import javafx.concurrent.Task; // 백그라운드 작업을 위해 Task 임포트

/**
 * (★수정★) '신고 목록'과 '패널티 관리 목록' 탭 분리
 */
public class AdminController {

    private AdminService adminService; 
    private Seat selectedSeat = null; 

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private BorderPane adminRootPane;     
    @FXML private TilePane floorButtonContainer; 
    @FXML private VBox roomButtonContainer; 
    
    @FXML private GridPane visualSeatGrid; 
    @FXML private ListView<String> overdueUserList; 
    @FXML private Label selectedSeatLabel;
    @FXML private TextField actionField; 
    @FXML private Button penaltyButton;
    @FXML private Button ejectButton;
    @FXML private Button toggleBrokenButton;

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
    private String currentSelectedRoom = null; 

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
                        
                        // 1. 창의 크기가 변경(설정)된 후에 중앙으로 이동
                        stage.widthProperty().addListener((o, oldVal, newVal) -> {
                            Platform.runLater(stage::centerOnScreen);
                        });
                        
                        // 2. 창이 보여질 때 중앙으로 이동
                        stage.setOnShown(e -> {
                            Platform.runLater(stage::centerOnScreen);
                        });

                        // 3. 이미 창이 떠있는 상태라면 즉시 이동
                        if (stage.isShowing()) {
                            Platform.runLater(stage::centerOnScreen);
                        }
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
            // (★수정★) 기본 스타일 - 흰색 배경에 검정 글씨
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
        List<String> rooms = adminService.getRoomsByFloor(floor);
        roomButtonContainer.getChildren().clear();
        visualSeatGrid.getChildren().clear(); 
        
        for (String room : rooms) {
            ToggleButton btn = new ToggleButton(room);
            btn.setToggleGroup(roomGroup);
            btn.setMaxWidth(Double.MAX_VALUE); 
            btn.setPrefHeight(40);
            // (★수정★) 기본 스타일 - 흰색 배경에 검정 글씨
            btn.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-font-weight: bold;");
            
            btn.setOnAction(e -> {
                if (btn.isSelected()) {
                    updateButtonStyles(roomGroup);
                    currentSelectedRoom = room;
                    loadSeatsForRoomInBackground(room);
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
                // (★수정★) 선택된 버튼 스타일 - 파란색 배경에 흰색 글씨
                btn.setStyle("-fx-background-color: #5c9aff; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
            } else {
                // (★수정★) 선택 안 된 버튼 스타일 - 흰색 배경에 검정 글씨
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
                        // (신규) 버튼 스타일
                        btn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
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
    
    /**
     * (★수정★)
     * '사용자 신고' 목록을 백그라운드 스레드로 불러와 'reportTable'을 채웁니다.
     */
    private void loadUserReportsInBackground() {
        Task<List<Penalty>> loadPenaltiesTask = new Task<>() {
            @Override
            protected List<Penalty> call() throws Exception {

                // (수정) 사용자 신고 목록만 가져옴
                return adminService.getAllUserReports();
            }
        };

        loadPenaltiesTask.setOnSucceeded(e -> {
            List<Penalty> penalties = loadPenaltiesTask.getValue();
            reportTable.setItems(FXCollections.observableArrayList(penalties));
        });

        loadPenaltiesTask.setOnFailed(e -> {
            showAlert(AlertType.ERROR, "오류", "사용자 신고 목록 로드 실패: " + loadPenaltiesTask.getException().getMessage());
            loadPenaltiesTask.getException().printStackTrace();
        });

        new Thread(loadPenaltiesTask).start();
    }

    /**
     * (★신규★)
     * '관리자 부여 패널티' 목록을 백그라운드 스레드로 불러와 'adminPenaltyTable'을 채웁니다.
     */

    private void loadAdminPenaltiesInBackground() {
        Task<List<Penalty>> loadAdminPenaltiesTask = new Task<>() {
            @Override
            protected List<Penalty> call() throws Exception {
                // (신규) 관리자 부여 목록만 가져옴
                return adminService.getAllAdminPenalties();
            }
        };

        loadAdminPenaltiesTask.setOnSucceeded(e -> {
            List<Penalty> penalties = loadAdminPenaltiesTask.getValue();
            adminPenaltyTable.setItems(FXCollections.observableArrayList(penalties));
        });

        loadAdminPenaltiesTask.setOnFailed(e -> {
            showAlert(AlertType.ERROR, "오류", "관리자 패널티 목록 로드 실패: " + loadAdminPenaltiesTask.getException().getMessage());
            loadAdminPenaltiesTask.getException().printStackTrace();
        });

        new Thread(loadAdminPenaltiesTask).start();
    }
    
    // --- (좌석 관련 핵심 로직) ---
    
    private void loadSeatsForRoomInBackground(String roomName) {
        visualSeatGrid.getChildren().clear(); 

        Task<List<Seat>> loadSeatsTask = new Task<>() {
            @Override
            protected List<Seat> call() throws Exception {
                return adminService.getSeatsByRoom(roomName);
            }
        };

        loadSeatsTask.setOnSucceeded(e -> {
            List<Seat> seatList = loadSeatsTask.getValue();
            renderVisualSeats(seatList, roomName); 
        });

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
            LocalDateTime startTime = selectedSeat.getStartTime();
            String startTimeStr = (startTime != null) ? startTime.format(TIME_FORMATTER) : "N/A";
            
            String combinedInfo = selectedSeat.getCurrentUserName();
    /**
     * (★임시 해결책★)
     * "이름|학번" 문자열을 분리하여 하단 상세 정보(selectedSeatLabel)에 표시합니다.
     */

            String userName = "";
            String studentId = "";

            if (combinedInfo != null && combinedInfo.contains("|")) {
                String[] parts = combinedInfo.split("\\|"); 
                userName = (parts.length > 0 && !parts[0].isEmpty()) ? parts[0] : "";
                studentId = (parts.length > 1 && !parts[1].isEmpty()) ? parts[1] : "";
            }

            String userDisplay;
            if (!userName.isEmpty() && !studentId.isEmpty()) {
                userDisplay = userName + " (학번: " + studentId + ")";
            } else if (!studentId.isEmpty()) {
                userDisplay = "학번: " + studentId; 
            } else if (!userName.isEmpty()) {
                 userDisplay = userName;
            } else {
                userDisplay = "정보 없음";
            }

            switch (status) {
                case "U": 
                    selectedSeatLabel.setText("좌석: " + seatNum + " (사용중, " + userDisplay + ", 시작: " + startTimeStr + ")");
                    break;
                case "R": 
                    selectedSeatLabel.setText("좌석: " + seatNum + " (예약됨, " + userDisplay + ")");
                    break;
                case "E": 
                    selectedSeatLabel.setText("좌석: " + seatNum + " (사용 가능)");
                    break;
                case "C": 
                    selectedSeatLabel.setText("좌석: " + seatNum + " (점검 중)");
                    break;
                case "G": 
                    selectedSeatLabel.setText("좌석: " + seatNum + " (사용 가능)");
                    break;
                default:
                    selectedSeatLabel.setText("좌석: " + seatNum + " (알 수 없음 - Status: " + status + ")");
                    break;
            }
        } else {
            String selectedRoom = currentSelectedRoom; // (수정) 현재 선택된 방 이름 사용
            if(selectedRoom != null) {
                selectedSeatLabel.setText(selectedRoom + " 룸의 좌석을 클릭하세요.");
            } else {
                selectedSeatLabel.setText("먼저 층과 룸을 선택한 후, 좌석을 클릭하세요.");
            }
        }
    }

    /**
     * '좌석 현황' 탭의 '패널티 부여' 버튼 로직 (백그라운드 Task 실행)
     */
    @FXML
    void handlePenalty(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        
        final Integer userId = selectedSeat.getCurrentUserId();
        final int seatIndex = selectedSeat.getSeatIndex(); 
        final String reason = actionField.getText();

        if (userId == null || userId == 0) { 
            showAlert(AlertType.WARNING, "알림", "선택한 좌석은 현재 이용자가 없거나 회원 ID 정보가 없습니다."); 
            return; 
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            showAlert(AlertType.ERROR, "오류", "패널티 부여 사유를 입력하세요."); 
            return;
        }

        String seatNum = selectedSeat.getSeatNumber();
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("패널티 부여 확인");
        confirmAlert.setHeaderText("좌석: " + seatNum + " (이용자 ID: " + userId + ")");
        confirmAlert.setContentText("사유: " + reason + "\n정말로 이 사용자에게 패널티를 부여하시겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            Task<Boolean> penaltyTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return adminService.grantPenalty(userId, reason, seatIndex); 
                }
            };

            penaltyTask.setOnSucceeded(e -> {
                boolean success = penaltyTask.getValue();
                if (success) {
                    showAlert(AlertType.INFORMATION, "성공", "ID: " + userId + " 님에게 패널티를 부여했습니다.");
                    actionField.clear(); 
                    loadAdminPenaltiesInBackground(); 
                } else {
                    showAlert(AlertType.ERROR, "실패", "DB 오류. 패널티 부여에 실패했습니다.");
                }
            });

            penaltyTask.setOnFailed(e -> {
                showAlert(AlertType.ERROR, "심각한 오류", "패널티 부여 중 예외 발생: " + penaltyTask.getException().getMessage());
                penaltyTask.getException().printStackTrace();
            });

            new Thread(penaltyTask).start();
        }
    }
    
//    private void handleGrantPenaltyFromReport(Penalty report) {
//        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
//        confirmAlert.setTitle("패널티 부여 확인 (신고 기반)");
//        confirmAlert.setHeaderText("신고된 사용자 ID: " + report.getStId() + " (학번: " + report.getStudentRealId() + ")");
//        confirmAlert.setContentText("신고 사유: " + report.getReason() + "\n정말로 이 사용자에게 패널티를 부여하시겠습니까?");
//        Optional<ButtonType> result = confirmAlert.showAndWait();
//
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            
//            Task<Boolean> penaltyTask = new Task<>() {
//                @Override
//                protected Boolean call() throws Exception {
//                    return adminService.grantPenalty(report.getStId(), report.getReason(), report.getSeatIndex()); 
//                }
//            };
//
//            penaltyTask.setOnSucceeded(e -> {
//                boolean success = penaltyTask.getValue();
//                if (success) {
//                    showAlert(AlertType.INFORMATION, "성공", "ID: " + report.getStId() + " 님에게 패널티를 부여했습니다.");
//                    loadUserReportsInBackground(); 
//                    // (수정) '관리자 패널티 목록' 탭을 새로고침
//                    loadAdminPenaltiesInBackground(); 
//                } else {
//                    showAlert(AlertType.ERROR, "실패", "DB 오류. 패널티 부여에 실패했습니다.");
//                }
//            });
//
//            penaltyTask.setOnFailed(e -> {
//                showAlert(AlertType.ERROR, "심각한 오류", "패널티 부여 중 예외 발생: " + penaltyTask.getException().getMessage());
//                penaltyTask.getException().printStackTrace();
//            });
//
//            new Thread(penaltyTask).start();
//        }
//    }
    
    /**
     * (★신규★)
     * '신고 목록' 탭의 '패널티 부여' 버튼을 눌렀을 때 실행되는 핸들러입니다.
     * @param report '신고 목록' 테이블에서 선택된 Penalty 객체
     */
    private void handleGrantPenaltyFromReport(Penalty report) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("패널티 부여 확인 (신고 기반)");
        confirmAlert.setHeaderText("신고된 사용자 ID: " + report.getStId() + " (학번: " + report.getStudentRealId() + ")");
        confirmAlert.setContentText("신고 사유: " + report.getReason() + "\n정말로 이 사용자에게 패널티를 부여하시겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            Task<Boolean> penaltyTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    // (userId, reason, seatIndex)
                    return adminService.grantPenalty(report.getStId(), report.getReason(), report.getSeatIndex()); 
                }
            };

            penaltyTask.setOnSucceeded(e -> {
                boolean success = penaltyTask.getValue();
                if (success) {
                    showAlert(AlertType.INFORMATION, "성공", "ID: " + report.getStId() + " 님에게 패널티를 부여했습니다.");
                    // ★ 양쪽 테이블 모두 새로고침
                    loadUserReportsInBackground(); // '신고 목록' 탭 (사용자 신고)
                    loadAdminPenaltiesInBackground(); // '패널티 관리' 탭 (관리자 부여)
                } else {
                    showAlert(AlertType.ERROR, "실패", "DB 오류. 패널티 부여에 실패했습니다.");
                }
            });

            penaltyTask.setOnFailed(e -> {
                showAlert(AlertType.ERROR, "심각한 오류", "패널티 부여 중 예외 발생: " + penaltyTask.getException().getMessage());
                penaltyTask.getException().printStackTrace();
            });

            new Thread(penaltyTask).start();
        }
    }
    
    /**
     * 강제 퇴실 (백그라운드 Task 실행)
     */
    @FXML
    void handleEject(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        
        final Integer userId = selectedSeat.getCurrentUserId();
        
        if (userId == null || userId == 0) { 
            showAlert(AlertType.WARNING, "알림", "선택한 좌석은 현재 이용자가 없습니다."); 
            return; 
        }
        
        final String seatNum = selectedSeat.getSeatNumber();
        final String reason = actionField.getText();
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("강제 퇴실 확인");
        confirmAlert.setHeaderText("좌석: " + seatNum + " (이용자 ID: " + userId + ")");
        confirmAlert.setContentText("정말로 이 사용자를 강제 퇴실시키겠습니까?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) { 
            
            Task<Boolean> ejectTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return adminService.forceEjectUser(userId, reason);
                }
            };
            
            ejectTask.setOnSucceeded(e -> {
                if (ejectTask.getValue()) {
                    showAlert(AlertType.INFORMATION, "성공", "ID: " + userId + " 님을 강제 퇴실시켰습니다.");
                    actionField.clear(); 
                    loadSeatsForRoomInBackground(currentSelectedRoom); // (수정) 현재 선택된 방 이름 사용

                } else {
                    showAlert(AlertType.ERROR, "실패", "DB 오류. 강제 퇴실에 실패했습니다.");
                }
            });
            
            ejectTask.setOnFailed(e -> {
                 showAlert(AlertType.ERROR, "심각한 오류", "퇴실 처리 중 예외 발생: " + ejectTask.getException().getMessage());
                 ejectTask.getException().printStackTrace();
            });

            new Thread(ejectTask).start();
        }
    }
    

    /**
     * 좌석 상태 변경 (백그라운드 Task 실행)
     */
    @FXML
    void handleToggleBroken(ActionEvent event) {
        if (selectedSeat == null) { showAlert(AlertType.ERROR, "오류", "먼저 좌석을 선택하세요."); return; }
        
        String currentStatus = selectedSeat.getStatus(); 
        String newStatus = null;
        String confirmText = null;

        if ("E".equals(currentStatus) || "G".equals(currentStatus)) { 
            newStatus = "C"; 
            confirmText = "이 좌석을 '점검 중(Ｃ)' 상태로 변경하시겠습니까?";
        } 
        else if ("C".equals(currentStatus)) { 
            newStatus = "E"; 
            confirmText = "이 좌석을 '사용 가능(Ｅ)' 상태로 변경하시겠습니까?";
        } 
        else {
            showAlert(AlertType.WARNING, "변경 불가", "사용 중('U')이거나 예약 중('R')인 좌석은\n점검 상태로 변경할 수 없습니다.");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("좌석 상태 변경 확인");
        confirmAlert.setHeaderText("좌석: " + selectedSeat.getSeatNumber());
        confirmAlert.setContentText(confirmText);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            final String finalNewStatus = newStatus;
            final int finalSeatId = selectedSeat.getId();

            Task<Boolean> toggleTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return adminService.setSeatStatus(finalSeatId, finalNewStatus); 
                }
            };
            
            toggleTask.setOnSucceeded(e -> {
                if(toggleTask.getValue()) {
                    showAlert(AlertType.INFORMATION, "성공", "좌석 상태를 '" + finalNewStatus + "'(으)로 변경했습니다.");
                    loadSeatsForRoomInBackground(currentSelectedRoom); // (수정) 현재 선택된 방 이름 사용
                } else {
                    showAlert(AlertType.ERROR, "실패", "DB 오류. 상태 변경에 실패했습니다.");
                }
            });

            toggleTask.setOnFailed(e -> {
                 showAlert(AlertType.ERROR, "심각한 오류", "상태 변경 중 예외 발생: " + toggleTask.getException().getMessage());
                 toggleTask.getException().printStackTrace();
            });
            
            new Thread(toggleTask).start();
        }
    }
    /**
     * 좌석 렌더링 (UI 스레드에서 실행됨)
     */
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
        Rectangle rect = new Rectangle(90, 60); 
        rect.setStroke(Color.DARKGRAY);
        rect.setArcWidth(10);
        rect.setArcHeight(10);

        Label seatLabel = new Label(seat.getSeatNumber());
        seatLabel.setFont(new Font("Arial", 14)); 
        seatLabel.setStyle("-fx-font-weight: bold;");
        
        Label userLabel = new Label();
        userLabel.setFont(new Font("Arial", 10)); 
        
        Label timeLabel = new Label(); 
        timeLabel.setFont(new Font("Arial", 10)); 

        LocalDateTime startTime = seat.getStartTime();
        
        String combinedInfo = seat.getCurrentUserName();
        String userName = "";
        String studentId = "";

        if (combinedInfo != null && combinedInfo.contains("|")) {
             String[] parts = combinedInfo.split("\\|");
             userName = (parts.length > 0) ? parts[0] : "";
             studentId = (parts.length > 1) ? parts[1] : "";
        }

        String userDisplay = "";
        if (!userName.isEmpty() && !studentId.isEmpty()) {
            userDisplay = userName + " (" + studentId + ")"; 
        } else if (!studentId.isEmpty()) {
            userDisplay = "학번: " + studentId; 
        } else if (!userName.isEmpty()) {
            userDisplay = userName; 
        }

        switch (seat.getStatus()) {
            case "E": 
                rect.setFill(Color.LIGHTGREEN); 
                break;
                
            case "R": 
                rect.setFill(Color.LIGHTYELLOW);
                if (!userDisplay.isEmpty()) {
                    userLabel.setText(userDisplay);
                } else {
                    userLabel.setText("(예약됨)");
                }
                break;
            
            case "U": 
                rect.setFill(Color.DARKGRAY); 
                
                if (!userDisplay.isEmpty()) {
                    userLabel.setText(userDisplay);
                }

                if (startTime != null) {
                    timeLabel.setText(startTime.format(TIME_FORMATTER) + " 부터");
                }
                userLabel.setTextFill(Color.WHITE); 
                seatLabel.setTextFill(Color.WHITE); 
                timeLabel.setTextFill(Color.WHITE);
                break;
                
            case "C": 
                rect.setFill(Color.INDIANRED); 
                userLabel.setText("(점검 중)");
                userLabel.setTextFill(Color.WHITE); 
                seatLabel.setTextFill(Color.WHITE); 
                break;
                
            case "G": 
            default: 
                rect.setFill(Color.LIGHTGRAY); 
                break;
        }
        
        VBox content = new VBox(3, seatLabel, userLabel, timeLabel); 
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
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * (★참고★)
     * 이 메서드는 현재 사용되지 않는 것으로 보입니다.
     */
    private void handlePenaltyAction(ActionEvent event) {
        User loggedInUser = LoginController.getCurrentLoggedInUser();

        if (loggedInUser != null) {
            // User 모델에 getStudentId()가 존재한다고 가정
            // String studentIdToAssignPenalty = loggedInUser.getStudentId(); 
        }
    }
}