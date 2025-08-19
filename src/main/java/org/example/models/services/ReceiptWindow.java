package org.example.models.services;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptWindow {
    
    // Legacy method for backward compatibility - matches your existing call
    public static void showReceipt(DefaultTableModel posModel, BigDecimal total, BigDecimal paid, BigDecimal change) {
        showReceipt(null, posModel, total, paid, change);
    }
    
    // Legacy method with parent parameter
    public static void showReceipt(JFrame parentFrame, DefaultTableModel posModel, BigDecimal total, BigDecimal paid, BigDecimal change) {
        // Calculate subtotal by subtracting estimated tax from total
        BigDecimal estimatedTaxRate = new BigDecimal("0.1"); // Assuming 10% tax rate
        BigDecimal subtotal = total.divide(BigDecimal.ONE.add(estimatedTaxRate), 2, BigDecimal.ROUND_HALF_UP);
        showReceipt(parentFrame, posModel, subtotal, total, paid, change, null, BigDecimal.ZERO);
    }
    
    // New method with subtotal parameter
    public static void showReceipt(DefaultTableModel posModel, BigDecimal subtotal, BigDecimal total, 
                                 BigDecimal paid, BigDecimal change) {
        showReceipt(null, posModel, subtotal, total, paid, change);
    }
    
    // New method with subtotal parameter and parent
    public static void showReceipt(JFrame parentFrame, DefaultTableModel posModel, BigDecimal subtotal, BigDecimal total, 
                                 BigDecimal paid, BigDecimal change) {
        showReceipt(parentFrame, posModel, subtotal, total, paid, change, null, BigDecimal.ZERO);
    }
    
    // Full method with discount support (backward compatibility)
    public static void showReceipt(DefaultTableModel posModel, BigDecimal subtotal, BigDecimal total, 
                                 BigDecimal paid, BigDecimal change, String discountName, 
                                 BigDecimal discountAmount) {
        showReceipt(null, posModel, subtotal, total, paid, change, discountName, discountAmount);
    }
    
    // Full method with discount support and parent parameter
    public static void showReceipt(JFrame parentFrame, DefaultTableModel posModel, BigDecimal subtotal, BigDecimal total, 
                                 BigDecimal paid, BigDecimal change, String discountName, 
                                 BigDecimal discountAmount) {
        
        JFrame receiptFrame = new JFrame("Receipt - My Business Store");
        receiptFrame.setSize(450, 700);
        receiptFrame.setLocationRelativeTo(parentFrame); // Use parentFrame instead of null
        receiptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // Receipt text area with improved styling
        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setForeground(Color.BLACK);
        receiptArea.setMargin(new Insets(10, 10, 10, 10));

        StringBuilder sb = new StringBuilder();
        
        // Header with better formatting
        sb.append("================================================\n");
        sb.append("              MY BUSINESS STORE\n");
        sb.append("          123 Main St, Hometown City\n");
        sb.append("            Tel: (123) 456-7890\n");
        sb.append("         Email: info@mybusiness.com\n");
        sb.append("================================================\n\n");

        // Date and receipt info
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
        String receiptId = String.valueOf(System.currentTimeMillis() % 100000); // Shorter ID
        
        sb.append(String.format("Date: %s\n", dateTime));
        sb.append(String.format("Receipt #: %s\n", receiptId));
        sb.append(String.format("Cashier: Store Manager\n\n"));

        // Items section with better alignment
        sb.append("ITEMS PURCHASED:\n");
        sb.append("================================================\n");
        sb.append(String.format("%-20s %3s %8s %10s\n", "Item", "Qty", "Price", "Total"));
        sb.append("------------------------------------------------\n");

        for (int i = 0; i < posModel.getRowCount(); i++) {
            String name = (String) posModel.getValueAt(i, 1);
            int qty = (int) posModel.getValueAt(i, 2);
            BigDecimal price = (BigDecimal) posModel.getValueAt(i, 3);
            BigDecimal totalItem = price.multiply(BigDecimal.valueOf(qty));

            // Truncate long item names
            if (name.length() > 18) {
                name = name.substring(0, 15) + "...";
            }

            sb.append(String.format("%-20s %3d %8.2f %10.2f\n", 
                name, qty, price.doubleValue(), totalItem.doubleValue()));
        }

        sb.append("================================================\n");
        
        // Financial summary with discount support
        sb.append(String.format("%-30s %12.2f\n", "Subtotal:", subtotal.doubleValue()));
        
        // Add discount information if applicable
        if (discountName != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s -%11.2f\n", discountName + ":", discountAmount.doubleValue()));
        }
        
        // Calculate and show tax
        BigDecimal tax = total.subtract(subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO));
        if (tax.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s %12.2f\n", "Tax:", tax.doubleValue()));
        }
        
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-30s %12.2f\n", "TOTAL:", total.doubleValue()));
        sb.append(String.format("%-30s %12.2f\n", "PAID:", paid.doubleValue()));
        sb.append(String.format("%-30s %12.2f\n", "CHANGE:", change.doubleValue()));
        sb.append("================================================\n\n");

        // Footer
        sb.append("           THANK YOU FOR YOUR PURCHASE!\n");
        sb.append("            Have a wonderful day!\n\n");
        sb.append("      Return Policy: 30 days with receipt\n");
        sb.append("         Customer Service: (123) 456-7891\n");
        sb.append("================================================\n");

        receiptArea.setText(sb.toString());

        // Create scroll pane with custom styling
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Enhanced button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        // Styled Print button
        JButton printBtn = new JButton("🖨 Print Receipt");
        printBtn.setPreferredSize(new Dimension(140, 35));
        printBtn.setBackground(new Color(76, 175, 80));
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        printBtn.setFont(new Font("Arial", Font.BOLD, 12));
        printBtn.addActionListener(e -> {
            try {
                receiptArea.print();
                JOptionPane.showMessageDialog(receiptFrame, 
                    "Receipt sent to printer successfully!", 
                    "Print Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(receiptFrame, 
                    "Printing Failed: " + ex.getMessage(), 
                    "Print Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Styled Save button
        JButton saveBtn = new JButton("💾 Save PDF");
        saveBtn.setPreferredSize(new Dimension(140, 35));
        saveBtn.setBackground(new Color(33, 150, 243));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        saveBtn.setFont(new Font("Arial", Font.BOLD, 12));
        saveBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("receipt_" + receiptId + ".txt"));
            if (fileChooser.showSaveDialog(receiptFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.nio.file.Files.write(fileChooser.getSelectedFile().toPath(), 
                        receiptArea.getText().getBytes());
                    JOptionPane.showMessageDialog(receiptFrame, 
                        "Receipt saved successfully!", 
                        "Save Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(receiptFrame, 
                        "Save Failed: " + ex.getMessage(), 
                        "Save Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Styled Close button
        JButton closeBtn = new JButton("❌ Close");
        closeBtn.setPreferredSize(new Dimension(140, 35));
        closeBtn.setBackground(new Color(158, 158, 158));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        closeBtn.setFont(new Font("Arial", Font.BOLD, 12));
        closeBtn.addActionListener(e -> receiptFrame.dispose());

        // Add hover effects to buttons
        addHoverEffect(printBtn, new Color(76, 175, 80), new Color(56, 142, 60));
        addHoverEffect(saveBtn, new Color(33, 150, 243), new Color(25, 118, 210));
        addHoverEffect(closeBtn, new Color(158, 158, 158), new Color(117, 117, 117));

        buttonPanel.add(printBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(closeBtn);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        receiptFrame.add(mainPanel);
        receiptFrame.setVisible(true);
    }

    private static void addHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normalColor);
                button.setCursor(Cursor.getDefaultCursor());
            }
        });
    }
}
