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
            Parent root = FXMLLoader.load(getClass().getResource("view/kiosk/LoginView.fxml")); 
            
            Scene scene = new Scene(root, 1400, 800); 
            
            primaryStage.setTitle("키오스크");
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
