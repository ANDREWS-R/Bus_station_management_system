import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UserPanel extends JFrame {

    JTable table;   // changed from JTextArea to JTable

    public UserPanel() {

        setTitle("User Panel");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();

        JButton arriveBtn = new JButton("About to Arrive");
        JButton leaveBtn = new JButton("About to Leave");
        JButton atStationBtn = new JButton("At Station");
        JButton logoutBtn = new JButton("Logout");

        topPanel.add(arriveBtn);
        topPanel.add(leaveBtn);
        topPanel.add(atStationBtn);
        topPanel.add(logoutBtn);

        add(topPanel, BorderLayout.NORTH);

        // TABLE SECTION
        table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        arriveBtn.addActionListener(e -> loadData("ARRIVING"));
        leaveBtn.addActionListener(e -> loadData("LEAVING"));
        atStationBtn.addActionListener(e -> loadData("AT_STATION"));

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        setVisible(true);
    }

    private void loadData(String status) {
        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT bus_name, route, arrival_time, departure_time " +
                            "FROM buses WHERE status = ? " +
                            "ORDER BY departure_time ASC"
            );

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            // Create table model
            javax.swing.table.DefaultTableModel model =
                    new javax.swing.table.DefaultTableModel(
                            new String[]{"Bus Name", "Route", "Arrival", "Departure"}, 0
                    );

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("bus_name"),
                        rs.getString("route"),
                        rs.getTime("arrival_time"),
                        rs.getTime("departure_time")
                });
            }

            table.setModel(model);

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}