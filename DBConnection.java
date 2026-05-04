import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/smartvote",
                    "root",
                    "1234"
            );
            return con;
        } catch (Exception e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }
}