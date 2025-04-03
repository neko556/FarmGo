package FarmGo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
 private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe"; // Change as needed
 private static final String USERNAME = "system";
 private static final String PASSWORD = "123450";

 public static Connection getConnection() throws SQLException, ClassNotFoundException {
     // Load the Oracle JDBC driver
     Class.forName("oracle.jdbc.driver.OracleDriver");
     return DriverManager.getConnection(URL, USERNAME, PASSWORD);
 }
}
