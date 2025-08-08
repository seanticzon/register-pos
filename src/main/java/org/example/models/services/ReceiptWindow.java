package org.example.models.services;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptWindow {
    public static void showReceipt(DefaultTableModel posModel, BigDecimal total, BigDecimal paid, BigDecimal change) {
        JFrame receiptFrame = new JFrame("Receipt");
        receiptFrame.setSize(400, 600);
        receiptFrame.setLocationRelativeTo(null);

        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);

        StringBuilder sb = new StringBuilder();
        sb.append("         My Business Store\n");
        sb.append("     123 Main St, Hometown City\n");
        sb.append("        Tel: (123) 456-7890\n\n");

        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sb.append("Date: ").append(dateTime).append("\n");
        sb.append("Receipt #: ").append(java.util.UUID.randomUUID()).append("\n\n");

        sb.append("Items:\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-12s %-5s %-7s %-7s\n", "Name", "Qty", "Price", "Total"));

        for (int i = 0; i < posModel.getRowCount(); i++) {
            String name = (String) posModel.getValueAt(i, 1);
            int qty = (int) posModel.getValueAt(i, 2);
            BigDecimal price = (BigDecimal) posModel.getValueAt(i, 3);
            BigDecimal totalItem = price.multiply(BigDecimal.valueOf(qty));

            sb.append(String.format("%-12s %-5d %-7.2f %-7.2f\n", name, qty, price, totalItem));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal:%28s\n", "$" + total));
        sb.append(String.format("Paid:%32s\n", "$" + paid));
        sb.append(String.format("Change:%30s\n", "$" + change));
        sb.append("----------------------------------------\n\n");

        sb.append("      THANK YOU FOR YOUR PURCHASE!\n");
        sb.append("----------------------------------------\n");

        receiptArea.setText(sb.toString());

        JButton printBtn = new JButton("Print");
        printBtn.addActionListener(e -> {
            try {
                receiptArea.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(receiptFrame, "Printing Failed: " + ex.getMessage());
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> receiptFrame.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(printBtn);
        btnPanel.add(closeBtn);

        receiptFrame.setLayout(new BorderLayout());
        receiptFrame.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        receiptFrame.add(btnPanel, BorderLayout.SOUTH);

        receiptFrame.setVisible(true);
    }
}
