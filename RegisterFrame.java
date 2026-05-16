package Laundry;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Graphics;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;

public class RegisterFrame {

    private JFrame frame;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<String> comboRole;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                RegisterFrame window = new RegisterFrame();
                window.frame.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public RegisterFrame() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Laundrify - Register");
        
        // Matches the Login screen's default fallback size
        frame.setBounds(100, 100, 1200, 700); 
        
        // THIS IS THE MAGIC LINE FOR FULL SCREEN
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Centers the window

        // --- SIDEBAR REGISTER PANEL (ON THE RIGHT/EAST) ---
        JPanel sidebarRegisterPanel = new JPanel();
        sidebarRegisterPanel.setBackground(SystemColor.window);
        sidebarRegisterPanel.setPreferredSize(new Dimension(500, 0));
        frame.getContentPane().add(sidebarRegisterPanel, BorderLayout.EAST);
        sidebarRegisterPanel.setLayout(null);

        JLabel lblTitle = new JLabel("Register");
        lblTitle.setForeground(SystemColor.desktop);
        lblTitle.setBounds(100, 80, 150, 38);
        lblTitle.setFont(new Font("Segoe UI Black", Font.BOLD, 28));
        sidebarRegisterPanel.add(lblTitle);

        // Username Field
        JLabel lblUser = new JLabel("Username");
        lblUser.setBounds(100, 129, 80, 16);
        lblUser.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        sidebarRegisterPanel.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(100, 156, 315, 43);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sidebarRegisterPanel.add(txtUsername);

        // Password Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setBounds(100, 210, 80, 16);
        lblPass.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        sidebarRegisterPanel.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(100, 237, 315, 43);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sidebarRegisterPanel.add(txtPassword);

        // Confirm Password Field
        JLabel lblConfirm = new JLabel("Confirm Password");
        lblConfirm.setBounds(97, 291, 120, 16);
        lblConfirm.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        sidebarRegisterPanel.add(lblConfirm);

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setBounds(97, 318, 315, 43);
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sidebarRegisterPanel.add(txtConfirmPassword);

        // Role Selection
        JLabel lblRole = new JLabel("Role");
        lblRole.setBounds(100, 372, 80, 16);
        lblRole.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        sidebarRegisterPanel.add(lblRole);

        // FIX 1: Roles now match exactly what Login.java is looking for!
        String[] roles = {"Cashier", "Admin", "Manager"};
        comboRole = new JComboBox<>(roles);
        comboRole.setModel(new DefaultComboBoxModel(new String[] {"Cashier", "Admin"}));
        comboRole.setBounds(95, 399, 315, 43);
        comboRole.setBackground(Color.WHITE);
        sidebarRegisterPanel.add(comboRole);

        JButton btnRegister = new JButton("REGISTER");
        btnRegister.setBounds(97, 480, 315, 53);
        btnRegister.addActionListener(e -> handleRegistration());
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(SystemColor.textHighlight);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        sidebarRegisterPanel.add(btnRegister);

        JLabel lblBackToLogin = new JLabel("Already have an account? Login here");
        lblBackToLogin.setBounds(161, 562, 220, 16);
        lblBackToLogin.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        lblBackToLogin.setForeground(SystemColor.textHighlight);
        lblBackToLogin.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        sidebarRegisterPanel.add(lblBackToLogin);
        
        lblBackToLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Login login = new Login();
                login.getFrame().setVisible(true); 
                frame.dispose(); 
            }
        });
        
        // --- MAIN LOGO PANEL (CENTER) ---
        // FIX 2: Correct absolute pathing for the ImagePanel string
        ImagePanel mainLogoPanel = new ImagePanel("/resources/laundrify-logo.png");
        mainLogoPanel.setOpaque(false);
        frame.getContentPane().add(mainLogoPanel, BorderLayout.CENTER);
        mainLogoPanel.setLayout(new BorderLayout(0, 0));
        
        JLabel lblTag = new JLabel("Smart Laundry Management System");
        lblTag.setHorizontalAlignment(SwingConstants.CENTER);
        lblTag.setForeground(Color.WHITE);
        lblTag.setFont(new Font("Segoe UI Light", Font.ITALIC, 22));
        mainLogoPanel.add(lblTag, BorderLayout.NORTH);
    }

    private void handleRegistration() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());
        String role = (String) comboRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(frame, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); 
            pstmt.setString(3, role);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(frame, "Registration Successful! Returning to Login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Automatically kick them back to the login screen after success
                Login login = new Login();
                login.getFrame().setVisible(true);
                frame.dispose();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // FIX 3: Re-applied the Bulletproof Image Loader inside your custom class
    class ImagePanel extends JPanel {
        private Image img;
        
        public ImagePanel(String imgPath) { 
            try {
                java.net.URL imgURL = getClass().getResource(imgPath);
                if (imgURL != null) {
                    this.img = new ImageIcon(imgURL).getImage();
                } else {
                    System.out.println("ERROR: Could not find image at " + imgPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                // The dark overlay you added (Looks great!)
                g.setColor(new Color(0, 0, 0, 40));
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // Fallback color if image is missing
                g.setColor(new Color(30, 60, 90)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
    
    public JFrame getFrame() {
        return this.frame;
    }
}