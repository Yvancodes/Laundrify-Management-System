package Laundry;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.io.FileOutputStream;
import java.io.File; 
import java.util.Stack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class Laundrify extends JFrame {

    private JPanel contentPane;
    private JTextArea receiptArea;
    private JTextField totalField;

    private double currentTotal = 0.00;
    private Stack<Double> priceHistory = new Stack<>();
    private Stack<String> itemHistory = new Stack<>();
    private String customerName = "";
    private String customerPhone = "";

    private final Color WATER_BLUE_BG = new Color(225, 240, 248); 
    private final Color CRISP_WHITE = Color.WHITE;
    private final Color NAVY_TEXT = new Color(30, 60, 90); 
    private final Color SUDSY_BORDER = new Color(170, 200, 220); 

    public Laundrify() {
        setTitle("Laundrify - Cashier POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setBounds(100, 100, 680, 550);
        setLocationRelativeTo(null);

        contentPane = new JPanel(new BorderLayout(20, 20));
        contentPane.setBackground(WATER_BLUE_BG);
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Laundrify");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 100, 150)); 
        headerPanel.add(titleLabel);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        JPanel mainBody = new JPanel(new GridLayout(1, 2, 25, 0));
        mainBody.setOpaque(false);
        contentPane.add(mainBody, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);
        mainBody.add(leftPanel);

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(new LineBorder(SUDSY_BORDER, 2, true));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel leftBottomPanel = new JPanel(new BorderLayout(0, 10));
        leftBottomPanel.setOpaque(false);
        leftPanel.add(leftBottomPanel, BorderLayout.SOUTH);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(CRISP_WHITE);
        totalPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SUDSY_BORDER, 2, true), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel totalLabel = new JLabel(" Total: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(NAVY_TEXT);
        totalField = new JTextField("0.00");
        totalField.setEditable(false);
        totalField.setBorder(null);
        totalField.setBackground(CRISP_WHITE);
        totalField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalField.setHorizontalAlignment(JTextField.RIGHT);
        totalPanel.add(totalLabel, BorderLayout.WEST);
        totalPanel.add(totalField, BorderLayout.CENTER);
        leftBottomPanel.add(totalPanel, BorderLayout.NORTH);

        JPanel clearRemovePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        clearRemovePanel.setOpaque(false);
        JButton btnClear = new JButton("❌ CLEAR");
        styleButton(btnClear, new Color(255, 230, 230), NAVY_TEXT);
        btnClear.addActionListener(e -> clearReceipt());
        clearRemovePanel.add(btnClear);
        JButton btnRemove = new JButton("➖ REMOVE");
        styleButton(btnRemove, new Color(255, 230, 230), NAVY_TEXT);
        btnRemove.addActionListener(e -> removeLastItem());
        clearRemovePanel.add(btnRemove);
        leftBottomPanel.add(clearRemovePanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        mainBody.add(rightPanel);

        JPanel servicesGrid = new JPanel(new GridLayout(3, 2, 12, 12));
        servicesGrid.setOpaque(false);
        addServiceButton(servicesGrid, "🌀 WASH", "Wash");
        addServiceButton(servicesGrid, "♨️ DRY", "Dry");
        addServiceButton(servicesGrid, "👕 FOLD", "Fold");
        addServiceButton(servicesGrid, "🔄 WASH & DRY", "Wash & Dry");
        addServiceButton(servicesGrid, "🧺 SELF-SERVE", "Self-Serve");
        JButton btnWeight = new JButton("⚖️ WEIGHT");
        styleButton(btnWeight, CRISP_WHITE, NAVY_TEXT);
        btnWeight.addActionListener(e -> addWeightService());
        servicesGrid.add(btnWeight);

        JPanel servicesWrapper = new JPanel(new BorderLayout(0, 12));
        servicesWrapper.setOpaque(false);
        servicesWrapper.add(servicesGrid, BorderLayout.CENTER);

        JPanel bottomOpsPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        bottomOpsPanel.setOpaque(false);
        JButton btnFullService = new JButton("🌟 FULL SERVICE");
        styleButton(btnFullService, CRISP_WHITE, NAVY_TEXT);
        btnFullService.setPreferredSize(new Dimension(100, 50));
        btnFullService.addActionListener(e -> addService("Full Service"));
        bottomOpsPanel.add(btnFullService);

        JPanel extraBtnsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        extraBtnsPanel.setOpaque(false);
        JButton btnCustInfo = new JButton("📝 CUSTOMER INFO");
        styleButton(btnCustInfo, CRISP_WHITE, NAVY_TEXT);
        btnCustInfo.addActionListener(e -> openCustomerForm());
        extraBtnsPanel.add(btnCustInfo);

        JButton btnQueue = new JButton("⏳ QUEUE");
        styleButton(btnQueue, CRISP_WHITE, NAVY_TEXT);
        btnQueue.addActionListener(e -> new QueueManager().setVisible(true));
        extraBtnsPanel.add(btnQueue);

        bottomOpsPanel.add(extraBtnsPanel);
        servicesWrapper.add(bottomOpsPanel, BorderLayout.SOUTH);
        rightPanel.add(servicesWrapper, BorderLayout.CENTER);

        JPanel rightBottomPanel = new JPanel(new BorderLayout(10, 0));
        rightBottomPanel.setOpaque(false);
        JPanel iconPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        iconPanel.setOpaque(false);
        
        JButton btnUser = new JButton("🚪");
        styleButton(btnUser, CRISP_WHITE, NAVY_TEXT);
        btnUser.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        btnUser.addActionListener(e -> logout()); 
        iconPanel.add(btnUser);
        
        JButton btnLock = new JButton("🔒");
        styleButton(btnLock, CRISP_WHITE, NAVY_TEXT);
        btnLock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        btnLock.addActionListener(e -> new Lock(this, "cashier").setVisible(true)); 
        iconPanel.add(btnLock);

        JButton btnPay = new JButton("PAY");
        styleButton(btnPay, CRISP_WHITE, NAVY_TEXT); 
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnPay.setPreferredSize(new Dimension(160, 55));
        btnPay.addActionListener(e -> processPaymentFlow()); 

        rightBottomPanel.add(iconPanel, BorderLayout.WEST);
        rightBottomPanel.add(btnPay, BorderLayout.CENTER);
        rightPanel.add(rightBottomPanel, BorderLayout.SOUTH);
    }

    private void addServiceButton(JPanel panel, String btnText, String itemText) {
        JButton btn = new JButton(btnText);
        styleButton(btn, CRISP_WHITE, NAVY_TEXT);
        btn.addActionListener(e -> addService(itemText));
        panel.add(btn);
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true);               
        button.setContentAreaFilled(false);   
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SUDSY_BORDER, 1, true), BorderFactory.createEmptyBorder(12, 10, 12, 10)));
    }

    private void openCustomerForm() {
        JTextField nameField = new JTextField(customerName, 15);
        JTextField phoneField = new JTextField(customerPhone, 15);

        phoneField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume(); 
                }
            }
        });

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Customer Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);

        boolean isValid = false;
        
        while (!isValid) {
            if (JOptionPane.showConfirmDialog(this, panel, "Enter Customer Info", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                String tempPhone = phoneField.getText().trim();
                
                if (!tempPhone.isEmpty() && !tempPhone.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid Input: Phone number can only contain numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    customerName = nameField.getText().trim();
                    customerPhone = tempPhone;
                    
                    receiptArea.append("========================\n");
                    if (!customerName.isEmpty()) receiptArea.append("Customer: " + customerName + "\n");
                    if (!customerPhone.isEmpty()) receiptArea.append("Contact: " + customerPhone + "\n");
                    receiptArea.append("========================\n");
                    
                    isValid = true;
                }
            } else {
                isValid = true;
            }
        }
    }

    private void addWeightService() {
        String input = JOptionPane.showInputDialog(this, "Enter Laundry Weight (in kg):", "Weight Entry", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                double weight = Double.parseDouble(input);
                
                if (weight <= 0) {
                    JOptionPane.showMessageDialog(this, "Weight must be greater than zero.", "Invalid Weight", JOptionPane.WARNING_MESSAGE);
                    return; 
                }
                
                double pricePerKg = 0.00;
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT price FROM prices WHERE service_name = 'Weight (per kg)'")) {
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) pricePerKg = rs.getDouble("price");
                    else { JOptionPane.showMessageDialog(this, "Price for 'Weight' not set."); return; }
                } catch (Exception ex) { return; }

                double totalCost = weight * pricePerKg;
                String entryName = String.format("Weight (%.1f kg)", weight);
                
                itemHistory.push(entryName);   
                priceHistory.push(totalCost);  
                
                receiptArea.append(String.format("%s - ₱%.2f\n", entryName, totalCost)); 
                
                currentTotal += totalCost;
                totalField.setText(String.format("%.2f", currentTotal));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addService(String name) {
        double currentPrice = 0.00;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT price FROM prices WHERE service_name = ?")) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) currentPrice = rs.getDouble("price");
            else { JOptionPane.showMessageDialog(this, "Price not set for: " + name); return; }
        } catch (Exception e) { return; }

        itemHistory.push(name);          
        priceHistory.push(currentPrice); 
        
        receiptArea.append(String.format("%s - ₱%.2f\n", name, currentPrice)); 
        
        currentTotal += currentPrice;
        totalField.setText(String.format("%.2f", currentTotal));
    }

    private void clearReceipt() {
        receiptArea.setText("");
        currentTotal = 0.00;
        priceHistory.clear();
        itemHistory.clear();
        customerName = "";
        customerPhone = "";
        totalField.setText("0.00");
    }

    private void removeLastItem() {
        if (!priceHistory.isEmpty()) {
            currentTotal -= priceHistory.pop();
            itemHistory.pop(); 
            if (currentTotal < 0) currentTotal = 0; 
            totalField.setText(String.format("%.2f", currentTotal));
            String text = receiptArea.getText();
            int lastNewLineIndex = text.lastIndexOf('\n', text.length() - 2);
            receiptArea.setText(lastNewLineIndex == -1 ? "" : text.substring(0, lastNewLineIndex + 1));
        }
    }

    private void processPaymentFlow() {
        if (currentTotal <= 0) { 
            JOptionPane.showMessageDialog(this, "No items selected.", "Wait!", JOptionPane.WARNING_MESSAGE); 
            return; 
        }

        boolean hasWeight = false;
        for (String item : itemHistory) {
            if (item.contains("Weight")) {
                hasWeight = true;
                break;
            }
        }

        if (!hasWeight) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "WAIT! No Laundry Weight was entered for this order.\n\nAre you sure you want to proceed to payment without a weight?", 
                "Missing Weight Check", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm != JOptionPane.YES_OPTION) { return; }
        }

        String input = JOptionPane.showInputDialog(this, String.format("Total Due: ₱%.2f\nEnter Amount Tendered:", currentTotal), "Payment", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amountPaid = Double.parseDouble(input);
                if (amountPaid < currentTotal) { JOptionPane.showMessageDialog(this, "Insufficient payment.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                double change = amountPaid - currentTotal;
                JOptionPane.showMessageDialog(this, String.format("Payment successful!\n\nChange Due: ₱%.2f", change), "Complete", JOptionPane.INFORMATION_MESSAGE);
                saveTransactionAndPrint(amountPaid, change);
            } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void saveTransactionAndPrint(double amountPaid, double change) {
        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String dbItems = String.join(", ", itemHistory); 
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO sales (invoice_number, customer_name, customer_phone, items, total_amount, amount_paid, change_amount) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setString(1, invoiceNumber); 
                pstmt.setString(2, customerName); 
                pstmt.setString(3, customerPhone);
                pstmt.setString(4, dbItems); 
                pstmt.setDouble(5, currentTotal); 
                pstmt.setDouble(6, amountPaid); 
                pstmt.setDouble(7, change);
                pstmt.executeUpdate();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        // Generates the actual PDF file via FileGeneration class[cite: 1]
        FileGeneration.generateReceipt(invoiceNumber, customerName, customerPhone, itemHistory, priceHistory, currentTotal, amountPaid, change);
        
        // UPDATED: Now shows the specific Invoice Number in the popup
        JOptionPane.showMessageDialog(this, 
            "Transaction Complete!\n\nInvoice No: " + invoiceNumber + 
            "\nReceipt has been saved in the 'receipt' folder.", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
        
        // Resets the screen for the next order
        clearReceipt(); 
    }
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); 
            
            Login loginScreen = new Login();
            loginScreen.getFrame().setVisible(true);
        }
    }
}