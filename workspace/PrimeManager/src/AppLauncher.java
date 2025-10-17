import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main.java의 코드를 그대로 복사하여 실행 진입점을 통일합니다.
public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("1. start() 메소드 시작");
        try {
            System.out.println("2. FXML 파일 로드를 시도합니다...");
            URL fxmlUrl = getClass().getResource("/view/admin/AdminView.fxml");
            
            if (fxmlUrl == null) {
                System.out.println("오류: FXML 파일을 찾을 수 없습니다. 경로를 확인하세요.");
                return; // FXML이 없으면 더 이상 진행하지 않음
            }
            System.out.println("3. FXML 파일 경로 찾기 성공: " + fxmlUrl);
            
            Parent root = FXMLLoader.load(fxmlUrl);
            System.out.println("4. FXML 로드 성공. root 객체: " + root);
            
            Scene scene = new Scene(root);
            System.out.println("5. Scene 생성 성공.");
            
            primaryStage.setTitle("Prime Manager - 관리자 시스템");
            primaryStage.setScene(scene);
            
            System.out.println("6. Stage에 Scene 설정 완료. 화면을 표시합니다...");
            primaryStage.show();
            System.out.println("7. show() 메소드 호출 완료.");
            
        } catch (Exception e) {
            System.out.println("오류: UI를 로드하는 중 예외가 발생했습니다.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("0. main() 메소드 시작, launch() 호출");
        launch(args);
    }
}
