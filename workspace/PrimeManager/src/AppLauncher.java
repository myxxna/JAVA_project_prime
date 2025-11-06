import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // FXML 파일 로드 경로를 확인하세요.
            Parent root = FXMLLoader.load(getClass().getResource("view/kiosk/LoginView.fxml"));
            
            // ⚠️ FXML에서 테두리 요청에 따른 UI 개선을 위해 Scene 크기를 조정할 수 있습니다.
            Scene scene = new Scene(root, 900, 650); 
            
            primaryStage.setTitle("스터디 카페 키오스크");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("FXML 파일을 로드할 수 없습니다. 경로를 확인하세요.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
