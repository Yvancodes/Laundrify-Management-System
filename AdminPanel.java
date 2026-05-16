package Laundry;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminPanel extends JFrame {

    private JPanel contentPane;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextField totalSalesField;
    private JComboBox<String> dateFilterBox; 

    // --- REFINED BRAND COLOR SCHEME ---
    private final Color BG_MAIN = new Color(245, 248, 252);       // Very subtle off-white/gray-blue
    private final Color CARD_WHITE = new Color(255, 255, 255);    // Crisp White Panels
    private final Color PRIMARY_BLUE = new Color(0, 102, 204);    // Strong "Brand Blue" for accents
    private final Color TEXT_DARK = new Color(20, 35, 55);        // Deep Navy for readable standard text
    private final Color BORDER_LIGHT = new Color(210, 225, 240);  // Clean Light Blue Borders
    private final Color ACCENT_HOVER = new Color(225, 240, 255);  // Soft Blue Hover State
    private final Color TEXT_MUTED = new Color(100, 120, 140);    // Professional Muted Navy

    public AdminPanel() {
        setTitle("Laundrify - Admin Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        
        // --- TRUE FULLSCREEN KIOSK MODE ---
        setUndecorated(true); 
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        // ----------------------------------

        contentPane = new JPanel(new BorderLayout(20, 20));
        contentPane.setBackground(BG_MAIN);
        contentPane.setBorder(new EmptyBorder(25, 25, 25, 25));
        setContentPane(contentPane);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_LIGHT, 2, true), BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Laundrify Admin Dashboard", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_BLUE); 
        
        JLabel lblSubtitle = new JLabel("REAL-TIME ORDER & SALES TRACKING", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSubtitle.setForeground(TEXT_MUTED); 
        
        titlePanel.add(lblTitle); 
        titlePanel.add(lblSubtitle);
        
        headerPanel.add(Box.createHorizontalStrut(100), BorderLayout.WEST); 
        headerPanel.add(titlePanel, BorderLayout.CENTER);

        JPanel topIconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topIconsPanel.setOpaque(false);
        
        JButton btnUser = new JButton("🚪");
        styleIconBtn(btnUser);
        btnUser.addActionListener(e -> logout()); 
        topIconsPanel.add(btnUser);

        JButton btnLock = new JButton("🔒");
        styleIconBtn(btnLock);
        btnLock.addActionListener(e -> new Lock(this, "admin").setVisible(true)); 
        topIconsPanel.add(btnLock);

        headerPanel.add(topIconsPanel, BorderLayout.EAST);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // --- CENTER BODY ---
        JPanel centerBody = new JPanel(new BorderLayout(20, 0));
        centerBody.setOpaque(false);
        contentPane.add(centerBody, BorderLayout.CENTER);

        // --- LEFT PANEL (SUMMARY) ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(680, 0)); 
        
        JPanel summaryHeader = new JPanel(new BorderLayout());
        summaryHeader.setOpaque(false);
        
        JLabel lblSummary = new JLabel("SUMMARY REPORT");
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSummary.setForeground(PRIMARY_BLUE); 
        
        String[] filters = {"All Time", "Today", "Last 7 Days", "Last 30 Days"};
        dateFilterBox = new JComboBox<>(filters);
        dateFilterBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateFilterBox.setBackground(CARD_WHITE);
        dateFilterBox.setForeground(TEXT_DARK);
        dateFilterBox.setPreferredSize(new Dimension(150, 28));
        dateFilterBox.addActionListener(e -> loadSalesData()); 
        
        summaryHeader.add(lblSummary, BorderLayout.WEST);
        summaryHeader.add(dateFilterBox, BorderLayout.EAST);
        leftPanel.add(summaryHeader, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Inv.", "Customer", "Items", "Total", "Paid", "Change", "Status", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        salesTable = new JTable(tableModel);
        salesTable.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Bold looks better on dark backgrounds
        salesTable.setRowHeight(30);
        
        // --- FULL BLUE TABLE STYLING ---
        salesTable.setBackground(PRIMARY_BLUE);                  // Brand Blue Rows
        salesTable.setForeground(Color.WHITE);                   // White Text
        salesTable.setSelectionBackground(TEXT_DARK);            // Deep Navy for Highlighted Row
        salesTable.setSelectionForeground(Color.WHITE);          // White Text on Highlight
        salesTable.setGridColor(new Color(255, 255, 255, 80));   // Soft Semi-Transparent White Grid Lines
        
        // --- TABLE HEADER ---
        salesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        salesTable.getTableHeader().setBackground(TEXT_DARK);    // Navy Header for contrast
        salesTable.getTableHeader().setForeground(Color.WHITE);  // White Text
           
        JScrollPane tableScroll = new JScrollPane(salesTable);
        tableScroll.setBorder(new LineBorder(BORDER_LIGHT, 2, true));
        tableScroll.getViewport().setBackground(PRIMARY_BLUE);   // Makes empty space match the blue rows
        leftPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel totalPanel = new JPanel(new BorderLayout(15, 0));
        totalPanel.setBackground(CARD_WHITE);
        totalPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_LIGHT, 2, true), BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        
        JLabel lblTotal = new JLabel("Total Revenue: ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(TEXT_DARK);
        
        totalSalesField = new JTextField("₱0.00");
        totalSalesField.setEditable(false);
        totalSalesField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalSalesField.setBackground(CARD_WHITE);
        totalSalesField.setForeground(PRIMARY_BLUE); 
        totalSalesField.setBorder(null); 
        totalSalesField.setHorizontalAlignment(JTextField.RIGHT);
        
        totalPanel.add(lblTotal, BorderLayout.WEST);
        totalPanel.add(totalSalesField, BorderLayout.CENTER);
        leftPanel.add(totalPanel, BorderLayout.SOUTH);
        centerBody.add(leftPanel, BorderLayout.CENTER);

        // --- RIGHT PANEL (OPERATIONS) ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(280, 0));
        
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        gridPanel.setOpaque(false);

        JButton btnSearch = new JButton("SEARCH");
        styleDynamicButton(btnSearch, CARD_WHITE, PRIMARY_BLUE);
        btnSearch.addActionListener(e -> searchSales());
        gridPanel.add(btnSearch);

        JButton btnPricing = new JButton("PRICING SETUP");
        styleDynamicButton(btnPricing, CARD_WHITE, PRIMARY_BLUE); 
        btnPricing.addActionListener(e -> new PricingSetup(this).setVisible(true)); 
        gridPanel.add(btnPricing);

        JButton btnBackup = new JButton("BACKUP DATA");
        styleDynamicButton(btnBackup, CARD_WHITE, PRIMARY_BLUE);
        btnBackup.addActionListener(e -> backupAllSalesToPDF());
        gridPanel.add(btnBackup);
        
        JButton btnRefresh = new JButton("REFRESH");
        styleDynamicButton(btnRefresh, CARD_WHITE, PRIMARY_BLUE);
        btnRefresh.addActionListener(e -> loadSalesData());
        gridPanel.add(btnRefresh);

        JButton btnPrint = new JButton("PRINT");
        styleDynamicButton(btnPrint, CARD_WHITE, PRIMARY_BLUE);
        btnPrint.addActionListener(e -> printSalesReportByDate());
        gridPanel.add(btnPrint);

        JButton btnClear = new JButton("HIDE");
        styleDynamicButton(btnClear, CARD_WHITE, PRIMARY_BLUE);
        btnClear.addActionListener(e -> { 
            tableModel.setRowCount(0); 
            totalSalesField.setText("₱0.00"); 
        });
        gridPanel.add(btnClear);

        rightPanel.add(gridPanel, BorderLayout.CENTER);
        centerBody.add(rightPanel, BorderLayout.EAST);

        loadSalesData(); 
    }

    private void styleDynamicButton(JButton button, Color defaultBg, Color fg) {
        button.setBackground(defaultBg); 
        button.setForeground(fg);
        button.setOpaque(true); 
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_LIGHT, 2, true), BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e) { button.setBackground(defaultBg); }
        });
    }

    private void styleIconBtn(JButton button) {
        button.setBackground(CARD_WHITE); 
        button.setForeground(PRIMARY_BLUE); 
        button.setOpaque(true); 
        button.setContentAreaFilled(false);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        button.setFocusPainted(false); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(50, 50));
        button.setBorder(new LineBorder(BORDER_LIGHT, 1, true));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e) { button.setBackground(CARD_WHITE); }
        });
    }

    private void loadSalesData() {
        tableModel.setRowCount(0);
        double totalRevenue = 0.00;
        
        String filter = dateFilterBox.getSelectedItem().toString();
        String query = "SELECT * FROM sales ";
        
        if (filter.equals("Today")) {
            query += "WHERE DATE(sale_date) = CURDATE() ";
        } else if (filter.equals("Last 7 Days")) {
            query += "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ";
        } else if (filter.equals("Last 30 Days")) {
            query += "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ";
        }
        
        query += "ORDER BY sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("invoice_number"), 
                    rs.getString("customer_name"), 
                    rs.getString("items").replace("\n", ", "), 
                    String.format("₱%.2f", rs.getDouble("total_amount")), 
                    String.format("₱%.2f", rs.getDouble("amount_paid")), 
                    String.format("₱%.2f", rs.getDouble("change_amount")), 
                    rs.getString("status"), 
                    rs.getString("sale_date")
                });
                totalRevenue += rs.getDouble("total_amount");
            }
            totalSalesField.setText(String.format("₱%.2f", totalRevenue));
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void searchSales() {
        String keyword = JOptionPane.showInputDialog(this, "Enter Invoice Number:", "Search Sales", JOptionPane.QUESTION_MESSAGE);
        if (keyword == null) return; 
        if (keyword.trim().isEmpty()) { 
            loadSalesData(); 
            return; 
        }

        tableModel.setRowCount(0);
        double totalRevenue = 0.00;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM sales WHERE invoice_number LIKE ? ORDER BY sale_date DESC")) {
            
            pstmt.setString(1, "%" + keyword.trim() + "%"); 
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("invoice_number"), 
                    rs.getString("customer_name"), 
                    rs.getString("items").replace("\n", ", "), 
                    String.format("₱%.2f", rs.getDouble("total_amount")), 
                    String.format("₱%.2f", rs.getDouble("amount_paid")), 
                    String.format("₱%.2f", rs.getDouble("change_amount")), 
                    rs.getString("status"), 
                    rs.getString("sale_date")
                });
                totalRevenue += rs.getDouble("total_amount");
            }
            totalSalesField.setText(String.format("₱%.2f", totalRevenue));
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void printSalesReportByDate() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        JTextField startField = new JTextField(today); 
        JTextField endField = new JTextField(today);
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Start Date (YYYY-MM-DD):")); 
        panel.add(startField);
        panel.add(new JLabel("End Date (YYYY-MM-DD):")); 
        panel.add(endField);
        
        if (JOptionPane.showConfirmDialog(this, panel, "Select Date Range", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String start = startField.getText().trim(); 
            String end = endField.getText().trim();
            generatePDFReport("SELECT * FROM sales WHERE DATE(sale_date) BETWEEN ? AND ? ORDER BY sale_date ASC", new String[]{start, end}, "sales/Sales_Report_" + start + "_to_" + end + ".pdf", "SALES REPORT: " + start + " TO " + end);
        }
    }

    private void backupAllSalesToPDF() {
        if (JOptionPane.showConfirmDialog(this, "Print a PDF of EVERY sale ever made?", "Backup Data", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            generatePDFReport("SELECT * FROM sales ORDER BY sale_date ASC", new String[]{}, "backup/Complete_Sales_Backup_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".pdf", "COMPLETE DATABASE BACKUP");
        }
    }

    private void generatePDFReport(String query, String[] params, String fileName, String title) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            
            FileGeneration.generateReportFromRS(rs, fileName, title);
            
            try {
                java.io.File pdfFile = new java.io.File(fileName);
                if (pdfFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(pdfFile);
                }
            } catch (Exception ex) {
                System.out.println("Could not open PDF automatically: " + ex.getMessage());
            }
            
            JOptionPane.showMessageDialog(this, "Report generated successfully!\nSaved as:\n" + fileName, "PDF Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Error creating PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
        }
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