import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Seat;
import interfaces.ISeatDAO;
import impl.SeatDAOImpl;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        TableView<Seat> tableView = new TableView<>();

        // 컬럼 생성
        TableColumn<Seat, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("seatId"));

        TableColumn<Seat, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Seat, String> seatCol = new TableColumn<>("Seat");
        seatCol.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));

        TableColumn<Seat, Boolean> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availableCol.setCellFactory(CheckBoxTableCell.forTableColumn(availableCol));

        tableView.getColumns().addAll(idCol, roomCol, seatCol, availableCol);

        // DB에서 좌석 조회
        ISeatDAO seatDAO = new SeatDAOImpl();
        List<Seat> seats = seatDAO.getAllSeats();

        if (seats.isEmpty()) {
            System.out.println("조회된 좌석 정보가 없습니다.");
        } else {
            tableView.getItems().addAll(seats);
            System.out.println("--- 좌석 목록 ---");
            for (Seat seat : seats) {
                System.out.println(seat);
            }
            System.out.println("-----------------");
        }

        VBox root = new VBox(tableView);
        Scene scene = new Scene(root, 400, 500);

        primaryStage.setTitle("좌석 목록");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
