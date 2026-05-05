package Laundry;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Lock extends JFrame {

    private JFrame parentFrame;
    private String accountRole; 
    private Color themeColor;
    
    private int failedAttempts = 0;
    private final int MAX_ATTEMPTS = 3;
    private Timer inactivityTimer;

    public Lock(JFrame parent, String role) {
        this.parentFrame = parent;
        this.accountRole = role; 
        
        if (parentFrame != null) {
            parentFrame.setVisible(false);
        }

        if (role.equals("admin")) {
            setTitle("Admin Locked");
            themeColor = new Color(238, 232, 220); 
        } else {
            setTitle("Cashier Locked");
            themeColor = new Color(225, 240, 248); 
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(themeColor);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        setContentPane(panel);

        JLabel lblMessage = new JLabel("System Locked. Enter " + role.toUpperCase() + " Password:", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblMessage, BorderLayout.NORTH);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        passwordField.setHorizontalAlignment(JTextField.CENTER);
        panel.add(passwordField, BorderLayout.CENTER);

        JButton btnUnlock = new JButton("UNLOCK");
        btnUnlock.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUnlock.setBackground(Color.WHITE);
        btnUnlock.setFocusPainted(false);
        
        btnUnlock.addActionListener(e -> {
            String pass = new String(passwordField.getPassword());
            boolean isAuthorized = false;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
                
                pstmt.setString(1, accountRole); 
                pstmt.setString(2, pass);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    isAuthorized = true; 
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error!", "System Error", JOptionPane.ERROR_MESSAGE);
                return; 
            }

            if (isAuthorized) {
                inactivityTimer.stop(); 
                this.dispose(); 
                if (parentFrame != null) {
                    parentFrame.setVisible(true); 
                }
            } else {
                failedAttempts++;
                int attemptsLeft = MAX_ATTEMPTS - failedAttempts;
                
                if (failedAttempts >= MAX_ATTEMPTS) {
                    JOptionPane.showMessageDialog(this, "Maximum attempts reached. Shutting down system for security.", "Security Breach Detected", JOptionPane.ERROR_MESSAGE);
                    System.exit(0); 
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect Password!\nAttempts remaining: " + attemptsLeft, "Security Alert", JOptionPane.WARNING_MESSAGE);
                    passwordField.setText("");
                }
            }
        });
        
        getRootPane().setDefaultButton(btnUnlock);
        panel.add(btnUnlock, BorderLayout.SOUTH);

        inactivityTimer = new Timer(300000, e -> {
            JOptionPane.showMessageDialog(this, "System left unattended for too long. Shutting down for security.", "Timeout", JOptionPane.WARNING_MESSAGE);
            System.exit(0); 
        });
        inactivityTimer.setRepeats(false); 
        inactivityTimer.start();

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                inactivityTimer.restart();
            }
        });
    }
}