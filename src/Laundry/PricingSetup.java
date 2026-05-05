package Laundry;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.sql.Connection;
import java.sql.PreparedStatement; // ADDED: Missing import for saving
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class PricingSetup extends JDialog {

    private final Color BG_BEIGE = new Color(238, 232, 220);      
    private final Color CARD_WHITE = new Color(252, 250, 245);    
    private final Color TEXT_DARK = new Color(70, 60, 50);        
    private final Color BORDER_BEIGE = new Color(215, 205, 190);  

    private Map<String, JTextField> priceFields;

    public PricingSetup(JFrame parent) {
        super(parent, "Service Pricing Management", true); 
        setSize(450, 550);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        JPanel contentPane = new JPanel(new BorderLayout(0, 20));
        contentPane.setBackground(BG_BEIGE);
        contentPane.setBorder(new EmptyBorder(25, 30, 25, 30));
        setContentPane(contentPane);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Pricing Setup", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT_DARK);
        
        JLabel lblSub = new JLabel("Update the current service rates below", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(130, 115, 100));
        
        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // --- BODY (FORM) ---
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(CARD_WHITE);
        
        formWrapper.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_BEIGE, 2, true), 
            new EmptyBorder(20, 40, 20, 40) 
        ));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 15, 20));
        formPanel.setOpaque(false);
        priceFields = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT service_name, price FROM prices")) {
            
            while (rs.next()) {
                String name = rs.getString("service_name");
                
                // UPDATED: Added Peso sign to the label
                JLabel nameLabel = new JLabel(name + " (₱):"); 
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
                nameLabel.setForeground(TEXT_DARK);
                
                JTextField priceInput = new JTextField(String.format("%.2f", rs.getDouble("price"))); 
                priceInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                priceInput.setHorizontalAlignment(JTextField.RIGHT);
                
                priceInput.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_BEIGE, 1, true),
                    new EmptyBorder(4, 8, 4, 8)
                ));
                
                formPanel.add(nameLabel); 
                formPanel.add(priceInput);
                priceFields.put(name, priceInput);
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Failed to load prices from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        formWrapper.add(formPanel, BorderLayout.CENTER);
        contentPane.add(formWrapper, BorderLayout.CENTER);

        // --- FOOTER (BUTTONS) ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        JButton btnCancel = new JButton("CANCEL");
        styleButton(btnCancel, TEXT_DARK, CARD_WHITE, false); 
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);

        JButton btnSave = new JButton("SAVE CHANGES");
        styleButton(btnSave, TEXT_DARK, CARD_WHITE, false); 
        btnSave.addActionListener(e -> savePrices());
        buttonPanel.add(btnSave);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, Color bg, Color fg, boolean isTransparent) {
        button.setBackground(bg); 
        button.setForeground(fg);
        button.setOpaque(true); 
        
        if (isTransparent) {
            button.setContentAreaFilled(false);
        }
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new LineBorder(isTransparent ? fg : bg, 2, true));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                if (!isTransparent) {
                    button.setBackground(new Color(100, 90, 80)); 
                } else {
                    button.setBackground(fg); 
                    button.setForeground(bg);
                }
            }
            public void mouseExited(MouseEvent e) { 
                button.setBackground(bg); 
                button.setForeground(fg); 
            }
        });
    }

    private void savePrices() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE prices SET price = ? WHERE service_name = ?")) {
            
            for (Map.Entry<String, JTextField> entry : priceFields.entrySet()) {
                double newPrice = Double.parseDouble(entry.getValue().getText().trim());
                pstmt.setDouble(1, newPrice);
                pstmt.setString(2, entry.getKey());
                pstmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Prices successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); 
            
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers only.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Database Error while saving.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}