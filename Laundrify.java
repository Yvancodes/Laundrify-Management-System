package Laundry;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.util.Stack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.util.UUID;

public class Laundrify extends JFrame {

    private JPanel contentPane;
    private JTextArea receiptArea;
    private JTextField totalField;
    

    private String loggedInCashier = "Unknown Cashier";
    private double currentTotal = 0.00;
    private Stack<Double> priceHistory = new Stack<>();
    private Stack<String> itemHistory = new Stack<>();
    private String customerName = "";
    private String customerPhone = "";

    // ==========================================
    
    // Structure colors
    private final Color MAIN_SYSTEM_BG = new Color(241, 245, 249); // Soft slate (anti-glare)
    private final Color CARD_HOLDER_BG = Color.WHITE;             // White card popping
    private final Color BORDER_GRAY = new Color(203, 213, 225);   // Structural borders
    
    // Text colors 
    private final Color SLATE_DARK_TEXT = new Color(30, 41, 59);  

    
    private final Color PRIMARY_VIBRANT_BLUE = new Color(0, 153, 255); // Sky Blue Accent
    private final Color CHECKOUT_GREEN = new Color(16, 185, 129);      // Checkout/Total
    private final Color DANGER_BG = new Color(254, 226, 226);          // Pale Red
    private final Color DANGER_TEXT = new Color(153, 27, 27);          // Dark Red

