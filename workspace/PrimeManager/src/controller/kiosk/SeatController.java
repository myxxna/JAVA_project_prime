// controller.kiosk 패키지에 위치하는지 확인
package controller.kiosk; 

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class SeatController implements Initializable {

    // FXML에서 정의한 fx:id가 없으므로 @FXML 필드를 모두 제거합니다.

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 초기화 로직은 비워둡니다.
        System.out.println("SeatController 초기화됨.");
    }
}