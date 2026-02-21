import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {

    JTextField userField;
    JPasswordField passField;

    public LoginPage() {

        setTitle("Bus Management Login");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel userLabel = new JLabel("Username:");
        userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        passField = new JPasswordField();

        JButton loginBtn = new JButton("Login");

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(new JLabel());
        panel.add(loginBtn);

        add(panel);

        loginBtn.addActionListener(e -> authenticateUser());

        setVisible(true);
    }

    private void authenticateUser() {

        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if(username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and Password cannot be empty");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT role FROM users WHERE username=? AND password=?"
            );

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                String role = rs.getString("role");

                if(role.equalsIgnoreCase("admin")) {
                    new AdminPanel();
                }
                else if(role.equalsIgnoreCase("user")) {
                    new UserPanel();
                }

                dispose();
            }
            else {
                JOptionPane.showMessageDialog(this,
                        "Invalid Username or Password");
            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}