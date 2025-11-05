package controller.kiosk;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SeatController implements Initializable {

    // --- FXML 요소들과 연결할 변수들 ---
    
    // 층 선택 버튼
    @FXML private Button floor4Button;
    @FXML private Button floor7Button;

    // 회의실/개인좌석 선택 버튼
    @FXML private Button meetingRoom1Button;
    @FXML private Button meetingRoom2Button; // "회의실 2" 버튼 추가
    @FXML private Button individualSeatButton;

    // 좌석들을 담고 있는 컨테이너
    @FXML private GridPane seatGrid;

    // 각 좌석 버튼 리스트
    private List<Button> seatButtons = new ArrayList<>();
    
    // 선택된 좌석 정보 라벨
    @FXML private Label selectedSeatLabel;

    // 홈으로 돌아가기 버튼
    @FXML private Button homeButton;
    
    // 현재 선택된 좌석을 추적하기 위한 변수
    private Button currentSelectedSeat = null;

 // --- 초기화 메서드 ---
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SeatController가 성공적으로 로드되었습니다.");
        
        // GridPane에서 좌석 버튼들을 찾아 리스트에 추가
        if (seatGrid != null) {
            seatGrid.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    Button seatBtn = (Button) node;
                    // FXML에 fx:id가 없는 좌석 버튼도 처리하도록 조건 변경
                    // (텍스트가 숫자인 버튼들을 좌석 버튼으로 간주)
                    try {
                        Integer.parseInt(seatBtn.getText()); // 텍스트가 숫자로 변환되면 좌석 버튼
                        seatButtons.add(seatBtn);
                        seatBtn.setOnAction(event -> handleSeatSelection(seatBtn)); 
                    } catch (NumberFormatException e) {
                        // 숫자가 아닌 버튼 (예: 다른 컨트롤)은 무시
                    }
                }
            });
        }
        
        // 초기 설정: 이미지와 같이 '개인좌석' 버튼이 기본 선택되도록 변경
        if(individualSeatButton != null) {
            // 이전에 'selected-room-button'이 있다면 제거하고 다시 추가
            individualSeatButton.getStyleClass().remove("selected-room-button");
            individualSeatButton.getStyleClass().add("selected-room-button");
        }
        
        // 초기 좌석 상태 로드 (DB 연동 등)
        loadSeatStatus();
    }

    // --- 이벤트 핸들러 메서드 ---

    // 층 선택 버튼 (4층, 7층) 클릭 시 호출
    @FXML
    private void handleFloorSelection(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        
        // 모든 층 버튼의 'selected' 스타일 제거
        floor4Button.getStyleClass().remove("selected-floor-button");
        floor7Button.getStyleClass().remove("selected-floor-button");
        
        // 클릭된 버튼에 'selected' 스타일 추가
        clickedButton.getStyleClass().add("selected-floor-button");
        
        String selectedFloor = clickedButton.getText().substring(0, 1);
        System.out.println(selectedFloor + "층이 선택되었습니다.");
        
        updateSeatsForFloor(selectedFloor);
    }
    
    // 회의실/개인좌석 선택 버튼 클릭 시 호출
    @FXML
    private void handleRoomSelection(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        
        // 모든 룸 타입 버튼의 'selected' 스타일 제거
        meetingRoom1Button.getStyleClass().remove("selected-room-button");
        meetingRoom2Button.getStyleClass().remove("selected-room-button"); // '회의실 2' 추가
        individualSeatButton.getStyleClass().remove("selected-room-button");
        
        // 클릭된 버튼에 'selected' 스타일 추가
        clickedButton.getStyleClass().add("selected-room-button");
        
        String selectedRoomType = clickedButton.getText();
        System.out.println(selectedRoomType + "이 선택되었습니다.");
        
        filterSeatsByType(selectedRoomType);
    }
    
    // 개별 좌석 버튼 클릭 시 호출
    private void handleSeatSelection(Button clickedButton) {
        // 이 좌석이 이미 사용 중인(occupied) 좌석인지 확인 (CSS 클래스 기준)
        if (clickedButton.getStyleClass().contains("occupied-seat")) {
            System.out.println("이미 사용 중인 좌석입니다.");
            return; // 사용 중인 좌석은 선택 불가
        }

        // 이전에 선택했던 좌석이 있다면, 'selected-seat' 스타일 제거
        if (currentSelectedSeat != null && currentSelectedSeat != clickedButton) {
            currentSelectedSeat.getStyleClass().remove("selected-seat");
            // 만약 이전에 선택했던 좌석이 'available-seat'였다면 다시 추가
            if (!currentSelectedSeat.getStyleClass().contains("available-seat")) {
                 currentSelectedSeat.getStyleClass().add("available-seat");
            }
        }
        
        // 현재 클릭한 좌석의 'available-seat' 스타일을 제거하고 'selected-seat' 스타일 추가
        clickedButton.getStyleClass().remove("available-seat");
        clickedButton.getStyleClass().add("selected-seat");
        
        // 현재 선택된 좌석을 이 버튼으로 업데이트
        currentSelectedSeat = clickedButton;

        String seatText = clickedButton.getText();
        System.out.println("좌석 " + seatText + "이(가) 선택되었습니다.");
        
        // 상단 '좌석 01' 라벨 업데이트
        if (selectedSeatLabel != null) {
            selectedSeatLabel.setText("좌석 " + seatText);
        }
    }
    
    // 홈으로 돌아가기 버튼 클릭 시 호출
    @FXML
    private void handleHomeButton() {
        try {
            // LoginView.fxml 경로가 bin 폴더 기준이 아닌 src 폴더 기준으로 올바르게 되었는지 확인하세요.
            // 보통 /view/kiosk/LoginView.fxml 입니다.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/kiosk/LoginView.fxml")); 
            Parent root = loader.load();
            
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("로그인");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("홈 화면으로 이동 중 오류 발생: " + e.getMessage());
        }
    }

    // --- 보조 메서드 ---

    private void loadSeatStatus() {
        System.out.println("좌석 상태를 로드 중...");
        for (Button seatBtn : seatButtons) {
            boolean isOccupied = Math.random() < 0.3; // 30% 확률로 사용 중 (DB 연동 대체)
            if (isOccupied) {
                seatBtn.getStyleClass().add("occupied-seat");
                // seatBtn.setDisable(true); // 클릭 자체를 막을 수도 있음
            } else {
                seatBtn.getStyleClass().add("available-seat");
            }
        }
    }
    
    private void updateSeatsForFloor(String floor) {
        System.out.println(floor + "층 좌석으로 업데이트합니다.");
        // TODO: DB에서 해당 층의 좌석 정보를 다시 loadSeatStatus() 같은 메서드로 불러오는 로직
    }
    
    private void filterSeatsByType(String roomType) {
        System.out.println(roomType + " 타입 좌석으로 필터링합니다.");
        // TODO: DB에서 해당 룸타입의 좌석 정보를 다시 불러오는 로직
    }
}