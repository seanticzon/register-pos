package org.example.components;

import org.example.models.services.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class JournalPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public JournalPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table model with columns
        model = new DefaultTableModel(new String[]{"Time", "Item ID", "Item Name", "Qty", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);

        // Top bar with actions
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton refreshBtn = new JButton("Refresh");
        topBar.add(refreshBtn);

        refreshBtn.addActionListener(e -> loadData());

        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Initial load
        loadData();
    }

    public void loadData() {
        // Clear current rows
        model.setRowCount(0);

        String sql = """
                SELECT j.datetime, j.item_id, COALESCE(p.name, '') AS name, j.item_qty, j.action
                FROM journal j
                LEFT JOIN pricebook p ON p.id = j.item_id
                ORDER BY j.datetime DESC, j.id DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("datetime");
                String itemId = rs.getString("item_id");
                String name = rs.getString("name");
                int qty = rs.getInt("item_qty");
                String action = rs.getString("action");

                model.addRow(new Object[]{ts, itemId, name, qty, action});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load journal entries: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
