package org.example.components;

import org.example.models.services.Item;
import org.example.models.services.JournalService;
import org.example.models.services.PricebookService;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BasketPanel {
    private final JPanel panel;
    private final DefaultTableModel model;
    private final JTable table;
    private final JournalService journalService;
    private final JLabel totalLabel;

    public BasketPanel(JournalService journalService) {
        this.journalService = journalService;

        model = new DefaultTableModel(new String[]{"ID", "Name", "Qty", "Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        table = new JTable(model);
        styleTable(table);

        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setOpaque(true);
        totalLabel.setBackground(new Color(45, 125, 255)); // Vibrant blue
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setBorder(new EmptyBorder(15, 20, 15, 20));

        panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(240, 242, 245)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Container panel with drop shadow and rounded corners
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        container.setOpaque(true);
        container.setPreferredSize(new Dimension(380, 0));
        container.setBackground(Color.WHITE);
        container.setLayout(new BorderLayout());

        // Rounded shadow effect on container
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                new RoundedShadowBorder(12, new Color(0, 0, 0, 30))
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(totalLabel, BorderLayout.SOUTH);

        panel.add(container, BorderLayout.CENTER);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(45, 125, 255));
        table.setSelectionForeground(Color.WHITE);
        //table.setTableHeader(null);  // Hide header for cleaner modern look

        // Custom cell renderer with subtle hover and alternating rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            Color hoverColor = new Color(220, 235, 255);
            int hoverRow = -1;

            {
                // Add mouse listener to track hovered row
                table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        int row = table.rowAtPoint(e.getPoint());
                        if (row != hoverRow) {
                            hoverRow = row;
                            table.repaint();
                        }
                    }
                });
                table.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hoverRow = -1;
                        table.repaint();
                    }
                });
            }

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);

                if (isSelected) {
                    c.setBackground(new Color(45, 125, 255));
                    c.setForeground(Color.WHITE);
                } else if (row == hoverRow) {
                    c.setBackground(hoverColor);
                    c.setForeground(Color.DARK_GRAY);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 255));
                    c.setForeground(Color.DARK_GRAY);
                }

                // Center qty and price columns
                if (col == 2 || col == 3) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });
    }

    // Custom rounded shadow border class for container
    private static class RoundedShadowBorder extends AbstractBorder {
        private final int radius;
        private final Color shadowColor;

        public RoundedShadowBorder(int radius, Color shadowColor) {
            this.radius = radius;
            this.shadowColor = shadowColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowGap = 6;
            int shadowOffset = 4;

            int rectX = x + shadowGap;
            int rectY = y + shadowGap;
            int rectWidth = width - shadowGap * 2 - shadowOffset;
            int rectHeight = height - shadowGap * 2 - shadowOffset;

            // Draw shadow
            g2d.setColor(shadowColor);
            g2d.fillRoundRect(rectX + shadowOffset, rectY + shadowOffset, rectWidth, rectHeight, radius, radius);

            // Draw main rounded rect
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, radius, radius);

            g2d.dispose();
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JTable getTable() {
        return table;
    }

    public JLabel getTotalLabel() {
        return totalLabel;
    }

    public void scanItem(String id) {
        id = id.trim();
        if (id.isEmpty()) return;

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(id)) {
                int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
                model.setValueAt(qty + 1, i, 2);
                updateTotal();
                journalService.log(id, qty + 1, "Quantity Increased");
                return;
            }
        }

        Item item = PricebookService.getItemById(id);
        if (item != null) {
            model.addRow(new Object[]{item.id, item.name, 1, item.price});
            updateTotal();
        }
    }

    public void removeSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String itemId = model.getValueAt(selectedRow, 0).toString();
            int qty = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());
            journalService.log(itemId, qty, "Voided");
            model.removeRow(selectedRow);
            updateTotal();
        }
    }

    public void clearBasket(boolean isPayment) {
        if (!isPayment && model.getRowCount() > 0) {
            journalService.log("SYS_VOID_ALL", 0, "Transaction Voided");
        }
        model.setRowCount(0);
        updateTotal();

        table.clearSelection();
        table.repaint();
    }

    public void clearBasket() {
        clearBasket(false);
    }

    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < model.getRowCount(); i++) {
            int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
            BigDecimal price = new BigDecimal(model.getValueAt(i, 3).toString());
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public void updateTotal() {
        totalLabel.setText("Total: $" + calculateTotal());
    }

    public void logTransactionPayment(BigDecimal payment, BigDecimal change, BigDecimal totalWithTax) {
        String description = String.format("Payment received: $%.2f, Change: $%.2f, Total: $%.2f",
                payment, change, totalWithTax);
        journalService.log("SYS_PAYMENT", 1, description);
    }
}
