package org.example.models.services;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public class ReceiptService {

    public static void saveReceipt(DefaultTableModel posModel, BigDecimal amountPaid, BigDecimal change) {
        String receiptId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                INSERT INTO receipts (
                    receipt_id, item_id, qty, unit_price, subtotal,
                    amount_paid, change_due, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < posModel.getRowCount(); i++) {
                    String itemId = posModel.getValueAt(i, 0).toString();
                    int qty = (int) posModel.getValueAt(i, 2);
                    BigDecimal price = (BigDecimal) posModel.getValueAt(i, 3);
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));

                    pstmt.setString(1, receiptId);
                    pstmt.setString(2, itemId);
                    pstmt.setInt(3, qty);
                    pstmt.setBigDecimal(4, price);
                    pstmt.setBigDecimal(5, subtotal);
                    pstmt.setBigDecimal(6, amountPaid);
                    pstmt.setBigDecimal(7, change);
                    pstmt.setTimestamp(8, now);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            System.out.println("Receipt saved successfully: " + receiptId);
        } catch (Exception e) {
            System.out.println("Error saving receipt: " + e.getMessage());
        }
    }
}

