package FarmGo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    
    public LoginFrame() {
        setTitle("Login");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
      
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);
        
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);
        
        panel.add(new JLabel("User Type:"));
        userTypeCombo = new JComboBox<>(new String[]{"Farmer", "Retailer"});
        panel.add(userTypeCombo);
        
        JButton loginButton = new JButton("Login");
        panel.add(loginButton);
        
        JButton signupButton = new JButton("Sign Up");
        panel.add(signupButton);
        
        add(panel);
        
        // Action for Login button
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());
                String userType = userTypeCombo.getSelectedItem().toString();
                
                UsersDAO dao = new UsersDAO();
                if (dao.login(email, password, userType)) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login successful!");
                    if (userType.equals("Farmer")) {
                        new FarmerDashboard(email).setVisible(true);
                    }
                    else if(userType.equals("Retailer")){ new  RetailerDashboard(email).setVisible(true);}
                    
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Invalid credentials. Please try again.");
                }
            }
        });
        
        // Action for Sign Up button
        signupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SignupFrame().setVisible(true);
                dispose();
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
