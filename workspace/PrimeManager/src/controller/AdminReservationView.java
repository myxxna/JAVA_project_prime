package controller;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminReservationView extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> typeSelector;
    private JButton sortAscButton;
    private JButton sortDescButton;
    private List<Reservation> reservations;

    public AdminReservationView(Frame parent) {
        super(parent, "예약 조회", false);
        setSize(800, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // 상단 컨트롤 영역
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typeSelector = new JComboBox<>(new String[]{"전체", "프라임실", "1인 좌석"});
        sortAscButton = new JButton("오름차순");
        sortDescButton = new JButton("내림차순");
        topPanel.add(new JLabel("구분:"));
        topPanel.add(typeSelector);
        topPanel.add(sortAscButton);
        topPanel.add(sortDescButton);
        add(topPanel, BorderLayout.NORTH);

        // 테이블 구성
        String[] columns = {"이름", "학번", "학과", "번호", "이용 시작 시간", "이용 시간(분)"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // DB에서 데이터 불러오기
        DBManager db = new DBManager();
        this.reservations = db.getReservations();

        // 초기 표시
        loadTableData("전체");

        // 이벤트 설정
        typeSelector.addActionListener(e -> loadTableData((String) typeSelector.getSelectedItem()));
        sortAscButton.addActionListener(e -> sortByNumber(true));
        sortDescButton.addActionListener(e -> sortByNumber(false));

        setVisible(true);
    }

    private void loadTableData(String filterType) {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Reservation r : reservations) {
            if (filterType.equals("전체") || r.getType().equals(filterType)) {
                model.addRow(new Object[]{
                    r.getName(),
                    r.getStudentId(),
                    r.getDepartment(),
                    r.getSeatOrRoomNumber(),
                    r.getStartTime().format(fmt),
                    r.getUsageMinutes()
                });
            }
        }
    }

    private void sortByNumber(boolean ascending) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(Collections.singletonList(
                new RowSorter.SortKey(3, ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING)
        ));
    }
}


public class Main {
	// 테스트 실행용 main
        public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminReservationView(null));
    }
}
