package FarmGo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDAO {

    // Method to sign up a new user
    public boolean signup(String name, String email, String password, String phone, String address, String userType) {
        String sql = "INSERT INTO Users (name, email, password, phone, address, user_type) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, phone);
            ps.setString(5, address);
            ps.setString(6, userType);
            
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Method for user login
    public boolean login(String email, String password, String userType) {
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ? AND user_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, userType);
            
            ResultSet rs = ps.executeQuery();
            return rs.next(); // Return true if user exists
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