    public Laundrify(String cashierName) {
    	this.loggedInCashier = cashierName; // Save the name!
        setTitle("Laundrify - Cashier POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // --- TRUE FULLSCREEN MODE ---
        setUndecorated(true); 
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setBounds(100, 100, 680, 550); 
        setLocationRelativeTo(null);

        contentPane = new JPanel(new BorderLayout(20, 20));
        contentPane.setBackground(MAIN_SYSTEM_BG);
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Laundrify POS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(PRIMARY_VIBRANT_BLUE); 
        headerPanel.add(titleLabel);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        JPanel mainBody = new JPanel(new GridLayout(1, 2, 25, 0));
        mainBody.setOpaque(false);
        contentPane.add(mainBody, BorderLayout.CENTER);

        // --- LEFT PANEL (RECEIPT & TOTAL) ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);
        mainBody.add(leftPanel);

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Consolas", Font.PLAIN, 16)); 
        receiptArea.setForeground(SLATE_DARK_TEXT);
        receiptArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(new LineBorder(BORDER_GRAY, 2, true));
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
     // --- NEW CONTAINER PANEL FOR THE HEADER ---
        // We use BorderLayout to easily push things Left (WEST) and Right (EAST)
        JPanel receiptHeaderPanel = new JPanel(new BorderLayout());
        receiptHeaderPanel.setBackground(Color.WHITE);
        receiptHeaderPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Adds a little breathing room

        // 1. Cashier Label (Pushed to the Left)
        JLabel lblCashier = new JLabel("Cashier: " + loggedInCashier);
        lblCashier.setFont(new Font("Consolas", Font.BOLD, 15)); // Match the receipt font
        lblCashier.setForeground(SLATE_DARK_TEXT);
        receiptHeaderPanel.add(lblCashier, BorderLayout.WEST);

        // 2. Date and Time Clock (Pushed to the Right)
        JLabel lblClock = new JLabel();
        lblClock.setHorizontalAlignment(SwingConstants.RIGHT);
        lblClock.setFont(new Font("Consolas", Font.BOLD, 15));
        lblClock.setForeground(SLATE_DARK_TEXT);
        receiptHeaderPanel.add(lblClock, BorderLayout.EAST);

        // Put the ENTIRE panel into the scrollPane header
        scrollPane.setColumnHeaderView(receiptHeaderPanel);

        // --- THE RUNNING CLOCK TIMER ---
        javax.swing.Timer timeTimer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Format: Jan 13, 2004 | 02:45:30 PM
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy  |  hh:mm:ss a");
                lblClock.setText(sdf.format(new java.util.Date()));
            }
        });
        timeTimer.start(); // Start the clock!

        JPanel leftBottomPanel = new JPanel(new BorderLayout(0, 10));
        leftBottomPanel.setOpaque(false);
        leftPanel.add(leftBottomPanel, BorderLayout.SOUTH);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(SLATE_DARK_TEXT); 
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(SLATE_DARK_TEXT, 2, true), 
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        
        JLabel totalLabel = new JLabel(" TOTAL DUE: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setForeground(Color.WHITE);
        
        totalField = new JTextField("0.00");
        totalField.setEditable(false);
        totalField.setBorder(null);
        totalField.setBackground(SLATE_DARK_TEXT);
        totalField.setForeground(CHECKOUT_GREEN); 
        totalField.setFont(new Font("Segoe UI", Font.BOLD, 32));
        totalField.setHorizontalAlignment(JTextField.RIGHT);
        
        totalPanel.add(totalLabel, BorderLayout.WEST);
        totalPanel.add(totalField, BorderLayout.CENTER);
        leftBottomPanel.add(totalPanel, BorderLayout.NORTH);

        JPanel clearRemovePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        clearRemovePanel.setOpaque(false);
        
        JButton btnClear = new JButton("CLEAR");
        styleButton(btnClear, DANGER_BG, DANGER_TEXT);
        btnClear.addActionListener(e -> clearReceipt());
        clearRemovePanel.add(btnClear);
        
        JButton btnRemove = new JButton("	REMOVE");
        styleButton(btnRemove, DANGER_BG, DANGER_TEXT);
        btnRemove.addActionListener(e -> removeLastItem());
        clearRemovePanel.add(btnRemove);
        
        leftBottomPanel.add(clearRemovePanel, BorderLayout.SOUTH);

        // --- RIGHT PANEL (SERVICES & ACTIONS) ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        mainBody.add(rightPanel);

        // STANDARD SERVICES HOLDER
        JPanel mainServicesHolder = new JPanel(new BorderLayout(0, 15));
        mainServicesHolder.setBackground(CARD_HOLDER_BG);
        mainServicesHolder.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel mainServicesTitle = new JLabel("Standard Laundry Services");
        mainServicesTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mainServicesTitle.setForeground(SLATE_DARK_TEXT);
        mainServicesHolder.add(mainServicesTitle, BorderLayout.NORTH);

        JPanel servicesGrid = new JPanel(new GridLayout(3, 2, 12, 12));
        servicesGrid.setOpaque(false);
        
        // --- VIBRANT WEB SAFE SERVICES Tile group group ---
        // Parameter grouping hardcoded here for unique vibrant tile colors.
        addServiceButton(servicesGrid, "WASH", "Wash", "washing-machine.png", new Color(71, 85, 105)); // Blue
        addServiceButton(servicesGrid, "DRY", "Dry", "ironing-service.png", new Color(71, 85, 105));  // Orange
        addServiceButton(servicesGrid, "FOLD", "Fold", "laundry-service.png", new Color(0, 153, 153));  // Fold Green
        addServiceButton(servicesGrid, "WASH & DRY", "Wash & Dry", "laundry.png", new Color(0, 153, 153)); // Purple
        addServiceButton(servicesGrid, "SELF-SERVE", "Self-Serve", "laundry(1).png", new Color(71, 85, 105)); // Yellow
        
        JButton btnWeight = new JButton("WEIGHT");
        // Web-safe Teal grouping hardcoded
        styleButton(btnWeight, new Color(71, 85, 105), Color.WHITE); 
        
        try {
            ImageIcon weightIcon = new ImageIcon("/resources/scale.png");
            Image weightImg = weightIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            btnWeight.setIcon(new ImageIcon(weightImg));
            btnWeight.setVerticalTextPosition(SwingConstants.BOTTOM);
            btnWeight.setHorizontalTextPosition(SwingConstants.CENTER);
            btnWeight.setIconTextGap(10);
        } catch (Exception e) {}
        
        btnWeight.addActionListener(e -> addWeightService());
        servicesGrid.add(btnWeight);
        
        mainServicesHolder.add(servicesGrid, BorderLayout.CENTER);
        rightPanel.add(mainServicesHolder, BorderLayout.CENTER); 

        // ---------------------------------------------------------
        // HOLDER CARD 2: QUICK ACTIONS & CHECKOUT
        // Parameters re-schemed with Turn 27 vibrant colorful colors.
        // ---------------------------------------------------------
        JPanel operationsWrapper = new JPanel(new BorderLayout(0, 20)); 
        operationsWrapper.setOpaque(false);

        JPanel quickActionsCard = new JPanel(new BorderLayout(0, 15));
        quickActionsCard.setBackground(CARD_HOLDER_BG);
        quickActionsCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel quickActionsTitle = new JLabel("Operations & Management");
        quickActionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        quickActionsTitle.setForeground(SLATE_DARK_TEXT);
        quickActionsCard.add(quickActionsTitle, BorderLayout.NORTH);

        JPanel bottomOpsPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        bottomOpsPanel.setOpaque(false);
        
        JPanel serviceAndAddonsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        serviceAndAddonsPanel.setOpaque(false);

        JButton btnFullService = new JButton(" FULL SERVICE");
        // Parameter hardcoded to Sky Blue Accent for vibrant card pop
        styleButton(btnFullService, new Color(0, 153, 153), Color.WHITE); 
        btnFullService.setPreferredSize(new Dimension(100, 60)); 
        btnFullService.addActionListener(e -> addService("Full Service"));
        serviceAndAddonsPanel.add(btnFullService);

        JButton btnAddOns = new JButton(" ADD ONS");
        // Parameter hardcoded to Sky Blue Accent for vibrant card pop
        styleButton(btnAddOns, new Color(0, 153, 153), Color.WHITE); 
        btnAddOns.addActionListener(e -> showAddOnsDialog());
        serviceAndAddonsPanel.add(btnAddOns);

        bottomOpsPanel.add(serviceAndAddonsPanel); 

        JPanel extraBtnsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        extraBtnsPanel.setOpaque(false);
        
        JButton btnCustInfo = new JButton(" CUSTOMER INFO");
        // Parameter hardcoded to dark slate for management group contrast
        styleButton(btnCustInfo, new Color(71, 85, 105), Color.WHITE); 
        btnCustInfo.addActionListener(e -> new CustomerDialog(this).setVisible(true));
        extraBtnsPanel.add(btnCustInfo);

        JButton btnQueue = new JButton(" QUEUE MANAGER");
        // Parameter hardcoded to dark slate for management group contrast
        styleButton(btnQueue, new Color(71, 85, 105), Color.WHITE); 
        btnQueue.addActionListener(e -> new QueueManager().setVisible(true));
        extraBtnsPanel.add(btnQueue);

        bottomOpsPanel.add(extraBtnsPanel);
        quickActionsCard.add(bottomOpsPanel, BorderLayout.CENTER);

        JPanel iconPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        iconPanel.setOpaque(false);
        
        JButton btnUser = new JButton("🚪");
        styleButton(btnUser, MAIN_SYSTEM_BG, SLATE_DARK_TEXT);
        btnUser.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        btnUser.addActionListener(e -> logout()); 
        iconPanel.add(btnUser);
        
        JButton btnLock = new JButton("🔒");
        styleButton(btnLock, MAIN_SYSTEM_BG, SLATE_DARK_TEXT);
        btnLock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        btnLock.addActionListener(e -> new Lock(this, "cashier").setVisible(true)); 
        iconPanel.add(btnLock);

        JButton btnPay = new JButton("PAY NOW");
        // Parameter hardcoded to MAXIMUM Green checkout Tile look.
        styleButton(btnPay, CHECKOUT_GREEN, Color.WHITE); 
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        try {
            ImageIcon payIcon = new ImageIcon("/resources/cash.png");
            Image payImg = payIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            btnPay.setIcon(new ImageIcon(payImg));
            btnPay.setVerticalTextPosition(SwingConstants.BOTTOM);
            btnPay.setHorizontalTextPosition(SwingConstants.CENTER);
            btnPay.setIconTextGap(8); 
        } catch (Exception e) {}
        
        btnPay.addActionListener(e -> processPaymentFlow()); 

        JPanel cardBottomArea = new JPanel(new BorderLayout(10, 0));
        cardBottomArea.setOpaque(false);
        cardBottomArea.add(iconPanel, BorderLayout.WEST);
        cardBottomArea.add(btnPay, BorderLayout.CENTER);
        
        quickActionsCard.add(cardBottomArea, BorderLayout.SOUTH);

        operationsWrapper.add(quickActionsCard, BorderLayout.SOUTH);
        rightPanel.add(operationsWrapper, BorderLayout.SOUTH);
    }
    
    // ==========================================
    // FULLSCREEN ADD-ONS OVERLAY
    // ==========================================
    private void showAddOnsDialog() {
        JDialog dialog = new JDialog(this, "Select Laundry Add-Ons", true);
        dialog.setUndecorated(true); 
        dialog.setBounds(this.getBounds()); 
        dialog.setBackground(new Color(15, 23, 42, 170)); 

        JPanel overlayWrapper = new JPanel(new GridBagLayout());
        overlayWrapper.setOpaque(false); 

        JPanel modalCard = new JPanel(new BorderLayout(15, 15));
        modalCard.setBackground(MAIN_SYSTEM_BG); 
        modalCard.setPreferredSize(new Dimension(500, 600)); 
        modalCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblHeader = new JLabel("Choose Add-On Services", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(SLATE_DARK_TEXT);
        lblHeader.setBorder(new EmptyBorder(10, 15, 10, 15));
        modalCard.add(lblHeader, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(CARD_HOLDER_BG); 
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
            itemPanel.setBackground(CARD_HOLDER_BG); 
            itemPanel.setMaximumSize(new Dimension(420, 50));
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_GRAY, 1, true),
                new EmptyBorder(8, 15, 8, 15)
            ));

            JCheckBox chkBox = new JCheckBox(String.format("%s (₱%.2f)", name, price));
            chkBox.setFont(new Font("Segoe UI", Font.BOLD, 15));
            chkBox.setBackground(CARD_HOLDER_BG);
            chkBox.setForeground(SLATE_DARK_TEXT);
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
        scrollPane.setBorder(new LineBorder(BORDER_GRAY, 2, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        modalCard.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnCancel = new JButton("CANCEL");
        styleButton(btnCancel, DANGER_BG, DANGER_TEXT);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnConfirm = new JButton("ADD TO ORDER");
        // Parameter Sky Blue accent for vibrant vibrant card pop
        styleButton(btnConfirm, PRIMARY_VIBRANT_BLUE, Color.WHITE);
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

        overlayWrapper.add(modalCard);
        dialog.setContentPane(overlayWrapper);

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
    // DYNAMIC TOUCHPAD UI
    // Parameters group grouping re-schemed with vibrant vibrant colors.
    // ==========================================
    private Double showTouchpadDialog(String title, boolean isPayment) {
        JDialog dialog = new JDialog(this, title, true); 
        dialog.setSize(400, 550);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setLayout(new BorderLayout(15, 15));
        dialog.getContentPane().setBackground(MAIN_SYSTEM_BG);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(15, 15, 0, 15));
        
        JTextField displayField = new JTextField();
        displayField.setFont(new Font("Segoe UI", Font.BOLD, 36));
        displayField.setForeground(SLATE_DARK_TEXT);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_GRAY, 2), new EmptyBorder(10, 10, 10, 10)));
        topPanel.add(displayField, BorderLayout.CENTER);
        dialog.getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 15, 10, 15));

        if (isPayment) {
            JPanel billsPanel = new JPanel(new GridLayout(2, 3, 8, 8));
            billsPanel.setOpaque(false);
            int[] bills = {20, 50, 100, 200, 500, 1000};
            
            for (int bill : bills) {
                JButton btnBill = new JButton("₱" + bill);
                btnBill.setFont(new Font("Segoe UI", Font.BOLD, 16));
                // Emerald Green checkout grout GROUPING hardcoded
                btnBill.setBackground(CHECKOUT_GREEN); 
                btnBill.setForeground(Color.WHITE);
                btnBill.setFocusPainted(false);
                btnBill.setBorder(new LineBorder(BORDER_GRAY, 1, true));
                btnBill.addActionListener(e -> displayField.setText(String.valueOf(bill)));
                billsPanel.add(btnBill);
            }
            centerPanel.add(billsPanel, BorderLayout.NORTH);
        }

        JPanel numpadPanel = new JPanel(new GridLayout(4, 3, 8, 8));
        numpadPanel.setOpaque(false);
        String[] keys = {"7", "8", "9", "4", "5", "6", "1", "2", "3", "C", "0", "."};
        
        for (String key : keys) {
            JButton btnKey = new JButton(key);
            btnKey.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            if(key.equals("C")) {
                btnKey.setBackground(DANGER_BG);
                btnKey.setForeground(DANGER_TEXT);
            } else {
                // Parameter Sky Blue grouping group GROUPING re-schemed GROUPING hardcoded
                btnKey.setBackground(new Color(224, 242, 254)); 
                btnKey.setForeground(PRIMARY_VIBRANT_BLUE);
            }
            
            btnKey.setFocusPainted(false);
            btnKey.setBorder(new LineBorder(BORDER_GRAY, 1, true));
            
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
        dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        JButton btnConfirm = new JButton("CONFIRM ENTRY");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 20));
        // Parameter Sky Blue grouping GROUPING hardcoded parameter parameter
        btnConfirm.setBackground(PRIMARY_VIBRANT_BLUE);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(0, 50));
        btnConfirm.setBorder(new LineBorder(PRIMARY_VIBRANT_BLUE.darker(), 1, true));
        
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
        
        bottomPanel.add(btnConfirm, BorderLayout.SOUTH);
        dialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true); 
        return finalResult[0]; 
    }

    
    // ==========================================
    private void addServiceButton(JPanel panel, String btnText, String itemText, String iconFileName, Color dynamicTileColor) {
        JButton btn = new JButton(btnText);
        
        try {
            ImageIcon icon = new ImageIcon("/resources/" + iconFileName);
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
            
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setIconTextGap(10); 
            
        } catch (Exception e) {
            System.out.println("Could not load icon placeholder: " + iconFileName);
        }

        styleButton(btn, dynamicTileColor, Color.WHITE); // White text looks best on these bold colors
        btn.addActionListener(e -> addService(itemText));
        panel.add(btn);
    }

    // ==========================================

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true);                
        
  
        button.setContentAreaFilled(true);    
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        Color borderHighlight;
        if (bg.equals(PRIMARY_VIBRANT_BLUE)) borderHighlight = PRIMARY_VIBRANT_BLUE.darker();
        else if (bg.equals(CHECKOUT_GREEN)) borderHighlight = CHECKOUT_GREEN.darker();
        else if (bg.equals(CARD_HOLDER_BG) || bg.equals(MAIN_SYSTEM_BG)) borderHighlight = BORDER_GRAY;
        else borderHighlight = bg.darker();

        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderHighlight, 2, true), 
            BorderFactory.createEmptyBorder(12, 10, 12, 10) 
        ));
    }

    public void applyCustomerToOrder(String name, String phone) {
        this.customerName = name;
        this.customerPhone = phone;
        
        receiptArea.append("==================================\n");
        receiptArea.append("  Customer: " + customerName + "\n");
        receiptArea.append("  Contact: " + customerPhone + "\n");
        receiptArea.append("==================================\n");
    }
    
    private void addWeightService() {
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
            
            receiptArea.append(String.format("  %-16s - ₱%.2f\n", entryName, totalCost)); 
            
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
        
        receiptArea.append(String.format("  %-16s - ₱%.2f\n", name, currentPrice)); 
        
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

     // Added the name of logged in cashier
        FileGeneration.generateReceipt(invoiceNumber, customerName, customerPhone, itemHistory, priceHistory, currentTotal, amountPaid, change, loggedInCashier);        
        try {
            java.io.File pdfFile = new java.io.File("receipt/" + invoiceNumber + "_Receipt.pdf");
            if (pdfFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception ex) {
            System.out.println("Could not open PDF automatically: " + ex.getMessage());
        }
        
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