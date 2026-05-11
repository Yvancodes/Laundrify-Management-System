package Laundry;

import java.io.FileOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.sql.ResultSet;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class FileGeneration {

    // --- FOR CASHIER: Generates Single Receipt ---
    public static void generateReceipt(String invoiceNumber, String customerName, String customerPhone, 
                                     Stack<String> itemHistory, Stack<Double> priceHistory, 
                                     double currentTotal, double amountPaid, double change) {
        try {
            File receiptDir = new File("receipt");
            if (!receiptDir.exists()) { receiptDir.mkdirs(); }
            
            String fileName = "receipt/" + invoiceNumber + "_Receipt.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            
            document.add(new Paragraph("=========================================="));
            document.add(new Paragraph("             LAUNDRIFY RECEIPT            "));
            document.add(new Paragraph("=========================================="));
            document.add(new Paragraph("Invoice No: " + invoiceNumber));
            document.add(new Paragraph("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            document.add(new Paragraph(" "));
            
            if (!customerName.isEmpty()) document.add(new Paragraph("Customer: " + customerName));
            if (!customerPhone.isEmpty()) document.add(new Paragraph("Contact: " + customerPhone));
            document.add(new Paragraph(" "));
            
            document.add(new Paragraph("----- ITEMS -----"));
            for (int i = 0; i < itemHistory.size(); i++) {
                document.add(new Paragraph(String.format("%s - ₱%.2f", itemHistory.get(i), priceHistory.get(i))));
            }
            
            document.add(new Paragraph("-----------------"));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(String.format("Total:      ₱%.2f", currentTotal)));
            document.add(new Paragraph(String.format("Paid:       ₱%.2f", amountPaid)));
            document.add(new Paragraph(String.format("Change:     ₱%.2f", change)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("=========================================="));
            document.add(new Paragraph("       Thank you for choosing Laundrify!  "));
            document.add(new Paragraph("=========================================="));
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- FOR ADMIN: Generates Reports from Database ResultSets ---
    public static double generateReportFromRS(ResultSet rs, String fileName, String title) throws Exception {
        File file = new File(fileName);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); 
        }

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        document.add(new Paragraph("=========================================================================="));
        document.add(new Paragraph("                             " + title));
        document.add(new Paragraph("=========================================================================="));
        document.add(new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        document.add(new Paragraph(" "));

        double totalRevenue = 0.00;
        while (rs.next()) {
            String cust = rs.getString("customer_name");
            if(cust == null || cust.isEmpty()) cust = "Walk-in";
            
            document.add(new Paragraph(String.format(
                "Date: %s | Inv: %s | Cust: %s | Total: ₱%.2f | Paid: ₱%.2f | Change: ₱%.2f | Status: %s", 
                rs.getString("sale_date"), rs.getString("invoice_number"), cust, 
                rs.getDouble("total_amount"), rs.getDouble("amount_paid"), rs.getDouble("change_amount"),
                rs.getString("status"))));
            
            totalRevenue += rs.getDouble("total_amount");
        }
        
        document.add(new Paragraph(" "));
        document.add(new Paragraph("=========================================================================="));
        document.add(new Paragraph(String.format("GRAND TOTAL REVENUE: ₱%.2f", totalRevenue)));
        document.add(new Paragraph("=========================================================================="));
        document.close();
        
        return totalRevenue;
    }
}