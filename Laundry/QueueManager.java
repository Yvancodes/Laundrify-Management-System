package Laundry;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueueManager extends JFrame {

    private DefaultTableModel pendingModel;
    private DefaultTableModel unclaimedModel; // NEW: Model for unclaimed tab
    private DefaultTableModel claimedModel;
    
    private JTable pendingTable;
    private JTable unclaimedTable; // NEW: Table for unclaimed tab
    private JTable claimedTable; 
    
    private JTextField searchField; 

    public QueueManager() {
        setTitle("Laundry Queue Management");
        setSize(750, 550); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(225, 240, 248)); 
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panel);

        // --- SEARCH PANEL ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search (Name/Invoice): ");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton btnSearch = new JButton("SEARCH");
        btnSearch.setBackground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.addActionListener(e -> loadQueueData(searchField.getText().trim()));
        
        JButton btnClear = new JButton("CLEAR");
        btnClear.setBackground(new Color(255, 230, 230));
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

        // 1. PENDING TAB (Clothes are washing/drying)
        JPanel pendingPanel = new JPanel(new BorderLayout(0, 10));
        pendingPanel.setOpaque(false);
        
        pendingModel = createNonEditableModel(); 
        pendingTable = new JTable(pendingModel);
        pendingTable.setRowHeight(25);
        pendingPanel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);
        
        JButton btnMarkUnclaimed = new JButton("FINISHED WASHING: MARK AS UNCLAIMED");
        btnMarkUnclaimed.setBackground(Color.WHITE);
        btnMarkUnclaimed.setForeground(new Color(204, 102, 0)); // Orange
        btnMarkUnclaimed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMarkUnclaimed.addActionListener(e -> updateOrderStatus(pendingTable, pendingModel, "Unclaimed", "pending"));
        pendingPanel.add(btnMarkUnclaimed, BorderLayout.SOUTH);

        // 2. UNCLAIMED TAB (Clothes are ready for pick-up)
        JPanel unclaimedPanel = new JPanel(new BorderLayout(0, 10));
        unclaimedPanel.setOpaque(false);
        
        unclaimedModel = createNonEditableModel(); 
        unclaimedTable = new JTable(unclaimedModel);
        unclaimedTable.setRowHeight(25);
        unclaimedPanel.add(new JScrollPane(unclaimedTable), BorderLayout.CENTER);

        JPanel unclaimedBtns = new JPanel(new GridLayout(1, 2, 10, 0));
        unclaimedBtns.setOpaque(false);
        
        JButton btnReturnPending = new JButton("UNDO: RETURN TO PENDING");
        btnReturnPending.setBackground(Color.WHITE);
        btnReturnPending.setForeground(new Color(200, 50, 50)); 
        btnReturnPending.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReturnPending.addActionListener(e -> updateOrderStatus(unclaimedTable, unclaimedModel, "Pending", "unclaimed"));
        
        JButton btnMarkClaimed = new JButton("MARK AS CLAIMED");
        btnMarkClaimed.setBackground(Color.WHITE);
        btnMarkClaimed.setForeground(new Color(34, 139, 34)); // Green
        btnMarkClaimed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMarkClaimed.addActionListener(e -> updateOrderStatus(unclaimedTable, unclaimedModel, "Claimed", "unclaimed"));
        
        unclaimedBtns.add(btnReturnPending);
        unclaimedBtns.add(btnMarkClaimed);
        unclaimedPanel.add(unclaimedBtns, BorderLayout.SOUTH);

        // 3. CLAIMED TAB (Finished and picked up)
        JPanel claimedPanel = new JPanel(new BorderLayout(0, 10));
        claimedPanel.setOpaque(false);
        
        claimedModel = createNonEditableModel(); 
        claimedTable = new JTable(claimedModel);
        claimedTable.setRowHeight(25);
        claimedPanel.add(new JScrollPane(claimedTable), BorderLayout.CENTER);

        JButton btnReturnUnclaimed = new JButton("UNDO: RETURN TO UNCLAIMED");
        btnReturnUnclaimed.setBackground(Color.WHITE);
        btnReturnUnclaimed.setForeground(new Color(200, 50, 50)); 
        btnReturnUnclaimed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReturnUnclaimed.addActionListener(e -> updateOrderStatus(claimedTable, claimedModel, "Unclaimed", "claimed"));
        claimedPanel.add(btnReturnUnclaimed, BorderLayout.SOUTH);

        // Add tabs to Pane
        tabbedPane.addTab("1. Pending Orders", pendingPanel);
        tabbedPane.addTab("2. Unclaimed (Ready)", unclaimedPanel);
        tabbedPane.addTab("3. Claimed", claimedPanel);
        panel.add(tabbedPane, BorderLayout.CENTER);

        loadQueueData(""); 
    }

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
    	    // NEW: THE PHONE NUMBER SANITIZER
    	    // This removes any +, -, spaces, or parentheses 
    	    // so PhilSMS gets a 100% clean number string.
    	    // ==========================================
    		// ==========================================
    	    // NEW: THE BULLETPROOF PHONE SANITIZER
    	    // ==========================================
    	    // 1. Remove ALL spaces, dashes, parentheses, and plus signs
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
//    // NEW FEATURE: MOCK SMS SIMULATOR (SAFE FOR DEFENSE)
//    // ==========================================
//    private void sendSMSNotification(String customerName, String phone, String invoice) {
//        String message = "Hello " + customerName + "!\nYour laundry (Inv: " + invoice + ") is finished and ready for pick-up at Laundrify. Thank you!";
//        
//        // Simulates the customer's phone screen receiving the text
//        JOptionPane.showMessageDialog(this, 
//            "📱 MESSAGE DELIVERED TO: " + phone + "\n\n" + message, 
//            "Customer Phone Simulator", 
//            JOptionPane.INFORMATION_MESSAGE);
//    }
    
}