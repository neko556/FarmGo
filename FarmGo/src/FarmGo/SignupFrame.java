package FarmGo;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SignupFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JTextField addressField;
    private JComboBox<String> userTypeCombo;

    public SignupFrame() {
        setTitle("Sign Up");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create panel with a grid layout for form fields
        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        
        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);
        
        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);
        
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);
        
        panel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        panel.add(phoneField);
        
        panel.add(new JLabel("Address:"));
        addressField = new JTextField();
        panel.add(addressField);
        
        panel.add(new JLabel("User Type:"));
        userTypeCombo = new JComboBox<>(new String[]{"Farmer", "Retailer"});
        panel.add(userTypeCombo);
        
        // Buttons for Sign Up and Back
        JButton signupButton = new JButton("Sign Up");
        panel.add(signupButton);
        JButton backButton = new JButton("Back");
        panel.add(backButton);
        
        add(panel);
        
        // Action when "Sign Up" is clicked
        signupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());
                String phone = phoneField.getText().trim();
                String address = addressField.getText().trim();
                String userType = userTypeCombo.getSelectedItem().toString();
                
                UsersDAO dao = new UsersDAO();
                boolean success = dao.signup(name, email, password, phone, address, userType);
                
                if (success) {
                    JOptionPane.showMessageDialog(SignupFrame.this, "Sign up successful!");
                    // After sign up, go to the Login screen
                    new LoginFrame().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(SignupFrame.this, "Sign up failed. Please try again.");
                }
            }
        });
        
        // Back button takes the user to the Login screen
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                new SignupFrame().setVisible(true);
            }
        });
    }
}
