import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppLauncher extends Application {

    @Override

    public void start(Stage primaryStage) throws IOException {
        // FXML 경로가 'src/view/kiosk/LoginView.fxml'이라고 가정하고 로드합니다.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/kiosk/LoginView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("프라임실 좌석 예약 로그인");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // JavaFX 앱 시작
    }

}
