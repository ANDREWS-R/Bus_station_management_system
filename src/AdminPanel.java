import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends JFrame {

    JTextField nameField, routeField, arrivalField, departureField;
    JComboBox<String> statusBox;
    JTable table;

    public AdminPanel() {

        setTitle("Admin Panel");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20,20));

        // ===== TOP SECTION (FORM + BUTTONS) =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JPanel formPanel = new JPanel(new GridLayout(5,2,10,10));

        nameField = new JTextField();
        routeField = new JTextField();
        arrivalField = new JTextField("HH:MM:SS");
        departureField = new JTextField("HH:MM:SS");

        String[] statuses = {"ARRIVING", "LEAVING", "AT_STATION"};
        statusBox = new JComboBox<>(statuses);

        formPanel.add(new JLabel("Bus Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Route:"));
        formPanel.add(routeField);
        formPanel.add(new JLabel("Arrival Time:"));
        formPanel.add(arrivalField);
        formPanel.add(new JLabel("Departure Time:"));
        formPanel.add(departureField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusBox);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel();

        JButton addBtn = new JButton("Add / Update Bus");
        JButton addMoreBtn = new JButton("Add More");
        JButton deleteBtn = new JButton("Delete Bus");

        buttonPanel.add(addMoreBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);

        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER TABLE =====
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadTableData();

        // ===== BOTTOM LOGOUT =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = new JButton("Logout");
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        addBtn.addActionListener(e -> {
            addOrUpdateBus();
            loadTableData();
        });

        addMoreBtn.addActionListener(e -> clearFields());

        deleteBtn.addActionListener(e -> {
            deleteBus();
            loadTableData();
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        startAutoUpdater();
        setVisible(true);
    }

    // ===== LOAD DATABASE INTO TABLE =====
    private void loadTableData() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM buses");

            table.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{},
                    new String[]{"ID","Bus Name","Route","Arrival","Departure","Status","Updated At"}
            ));

            javax.swing.table.DefaultTableModel model =
                    (javax.swing.table.DefaultTableModel) table.getModel();

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("bus_name"),
                        rs.getString("route"),
                        rs.getTime("arrival_time"),
                        rs.getTime("departure_time"),
                        rs.getString("status"),
                        rs.getTimestamp("status_updated_at")
                });
            }

            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // ===== DELETE BUS (BY NAME + ROUTE) =====
    private void deleteBus() {
        try {
            String name = nameField.getText().toUpperCase();
            String route = routeField.getText().toUpperCase();

            if(name.isEmpty() || route.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Enter Bus Name and Route to Delete");
                return;
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM buses WHERE bus_name=? AND route=?"
            );

            ps.setString(1, name);
            ps.setString(2, route);

            int rows = ps.executeUpdate();

            if(rows > 0)
                JOptionPane.showMessageDialog(this,"Bus Deleted Successfully");
            else
                JOptionPane.showMessageDialog(this,"Bus Not Found");

            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // ===== CLEAR FIELDS =====
    private void clearFields() {
        nameField.setText("");
        routeField.setText("");
        arrivalField.setText("HH:MM:SS");
        departureField.setText("HH:MM:SS");
        statusBox.setSelectedIndex(0);
    }

    // ===== ADD OR UPDATE BUS =====
    private void addOrUpdateBus() {

        try {
            Connection con = DBConnection.getConnection();

            String name = nameField.getText().toUpperCase();
            String route = routeField.getText().toUpperCase();
            String arrival = arrivalField.getText();
            String departure = departureField.getText();
            String status = statusBox.getSelectedItem().toString();

            if(status.equals("LEAVING")) {

                PreparedStatement check = con.prepareStatement(
                        "SELECT * FROM buses WHERE bus_name=?"
                );
                check.setString(1, name);
                ResultSet rs = check.executeQuery();

                if(rs.next()) {
                    PreparedStatement update = con.prepareStatement(
                            "UPDATE buses SET status=?, status_updated_at=NOW() WHERE bus_name=?"
                    );
                    update.setString(1, "LEAVING");
                    update.setString(2, name);
                    update.executeUpdate();

                    JOptionPane.showMessageDialog(this,"Bus status updated to LEAVING");
                    con.close();
                    return;
                }
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO buses(bus_name,route,arrival_time,departure_time,status,status_updated_at) VALUES(?,?,?,?,?,NOW())"
            );

            ps.setString(1, name);
            ps.setString(2, route);
            ps.setTime(3, Time.valueOf(arrival));
            ps.setTime(4, Time.valueOf(departure));
            ps.setString(5, status);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"Bus Added Successfully");

            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // ===== AUTO STATUS UPDATE & DELETE =====
    private void startAutoUpdater() {

        Timer timer = new Timer(60000, e -> {
            try {
                Connection con = DBConnection.getConnection();

                Statement st1 = con.createStatement();
                st1.executeUpdate(
                        "UPDATE buses SET status='AT_STATION' " +
                                "WHERE status='ARRIVING' AND " +
                                "TIMESTAMPDIFF(MINUTE, status_updated_at, NOW()) >= 5"
                );

                Statement st2 = con.createStatement();
                st2.executeUpdate(
                        "DELETE FROM buses " +
                                "WHERE status='LEAVING' AND " +
                                "TIMESTAMPDIFF(MINUTE, status_updated_at, NOW()) >= 5"
                );

                con.close();

                loadTableData();

            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });

        timer.start();
    }
}
