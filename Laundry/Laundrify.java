package Laundry;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.util.Stack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

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
//        setTitle("Laundrify - Cashier POS");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setExtendedState(JFrame.MAXIMIZED_BOTH);
//        setBounds(100, 100, 680, 550);
//        setLocationRelativeTo(null);
    	setTitle("Laundrify - Cashier POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // --- NEW: TRUE FULLSCREEN MODE ---
        setUndecorated(true); // Removes the top window bar (X button)
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizes over the taskbar
        // ---------------------------------
        
        // You can keep or remove setBounds and setLocationRelativeTo, 
        // they will be ignored now that the system forces fullscreen.
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

     // --- UPDATED: Service & Add-ons Wrapper ---
        JPanel bottomOpsPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        bottomOpsPanel.setOpaque(false);
        
        // Create a 2-column grid so Full Service and Add-Ons share the space equally
        JPanel serviceAndAddonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        serviceAndAddonsPanel.setOpaque(false);

        JButton btnFullService = new JButton("🌟 FULL SERVICE");
        styleButton(btnFullService, CRISP_WHITE, NAVY_TEXT);
        btnFullService.setPreferredSize(new Dimension(100, 50));
        btnFullService.addActionListener(e -> addService("Full Service"));
        serviceAndAddonsPanel.add(btnFullService);

        // NEW: Add-Ons Button matching exact styling and hover effects
        JButton btnAddOns = new JButton("➕ ADD ONS");
        styleButton(btnAddOns, CRISP_WHITE, NAVY_TEXT);
        btnAddOns.addActionListener(e -> showAddOnsDialog());
        serviceAndAddonsPanel.add(btnAddOns);

        bottomOpsPanel.add(serviceAndAddonsPanel); // Add the combined row
        // ------------------------------------------

        JPanel extraBtnsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        extraBtnsPanel.setOpaque(false);
        JButton btnCustInfo = new JButton("📝 CUSTOMER INFO");
        styleButton(btnCustInfo, CRISP_WHITE, NAVY_TEXT);
     // DELETED: btnCustInfo.addActionListener(e -> openCustomerForm());
        // NEW:
        btnCustInfo.addActionListener(e -> new CustomerDialog(this).setVisible(true));
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
    
    
    
    
 // ==========================================
    // NEW FEATURE: ADD-ONS POPUP UI
    // ==========================================
 // ==========================================
    // NEW FEATURE: FULLSCREEN ADD-ONS OVERLAY
    // ==========================================
    private void showAddOnsDialog() {
        JDialog dialog = new JDialog(this, "Select Laundry Add-Ons", true);
        
        // --- FULLSCREEN OVERLAY LOGIC ---
        dialog.setUndecorated(true); // Removes standard Windows/Mac borders and X button
        dialog.setBounds(this.getBounds()); // Matches the exact size and position of the main app
        dialog.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black for dimmed effect

        // Container to center the modal card
        JPanel overlayWrapper = new JPanel(new GridBagLayout());
        overlayWrapper.setOpaque(false); // Must be false to let the dimmed background show through

        // The actual Modal Card (White Box)
        JPanel modalCard = new JPanel(new BorderLayout(15, 15));
        modalCard.setBackground(WATER_BLUE_BG);
        modalCard.setPreferredSize(new Dimension(500, 600)); 
        modalCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(SUDSY_BORDER, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // 1. Header
        JLabel lblHeader = new JLabel("Choose Add-On Services", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(NAVY_TEXT);
        lblHeader.setBorder(new EmptyBorder(10, 15, 10, 15));
        modalCard.add(lblHeader, BorderLayout.NORTH);

        // 2. Scrollable Content Panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(CRISP_WHITE);
        listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        Object[][] addonsData = {
            {"Detergent Soap", 15.00},
            {"Fabric Conditioner", 20.00},
            {"Bleach", 10.00},
            {"Stain Remover", 25.00},
            {"Perfume / Freshener", 15.00},
            {"Extra Rinse", 30.00},
            {"Express Service", 50.00},
            {"Fold Service", 40.00}
        };

        java.util.List<AddonUIContext> addonItems = new java.util.ArrayList<>();

        for (Object[] data : addonsData) {
            String name = (String) data[0];
            double price = (Double) data[1];

            JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
            itemPanel.setBackground(CRISP_WHITE);
            itemPanel.setMaximumSize(new Dimension(420, 50));
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SUDSY_BORDER, 1, true),
                new EmptyBorder(8, 15, 8, 15)
            ));

            JCheckBox chkBox = new JCheckBox(String.format("%s (₱%.2f)", name, price));
            chkBox.setFont(new Font("Segoe UI", Font.BOLD, 15));
            chkBox.setBackground(CRISP_WHITE);
            chkBox.setForeground(NAVY_TEXT);
            chkBox.setFocusPainted(false);
            chkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

            SpinnerModel sm = new SpinnerNumberModel(1, 1, 10, 1);
            JSpinner spinner = new JSpinner(sm);
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            spinner.setPreferredSize(new Dimension(70, 35));
            spinner.setEnabled(false); 

            chkBox.addActionListener(e -> spinner.setEnabled(chkBox.isSelected()));

            itemPanel.add(chkBox, BorderLayout.CENTER);
            itemPanel.add(spinner, BorderLayout.EAST);

            listPanel.add(itemPanel);
            listPanel.add(Box.createRigidArea(new Dimension(0, 10))); 

            addonItems.add(new AddonUIContext(name, price, chkBox, spinner));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(new LineBorder(SUDSY_BORDER, 2, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        modalCard.add(scrollPane, BorderLayout.CENTER);

        // 3. Footer (Action Buttons)
        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnCancel = new JButton("CANCEL");
        styleButton(btnCancel, new Color(255, 230, 230), NAVY_TEXT);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnConfirm = new JButton("ADD TO ORDER");
        styleButton(btnConfirm, NAVY_TEXT, NAVY_TEXT);
        btnConfirm.addActionListener(e -> {
            boolean hasSelection = false;
            for (AddonUIContext item : addonItems) {
                if (item.chkBox.isSelected()) {
                    int qty = (Integer) item.spinner.getValue();
                    double totalItemPrice = item.price * qty;
                    String entryName = String.format("%s (x%d)", item.name, qty);

                    itemHistory.push(entryName);
                    priceHistory.push(totalItemPrice);
                    
                    receiptArea.append(String.format("%s - ₱%.2f\n", entryName, totalItemPrice));
                    currentTotal += totalItemPrice;
                    hasSelection = true;
                }
            }
            if (hasSelection) {
                totalField.setText(String.format("%.2f", currentTotal));
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select at least one add-on.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        footerPanel.add(btnCancel);
        footerPanel.add(btnConfirm);
        modalCard.add(footerPanel, BorderLayout.SOUTH);

        // Combine everything
        overlayWrapper.add(modalCard);
        dialog.setContentPane(overlayWrapper);

        // --- SMOOTH FADE-IN ANIMATION ---
        dialog.setOpacity(0.0f);
        Timer fadeInTimer = new Timer(15, e -> {
            float newOpacity = dialog.getOpacity() + 0.08f;
            if (newOpacity >= 1.0f) {
                dialog.setOpacity(1.0f);
                ((Timer)e.getSource()).stop();
            } else {
                dialog.setOpacity(newOpacity);
            }
        });
        fadeInTimer.start();

        dialog.setVisible(true);
    }

    // Inner helper class to group dynamic UI elements securely
    private class AddonUIContext {
        String name;
        double price;
        JCheckBox chkBox;
        JSpinner spinner;

        AddonUIContext(String name, double price, JCheckBox chkBox, JSpinner spinner) {
            this.name = name;
            this.price = price;
            this.chkBox = chkBox;
            this.spinner = spinner;
        }
    }
    
    // ==========================================
    // NEW FEATURE: DYNAMIC TOUCHPAD UI
    // ==========================================
    private Double showTouchpadDialog(String title, boolean isPayment) {
        JDialog dialog = new JDialog(this, title, true); 
        dialog.setSize(400, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.getContentPane().setBackground(WATER_BLUE_BG);
        
        // 1. The Display Screen
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(15, 15, 0, 15));
        
        JTextField displayField = new JTextField();
        displayField.setFont(new Font("Segoe UI", Font.BOLD, 36));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SUDSY_BORDER, 2), new EmptyBorder(10, 10, 10, 10)));
        topPanel.add(displayField, BorderLayout.CENTER);
        dialog.add(topPanel, BorderLayout.NORTH);

        // 2. The Keyboard Area
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 15, 10, 15));

        // --- QUICK BILLS PANEL (Only shows for Payment) ---
        if (isPayment) {
            JPanel billsPanel = new JPanel(new GridLayout(2, 3, 8, 8));
            billsPanel.setOpaque(false);
            int[] bills = {20, 50, 100, 200, 500, 1000};
            
            for (int bill : bills) {
                JButton btnBill = new JButton("₱" + bill);
                btnBill.setFont(new Font("Segoe UI", Font.BOLD, 16));
                btnBill.setBackground(new Color(144, 238, 144)); 
                btnBill.setForeground(NAVY_TEXT);
                btnBill.setFocusPainted(false);
                btnBill.addActionListener(e -> displayField.setText(String.valueOf(bill)));
                billsPanel.add(btnBill);
            }
            centerPanel.add(billsPanel, BorderLayout.NORTH);
        }

        // --- NUMPAD PANEL ---
        JPanel numpadPanel = new JPanel(new GridLayout(4, 3, 8, 8));
        numpadPanel.setOpaque(false);
        String[] keys = {"7", "8", "9", "4", "5", "6", "1", "2", "3", "C", "0", "."};
        
        for (String key : keys) {
            JButton btnKey = new JButton(key);
            btnKey.setFont(new Font("Segoe UI", Font.BOLD, 24));
            btnKey.setBackground(CRISP_WHITE);
            btnKey.setForeground(NAVY_TEXT);
            btnKey.setFocusPainted(false);
            
            btnKey.addActionListener(e -> {
                if (key.equals("C")) {
                    displayField.setText(""); 
                } else {
                    displayField.setText(displayField.getText() + key); 
                }
            });
            numpadPanel.add(btnKey);
        }
        centerPanel.add(numpadPanel, BorderLayout.CENTER);
        dialog.add(centerPanel, BorderLayout.CENTER);

        // 3. The Confirm Button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        JButton btnConfirm = new JButton("CONFIRM");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnConfirm.setBackground(NAVY_TEXT);
        btnConfirm.setForeground(CRISP_WHITE);
        btnConfirm.setPreferredSize(new Dimension(0, 50));
        
        final Double[] finalResult = {null}; 
        
        btnConfirm.addActionListener(e -> {
            try {
                if (!displayField.getText().isEmpty()) {
                    finalResult[0] = Double.parseDouble(displayField.getText());
                    dialog.dispose(); 
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid Number Format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        bottomPanel.add(btnConfirm, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true); 
        return finalResult[0]; 
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


 // Receives data from the CustomerDialog class
    public void applyCustomerToOrder(String name, String phone) {
        this.customerName = name;
        this.customerPhone = phone;
        
        receiptArea.append("========================\n");
        receiptArea.append("Customer: " + customerName + "\n");
        receiptArea.append("Contact: " + customerPhone + "\n");
        receiptArea.append("========================\n");
    }
    
    private void addWeightService() {
        // USING NEW TOUCHPAD: false = No Peso Bills
        Double weight = showTouchpadDialog("Enter Laundry Weight (kg)", false);
        
        if (weight != null) {
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

        // USING NEW TOUCHPAD: true = Show Quick Peso Bills
        Double amountPaid = showTouchpadDialog(String.format("Total Due: ₱%.2f", currentTotal), true);
        
        if (amountPaid != null) {
            if (amountPaid < currentTotal) { 
                JOptionPane.showMessageDialog(this, "Insufficient payment.", "Error", JOptionPane.ERROR_MESSAGE); 
                return; 
            }
            double change = amountPaid - currentTotal;
            JOptionPane.showMessageDialog(this, String.format("Payment successful!\n\nChange Due: ₱%.2f", change), "Complete", JOptionPane.INFORMATION_MESSAGE);
            saveTransactionAndPrint(amountPaid, change);
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

        // 1. Generate the PDF file
        FileGeneration.generateReceipt(invoiceNumber, customerName, customerPhone, itemHistory, priceHistory, currentTotal, amountPaid, change);
        
        // ---------------------------------------------------------
        // NEW FEATURE: AUTO-OPEN RECEIPT POP-UP
        // ---------------------------------------------------------
        try {
            // Target the exact file we just created
            java.io.File pdfFile = new java.io.File("receipt/" + invoiceNumber + "_Receipt.pdf");
            
            // If the computer supports opening files, open it automatically!
            if (pdfFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception ex) {
            // Silently catch errors if the computer doesn't have a PDF viewer installed
            System.out.println("Could not open PDF automatically: " + ex.getMessage());
        }
        // ---------------------------------------------------------
        
        JOptionPane.showMessageDialog(this, 
            "Transaction Complete!\n\nInvoice No: " + invoiceNumber + 
            "\nReceipt has been saved in the 'receipt' folder.", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
        
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