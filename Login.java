package Laundry;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JCheckBox; 
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
// NEW IMPORTS FOR MOUSE CLICKS
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Cursor; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

	private JFrame frame;
	private JTextField txtUsername;
	private JPasswordField txtPassword;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login window = new Login();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Login() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Laundrify - Login");
		frame.setBounds(100, 100, 1200, 700); 
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null); 
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel sidebarLoginPanel = new JPanel();
		sidebarLoginPanel.setBackground(SystemColor.window);
		sidebarLoginPanel.setPreferredSize(new Dimension(500, 0)); 
		frame.getContentPane().add(sidebarLoginPanel, BorderLayout.WEST);

		GridBagLayout gbl_sidebarLoginPanel = new GridBagLayout();
		gbl_sidebarLoginPanel.columnWidths = new int[] { 0, 350, 0 }; 
		gbl_sidebarLoginPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_sidebarLoginPanel.columnWeights = new double[] { 1.0, 0.0, 1.0 }; 
		gbl_sidebarLoginPanel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 }; 
		sidebarLoginPanel.setLayout(gbl_sidebarLoginPanel);

		JLabel lblLoginTitle = new JLabel("Login");
		lblLoginTitle.setFont(new Font("Segoe UI Black", Font.BOLD, 36)); 
		GridBagConstraints gbc_lblLoginTitle = new GridBagConstraints();
		gbc_lblLoginTitle.insets = new Insets(0, 0, 40, 5);
		gbc_lblLoginTitle.anchor = GridBagConstraints.WEST;
		gbc_lblLoginTitle.gridx = 1;
		gbc_lblLoginTitle.gridy = 1;
		sidebarLoginPanel.add(lblLoginTitle, gbc_lblLoginTitle);

		JLabel lblUserHint = new JLabel("Username");
		lblUserHint.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
		GridBagConstraints gbc_lblUserHint = new GridBagConstraints();
		gbc_lblUserHint.anchor = GridBagConstraints.WEST;
		gbc_lblUserHint.insets = new Insets(0, 0, 8, 5);
		gbc_lblUserHint.gridx = 1;
		gbc_lblUserHint.gridy = 2;
		sidebarLoginPanel.add(lblUserHint, gbc_lblUserHint);

		txtUsername = new JTextField();
		txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtUsername.setPreferredSize(new Dimension(350, 45)); 
		GridBagConstraints gbc_txtUsername = new GridBagConstraints();
		gbc_txtUsername.insets = new Insets(0, 0, 20, 5);
		gbc_txtUsername.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUsername.gridx = 1;
		gbc_txtUsername.gridy = 3;
		sidebarLoginPanel.add(txtUsername, gbc_txtUsername);
		txtUsername.setColumns(10);

		JLabel lblPassHint = new JLabel("Password");
		lblPassHint.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
		GridBagConstraints gbc_lblPassHint = new GridBagConstraints();
		gbc_lblPassHint.anchor = GridBagConstraints.WEST;
		gbc_lblPassHint.insets = new Insets(0, 0, 8, 5);
		gbc_lblPassHint.gridx = 1;
		gbc_lblPassHint.gridy = 4;
		sidebarLoginPanel.add(lblPassHint, gbc_lblPassHint);

		txtPassword = new JPasswordField();
		txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtPassword.setEchoChar('●');
		txtPassword.setPreferredSize(new Dimension(350, 45));
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.insets = new Insets(0, 0, 5, 5); 
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 1;
		gbc_txtPassword.gridy = 5;
		sidebarLoginPanel.add(txtPassword, gbc_txtPassword);

        // --- SHOW PASSWORD CHECKBOX ---
        JCheckBox chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setBackground(SystemColor.window);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        chkShowPassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chkShowPassword.isSelected()) {
                    txtPassword.setEchoChar((char) 0); 
                } else {
                    txtPassword.setEchoChar('●'); 
                }
            }
        });
        
        GridBagConstraints gbc_chkShowPassword = new GridBagConstraints();
        gbc_chkShowPassword.anchor = GridBagConstraints.WEST;
        gbc_chkShowPassword.insets = new Insets(0, 0, 20, 5); 
        gbc_chkShowPassword.gridx = 1;
        gbc_chkShowPassword.gridy = 6;
        sidebarLoginPanel.add(chkShowPassword, gbc_chkShowPassword);

		JButton btnLogin = new JButton("LOG IN");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleLogin(); 
			}
		});
		btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
		btnLogin.setFocusPainted(false);
		btnLogin.setForeground(SystemColor.textHighlightText);
		btnLogin.setBackground(SystemColor.textHighlight);
		btnLogin.setPreferredSize(new Dimension(350, 50)); 
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLogin.insets = new Insets(0, 0, 20, 5);
		gbc_btnLogin.gridx = 1;
		gbc_btnLogin.gridy = 7; 
		sidebarLoginPanel.add(btnLogin, gbc_btnLogin);

		ImagePanel MainLogoPanel = new ImagePanel("/resources/laundrify-logo.png");
		
		MainLogoPanel.setOpaque(false);
		frame.getContentPane().add(MainLogoPanel, BorderLayout.CENTER);
		MainLogoPanel.setLayout(new BorderLayout(0, 0));

		JLabel lblTagline = new JLabel("Smart Laundry Management System");
		lblTagline.setHorizontalAlignment(SwingConstants.CENTER); 
		lblTagline.setBorder(new EmptyBorder(15, 0, 0, 20)); 
		lblTagline.setForeground(Color.WHITE);
		lblTagline.setFont(new Font("Segoe UI Light", Font.ITALIC, 22));
		MainLogoPanel.add(lblTagline, BorderLayout.NORTH);
		
		frame.getRootPane().setDefaultButton(btnLogin);
		
		// ==========================================
		// UPDATED: SWAPPED POSITIONS AND ADDED CLICK LISTENER
		// ==========================================
		
		// 1. "Don't have an account?" (Moved to gridy = 8)
		JLabel lblNewLabel = new JLabel("Don't have an account?");
		lblNewLabel.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 11));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 8;
		sidebarLoginPanel.add(lblNewLabel, gbc_lblNewLabel);

		// 2. "Register here" (Moved to gridy = 9, made clickable)
		JLabel lblNewLabel_1 = new JLabel("Register here");
		lblNewLabel_1.setForeground(SystemColor.textHighlight);
		lblNewLabel_1.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblNewLabel_1.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Makes the mouse a pointing hand
		
		// Adds the click functionality
		lblNewLabel_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 1. Create the RegisterFrame object
				RegisterFrame registerWindow = new RegisterFrame();
				
				// 2. GET the frame from inside the object, THEN make it visible
				registerWindow.getFrame().setVisible(true);
				
				// 3. Close the Login window
				frame.dispose(); 
			}
		});
		
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 9;
		sidebarLoginPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
	}

	private void handleLogin() {
		String username = txtUsername.getText().trim();
		String password = new String(txtPassword.getPassword());

		if (username.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Please enter both credentials.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String sql = "SELECT * FROM users WHERE username = ? AND password = ?"; 
		
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, username); 
			pstmt.setString(2, password);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String role = rs.getString("role");

				if (role != null && role.equalsIgnoreCase("admin")) {
					new AdminPanel().setVisible(true);
					frame.dispose(); 
				} else if (role != null && role.equalsIgnoreCase("cashier")) {
					new Laundrify(username).setVisible(true); // <--- PASS IT HERE
					frame.dispose();
				} else if (role != null && role.equalsIgnoreCase("manager")) {
					new Laundrify(username).setVisible(true); // <--- AND HERE
					frame.dispose();
				} else {
					// FIXED: Removed the extra curly brace that was right above this line!
					JOptionPane.showMessageDialog(frame, "Account Role not recognized.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				
			} else {
				JOptionPane.showMessageDialog(frame, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Database Error! Make sure XAMPP is running.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	//--------------------------
	class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Image img;

		public ImagePanel(String imgPath) {
			try {
				java.net.URL imgURL = getClass().getResource(imgPath);
				if (imgURL != null) {
					this.img = new ImageIcon(imgURL).getImage();
				} else {
					System.out.println("Could not find image at: " + imgPath);
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
			} else {
				g.setColor(new Color(30, 60, 90)); 
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}
	
	public JFrame getFrame() {
	    return this.frame;
	}
}