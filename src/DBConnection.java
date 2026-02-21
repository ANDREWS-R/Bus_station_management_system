import java.sql.*;

public class DBConnection {
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bus_management",
                "root",
                "AK#abhi"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
