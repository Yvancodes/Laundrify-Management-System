package Laundry;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueueManager extends JFrame {

    private DefaultTableModel pendingModel;
    private DefaultTableModel unclaimedModel; 
    private DefaultTableModel claimedModel;
    
    private JTable pendingTable;
    private JTable unclaimedTable; 
    private JTable claimedTable; 
    
    private JTextField searchField; 

    // --- REFINED BRAND COLOR SCHEME ---
    private final Color BG_MAIN = new Color(245, 248, 252);       // Very subtle off-white/gray-blue
    private final Color CARD_WHITE = new Color(255, 255, 255);    // Crisp White Panels
    private final Color PRIMARY_BLUE = new Color(0, 102, 204);    // Strong "Brand Blue" for accents
    private final Color TEXT_DARK = new Color(20, 35, 55);        // Deep Navy for readable standard text
    private final Color BORDER_LIGHT = new Color(210, 225, 240);  // Clean Light Blue Borders

    public QueueManager() {
        setTitle("Laundry Queue Management");
        setSize(750, 550); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN); 
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panel);

        // --- SEARCH PANEL ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search (Name/Invoice): ");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(new Color(0, 0, 0));
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_LIGHT, 2, true), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        JButton btnSearch = new JButton("SEARCH");
        btnSearch.setBackground(CARD_WHITE);
        btnSearch.setForeground(PRIMARY_BLUE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_LIGHT, 2, true), BorderFactory.createEmptyBorder(6, 15, 6, 15)));
        btnSearch.addActionListener(e -> loadQueueData(searchField.getText().trim()));
        
        JButton btnClear = new JButton("CLEAR");
        btnClear.setBackground(CARD_WHITE);
        btnClear.setForeground(new Color(165, 42, 42)); // Red accent for clear
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setFocusPainted(false);
        btnClear.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(250, 200, 200), 2, true), BorderFactory.createEmptyBorder(6, 15, 6, 15)));
        btnClear.addActionListener(e -> {
            searchField.setText("");
            loadQueueData(""); 
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);
        panel.add(searchPanel, BorderLayout.NORTH);

        // --- TABBED PANE SETUP ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(CARD_WHITE);
        tabbedPane.setForeground(TEXT_DARK);

        // 1. PENDING TAB (Clothes are washing/drying)
        JPanel pendingPanel = new JPanel(new BorderLayout(0, 10));
        pendingPanel.setOpaque(false);
        pendingPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        pendingModel = createNonEditableModel(); 
        pendingTable = new JTable(pendingModel);
        JScrollPane pendingScroll = new JScrollPane(pendingTable);
        styleBlueTable(pendingTable, pendingScroll);
        pendingPanel.add(pendingScroll, BorderLayout.CENTER);
        
        JButton btnMarkUnclaimed = new JButton("MARK AS UNCLAIMED");
        styleActionButton(btnMarkUnclaimed, new Color(204, 102, 0)); // Orange
        btnMarkUnclaimed.addActionListener(e -> updateOrderStatus(pendingTable, pendingModel, "Unclaimed", "pending"));
        pendingPanel.add(btnMarkUnclaimed, BorderLayout.SOUTH);

        // 2. UNCLAIMED TAB (Clothes are ready for pick-up)
        JPanel unclaimedPanel = new JPanel(new BorderLayout(0, 10));
        unclaimedPanel.setOpaque(false);
        unclaimedPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        unclaimedModel = createNonEditableModel(); 
        unclaimedTable = new JTable(unclaimedModel);
        JScrollPane unclaimedScroll = new JScrollPane(unclaimedTable);
        styleBlueTable(unclaimedTable, unclaimedScroll);
        unclaimedPanel.add(unclaimedScroll, BorderLayout.CENTER);

        JPanel unclaimedBtns = new JPanel(new GridLayout(1, 2, 10, 0));
        unclaimedBtns.setOpaque(false);
        
        JButton btnReturnPending = new JButton("RETURN TO PENDING");
        styleActionButton(btnReturnPending, new Color(200, 50, 50)); // Red
        btnReturnPending.addActionListener(e -> updateOrderStatus(unclaimedTable, unclaimedModel, "Pending", "unclaimed"));
        
        JButton btnMarkClaimed = new JButton("MARK AS CLAIMED");
        styleActionButton(btnMarkClaimed, new Color(34, 139, 34)); // Green
        btnMarkClaimed.addActionListener(e -> updateOrderStatus(unclaimedTable, unclaimedModel, "Claimed", "unclaimed"));
        
        unclaimedBtns.add(btnReturnPending);
        unclaimedBtns.add(btnMarkClaimed);
        unclaimedPanel.add(unclaimedBtns, BorderLayout.SOUTH);

        // 3. CLAIMED TAB (Finished and picked up)
        JPanel claimedPanel = new JPanel(new BorderLayout(0, 10));
        claimedPanel.setOpaque(false);
        claimedPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        claimedModel = createNonEditableModel(); 
        claimedTable = new JTable(claimedModel);
        JScrollPane claimedScroll = new JScrollPane(claimedTable);
        styleBlueTable(claimedTable, claimedScroll);
        claimedPanel.add(claimedScroll, BorderLayout.CENTER);

        JButton btnReturnUnclaimed = new JButton("RETURN TO UNCLAIMED");
        styleActionButton(btnReturnUnclaimed, new Color(200, 50, 50)); // Red
        btnReturnUnclaimed.addActionListener(e -> updateOrderStatus(claimedTable, claimedModel, "Unclaimed", "claimed"));
        claimedPanel.add(btnReturnUnclaimed, BorderLayout.SOUTH);

        // Add tabs to Pane
        tabbedPane.addTab("1. Pending Orders", pendingPanel);
        tabbedPane.addTab("2. Unclaimed (Ready)", unclaimedPanel);
        tabbedPane.addTab("3. Claimed", claimedPanel);
        panel.add(tabbedPane, BorderLayout.CENTER);

        loadQueueData(""); 
    }

    // --- STYLING HELPERS ---
    
    private void styleBlueTable(JTable table, JScrollPane scroll) {
        table.setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setRowHeight(30);
        table.setBackground(PRIMARY_BLUE);
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(TEXT_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(255, 255, 255, 80)); 
        
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(TEXT_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        
        scroll.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        scroll.getViewport().setBackground(PRIMARY_BLUE); 
    }

    private void styleActionButton(JButton btn, Color textColor) {
        btn.setBackground(CARD_WHITE);
        btn.setForeground(new Color(0, 0, 0));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_LIGHT, 2, true), BorderFactory.createEmptyBorder(10, 15, 10, 15)));
    }

    // --- FUNCTIONAL LOGIC (UNCHANGED) ---

    private DefaultTableModel createNonEditableModel() {
        return new DefaultTableModel(new String[]{"ID", "Invoice", "Customer", "Contact", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
    }

    private void updateOrderStatus(JTable table, DefaultTableModel model, String newStatus, String origin) {
        int row = table.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a " + origin + " order first.");
            return;
        }
        
        // Extract data directly from the selected row in the table
        String id = model.getValueAt(row, 0).toString();
        String inv = model.getValueAt(row, 1).toString();
        String name = model.getValueAt(row, 2).toString();
        String phone = model.getValueAt(row, 3).toString();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE sales SET status = ? WHERE id = ?")) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            
            // --- NEW: TRIGGER SMS ON SPECIFIC STATUS CHANGE ---
            // If the clothes are moving from Pending to Unclaimed (Ready), send the text!
            if (newStatus.equals("Unclaimed") && origin.equals("pending")) {
                sendSMSNotification(name, phone, inv);
                
                // Show an alert to the cashier so they know the system did its job
                JOptionPane.showMessageDialog(this, "Order marked as Ready!\nAn SMS notification is being sent to " + phone, "SMS Triggered", JOptionPane.INFORMATION_MESSAGE);
            }
            
            loadQueueData(searchField.getText().trim());
            
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Database Error!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQueueData(String keyword) {
        // Clear all tables before loading
        pendingModel.setRowCount(0);
        unclaimedModel.setRowCount(0);
        claimedModel.setRowCount(0);

        String query = "SELECT id, invoice_number, customer_name, customer_phone, status FROM sales ";
        boolean hasSearch = keyword != null && !keyword.isEmpty();
        
        if (hasSearch) { query += "WHERE customer_name LIKE ? OR invoice_number LIKE ? "; }
        query += "ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            if (hasSearch) {
                String searchPattern = "%" + keyword + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }
             
            ResultSet rs = pstmt.executeQuery();
             
            while (rs.next()) {
                String id = rs.getString("id");
                String inv = rs.getString("invoice_number");
                String name = rs.getString("customer_name");
                String phone = rs.getString("customer_phone");
                String status = rs.getString("status");
                
                // Sort the data into the 3 tabs based on their database status
                if (status.equals("Pending")) {
                    pendingModel.addRow(new Object[]{id, inv, name, phone, status});
                } else if (status.equals("Unclaimed")) {
                    unclaimedModel.addRow(new Object[]{id, inv, name, phone, status});
                } else if (status.equals("Claimed")) {
                    claimedModel.addRow(new Object[]{id, inv, name, phone, status});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ==========================================
    // NEW FEATURE: PHILSMS NOTIFICATION ENGINE
    // ==========================================
    private void sendSMSNotification(String customerName, String phone, String invoice) {
        new Thread(() -> extracted(customerName, phone, invoice)).start();
    }

    private void extracted(String customerName, String phone, String invoice) {
        try {
            // ==========================================
            // NEW: E.164 GLOBAL STANDARD SANITIZER
            // ==========================================
            // 1. Remove ALL spaces, dashes, parentheses, and existing plus signs
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            
            // 2. Convert to strict International E.164 format (+639...)
            if (cleanPhone.startsWith("0")) {
                // If they typed 0991..., change it to +63991...
                cleanPhone = "+63" + cleanPhone.substring(1);
            } else if (cleanPhone.startsWith("63")) {
                // If they typed 63991..., just add the +
                cleanPhone = "+" + cleanPhone;
            } else if (!cleanPhone.startsWith("+")) {
                // Fallback just in case
                cleanPhone = "+" + cleanPhone;
            }
            
            System.out.println("Global Format being sent to API: " + cleanPhone);
            // ==========================================
            // Your exact PhilSMS API Token
            String apiToken = "2916|m2fyvXVwTUUn2wXQ9nix6KlRMMMYfOnfXTSB3aCe0c99bc76"; 
            
            // The updated official PhilSMS sending endpoint
            String apiUrl = "https://dashboard.philsms.com/api/v3/sms/send";
            
            // Use the default sender ID
            String senderId = "PhilSMS"; 
            
            String messageText = "Hello " + customerName + "! Your laundry (Inv: " + invoice + ") is finished and ready for pick-up at Laundrify. Thank you!";
            
            // Build the JSON payload using the CLEANED phone number
            String jsonPayload = "{"
                + "\"recipient\":\"" + cleanPhone + "\","
                + "\"sender_id\":\"" + senderId + "\","
                + "\"type\":\"plain\","
                + "\"message\":\"" + messageText + "\""
                + "}";

            // Open pure Java connection to PhilSMS
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            
            // Add required PhilSMS Headers
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Send the JSON data
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the exact server response
            int responseCode = conn.getResponseCode();
            java.io.InputStream stream;
            
            if (responseCode >= 200 && responseCode < 300) {
                stream = conn.getInputStream();
            } else {
                stream = conn.getErrorStream();
            }
            
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
            StringBuilder response = new StringBuilder();
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                response.append(currentLine);
            }
            
            // Print the raw JSON from PhilSMS
            System.out.println("HTTP Code: " + responseCode);
            System.out.println("PhilSMS Raw Server Response: " + response.toString());

        } catch (Exception ex) {
            System.out.println("Internet or API Error: " + ex.getMessage());
        }
     }
    
//   
// // ==========================================
//   // NEW FEATURE: MOCK SMS SIMULATOR (SAFE FOR DEFENSE)
//   // ==========================================
//   private void sendSMSNotification(String customerName, String phone, String invoice) {
//       String message = "Hello " + customerName + "!\nYour laundry (Inv: " + invoice + ") is finished and ready for pick-up at Laundrify. Thank you!";
//       
//       // Simulates the customer's phone screen receiving the text
//       JOptionPane.showMessageDialog(this, 
//           "📱 MESSAGE DELIVERED TO: " + phone + "\n\n" + message, 
//           "Customer Phone Simulator", 
//           JOptionPane.INFORMATION_MESSAGE);
//   }
    
}