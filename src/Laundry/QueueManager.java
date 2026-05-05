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
    private DefaultTableModel claimedModel;
    private JTable pendingTable;
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

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel pendingPanel = new JPanel(new BorderLayout(0, 10));
        pendingPanel.setOpaque(false);
        
        pendingModel = createNonEditableModel(); 
        pendingTable = new JTable(pendingModel);
        pendingTable.setRowHeight(25);
        pendingPanel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);
        
        JButton btnClaim = new JButton("MARK SELECTED AS CLAIMED");
        btnClaim.setBackground(Color.WHITE);
        btnClaim.setForeground(new Color(34, 139, 34)); 
        btnClaim.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClaim.addActionListener(e -> updateOrderStatus(pendingTable, pendingModel, "Claimed", "pending"));
        pendingPanel.add(btnClaim, BorderLayout.SOUTH);

        JPanel claimedPanel = new JPanel(new BorderLayout(0, 10));
        claimedPanel.setOpaque(false);
        
        claimedModel = createNonEditableModel(); 
        claimedTable = new JTable(claimedModel);
        claimedTable.setRowHeight(25);
        claimedPanel.add(new JScrollPane(claimedTable), BorderLayout.CENTER);

        JButton btnReturn = new JButton("RETURN SELECTED TO PENDING");
        btnReturn.setBackground(Color.WHITE);
        btnReturn.setForeground(new Color(200, 50, 50)); 
        btnReturn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReturn.addActionListener(e -> updateOrderStatus(claimedTable, claimedModel, "Pending", "claimed"));
        claimedPanel.add(btnReturn, BorderLayout.SOUTH);

        tabbedPane.addTab("Pending Orders", pendingPanel);
        tabbedPane.addTab("Claimed Orders", claimedPanel);
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
        String id = model.getValueAt(row, 0).toString();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE sales SET status = ? WHERE id = ?")) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            
            loadQueueData(searchField.getText().trim());
            
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Database Error!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQueueData(String keyword) {
        pendingModel.setRowCount(0);
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
                
                if (status.equals("Pending")) {
                    pendingModel.addRow(new Object[]{id, inv, name, phone, status});
                } else if (status.equals("Claimed")) {
                    claimedModel.addRow(new Object[]{id, inv, name, phone, status});
                }
            }
        } catch (Exception e) {}
    }
}