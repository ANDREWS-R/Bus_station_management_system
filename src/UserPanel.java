import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UserPanel extends JFrame {

    JTextArea area;

    public UserPanel() {

        setTitle("User Panel");
        setSize(500, 400);
        setLocationRelativeTo(null); // center window
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

        area = new JTextArea();
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        arriveBtn.addActionListener(e -> loadData("ARRIVING"));
        leaveBtn.addActionListener(e -> loadData("LEAVING"));
        atStationBtn.addActionListener(e -> loadData("AT_STATION"));

        logoutBtn.addActionListener(e -> {
            dispose();        // Close UserPanel
            new LoginPage();      // Open Login window (change if needed)
        });

        setVisible(true);
    }

    private void loadData(String status) {
        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM buses WHERE status = ?"
            );

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            area.setText("");

            while (rs.next()) {
                area.append("Bus: " + rs.getString("bus_name") + "\n");
                area.append("Route: " + rs.getString("route") + "\n");
                area.append("Arrival: " + rs.getString("arrival_time") + "\n");
                area.append("Departure: " + rs.getString("departure_time") + "\n");
                area.append("----------------------------\n");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
