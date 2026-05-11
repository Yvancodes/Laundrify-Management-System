package Laundry;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
        
        // --- TRUE FULLSCREEN KIOSK MODE ---
        setUndecorated(true); // Removes the top window bar (X button)
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizes over the taskbar

        // --- NEW: FULL WINDOW IMAGE BACKGROUND ---
        // Creating a custom JPanel that automatically scales the background image to fill the window
        JPanel mainWrapper = new JPanel(new BorderLayout()) {
            private Image bgImage;
            {
                try {
                    // Load the image from your resources folder
                    java.net.URL imgURL = getClass().getResource("/resources/lockscreen3.png");
                    if (imgURL != null) {
                        bgImage = new ImageIcon(imgURL).getImage();
                    }
                } catch (Exception ex) {
                    System.out.println("Image not found: " + ex.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    // Draws the image to completely fill the panel dimensions dynamically
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback color if image fails to load
                    g.setColor(themeColor);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        setContentPane(mainWrapper);

        // --- EXISTING LOGIN FORM (Anchored at the Bottom) ---
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setForeground(new Color(255, 0, 0));
        panel.setBackground(SystemColor.activeCaption);
        panel.setBorder(new EmptyBorder(40, 30, 40, 30));

        JLabel lblMessage = new JLabel("System Locked. Enter Password:", SwingConstants.CENTER);
        lblMessage.setBackground(SystemColor.desktop);
        lblMessage.setForeground(SystemColor.desktop);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblMessage, BorderLayout.NORTH);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        passwordField.setHorizontalAlignment(JTextField.CENTER);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(170, 200, 220), 2, true), new EmptyBorder(5, 5, 5, 5)
        ));
        panel.add(passwordField, BorderLayout.CENTER);

        JButton btnUnlock = new JButton("UNLOCK");
        btnUnlock.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUnlock.setBackground(Color.WHITE);
        btnUnlock.setFocusPainted(false);
        btnUnlock.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
        
        // Add the login panel to the SOUTH (bottom) of the fullscreen wrapper
        mainWrapper.add(panel, BorderLayout.SOUTH);

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