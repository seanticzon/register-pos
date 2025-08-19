package org.example.components;

import org.example.models.services.Item;
import org.example.models.services.JournalService;
import org.example.models.services.PricebookService;
import org.example.utils.TaxCalculator;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BasketPanel {
    private final JPanel panel;
    private final DefaultTableModel model;
    private final JTable table;
    private final JournalService journalService;
    private final JPanel subtotalPanel, taxPanel, totalPanel;
    private final JLabel subtotalAmountLabel, taxAmountLabel, totalAmountLabel;
    private final JLabel basketTitleLabel;
    private final JLabel itemCountLabel;
    
    // Add fields to track discount information (kept for compatibility but not displayed)
    private BigDecimal currentDiscountAmount = BigDecimal.ZERO;
    private String currentDiscountName = "None";
    private double currentDiscountPercentage = 0.0;

    public BasketPanel(JournalService journalService) {
        this.journalService = journalService;

        model = new DefaultTableModel(new String[]{"", "Item", "Qty", "Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable(table);

        // Create header with basket title and item count
        basketTitleLabel = new JLabel("🛒 Shopping Cart");
        basketTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        basketTitleLabel.setForeground(new Color(51, 51, 51));
        
        itemCountLabel = new JLabel("0 items");
        itemCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemCountLabel.setForeground(new Color(128, 128, 128));

        // Create modern financial summary panels (removed discount panel)
        subtotalPanel = createFinancialRowPanel("Subtotal", "$0.00", new Color(102, 102, 102));
        subtotalAmountLabel = (JLabel) ((JPanel) subtotalPanel.getComponent(1)).getComponent(0);
        
        taxPanel = createFinancialRowPanel("Tax", "$0.00", new Color(102, 102, 102));
        taxAmountLabel = (JLabel) ((JPanel) taxPanel.getComponent(1)).getComponent(0);
        
        totalPanel = createTotalRowPanel("Total", "$0.00");
        totalAmountLabel = (JLabel) ((JPanel) totalPanel.getComponent(1)).getComponent(0);

        // Initialize the main panel directly instead of reassigning
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    0, getHeight(), new Color(241, 245, 249)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        setupMainPanel();
    }

    private void setupMainPanel() {
        panel.setLayout(new BorderLayout(0, 16));
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Main content card
        JPanel contentCard = createContentCard();
        
        // Financial summary card
        JPanel financialCard = createFinancialCard();

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentCard, BorderLayout.CENTER);
        panel.add(financialCard, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(basketTitleLabel);
        
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        countPanel.setOpaque(false);
        countPanel.add(itemCountLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(countPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createContentCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new ModernCardBorder());
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Custom scrollbar styling
        styleScrollBar(scrollPane);
        
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFinancialCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new ModernCardBorder());
        
        // Add financial rows with separators (removed discount row)
        card.add(Box.createVerticalStrut(16));
        card.add(subtotalPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(createSeparator());
        card.add(Box.createVerticalStrut(8));
        card.add(taxPanel);
        card.add(Box.createVerticalStrut(12));
        card.add(createThickSeparator());
        card.add(Box.createVerticalStrut(12));
        card.add(totalPanel);
        card.add(Box.createVerticalStrut(16));
        
        return card;
    }

    private JPanel createFinancialRowPanel(String label, String amount, Color amountColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        labelComponent.setForeground(new Color(71, 85, 105));
        
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        amountPanel.setOpaque(false);
        JLabel amountLabel = new JLabel(amount);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        amountLabel.setForeground(amountColor);
        amountPanel.add(amountLabel);
        
        panel.add(labelComponent, BorderLayout.WEST);
        panel.add(amountPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createTotalRowPanel(String label, String amount) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelComponent.setForeground(new Color(30, 41, 59));
        
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        amountPanel.setOpaque(false);
        JLabel amountLabel = new JLabel(amount);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amountLabel.setForeground(new Color(59, 130, 246));
        amountPanel.add(amountLabel);
        
        panel.add(labelComponent, BorderLayout.WEST);
        panel.add(amountPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(226, 232, 240));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return separator;
    }

    private JSeparator createThickSeparator() {
        JSeparator separator = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(203, 213, 225));
                g2d.fillRect(0, 0, getWidth(), 2);
            }
        };
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return separator;
    }

    private void styleScrollBar(JScrollPane scrollPane) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUI(new ModernScrollBarUI());
        verticalBar.setPreferredSize(new Dimension(8, 0));
        verticalBar.setUnitIncrement(16);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(48);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 58, 138));
        table.setBackground(Color.WHITE);

        // Style the table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(new Color(71, 85, 105));
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        header.setOpaque(true);

        // Hide the ID column but keep it for data
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Set column widths
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Item
        table.getColumnModel().getColumn(2).setPreferredWidth(60);  // Qty
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Price

        // Custom cell renderer
        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
    }

    private static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        private Color hoverColor = new Color(248, 250, 252);
        private int hoverRow = -1;

        public ModernTableCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            // Install hover listener only once
            if (table.getMouseMotionListeners().length == 0) {
                table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        int newRow = table.rowAtPoint(e.getPoint());
                        if (newRow != hoverRow) {
                            hoverRow = newRow;
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

            if (isSelected) {
                c.setBackground(new Color(239, 246, 255));
                c.setForeground(new Color(30, 58, 138));
            } else if (row == hoverRow) {
                c.setBackground(hoverColor);
                c.setForeground(new Color(51, 65, 85));
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                c.setForeground(new Color(51, 65, 85));
            }

            // Column-specific styling
            if (col == 1) { // Item name
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            } else if (col == 2) { // Quantity
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setText("×" + value); // Add multiplication symbol
            } else if (col == 3) { // Price
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                c.setForeground(new Color(34, 197, 94));
                setText("$" + value);
            }

            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            return c;
        }
    }

    private static class ModernCardBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Drop shadow
            for (int i = 0; i < 4; i++) {
                g2d.setColor(new Color(0, 0, 0, 8 - i * 2));
                g2d.drawRoundRect(x + i, y + i, width - 2 * i - 1, height - 2 * i - 1, 16 - i, 16 - i);
            }

            // Card border
            g2d.setColor(new Color(226, 232, 240));
            g2d.drawRoundRect(x + 4, y + 4, width - 9, height - 9, 12, 12);

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 8, 8, 8);
        }
    }

    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(203, 213, 225);
            this.trackColor = new Color(248, 250, 252);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(thumbColor);
            g2d.fill(new RoundRectangle2D.Float(thumbBounds.x + 1, thumbBounds.y + 1, 
                                              thumbBounds.width - 2, thumbBounds.height - 2, 4, 4));
            g2d.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(trackColor);
            g2d.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
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
        return totalAmountLabel;
    }

    public JLabel getSubtotalLabel() {
        return subtotalAmountLabel;
    }

    // Keep getDiscountLabel() for compatibility but return null since discount panel is removed
    public JLabel getDiscountLabel() {
        return null;
    }

    public JLabel getTaxLabel() {
        return taxAmountLabel;
    }

    public void setDiscountInfo(BigDecimal discountAmount, String discountName, double discountPercentage) {
        this.currentDiscountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.currentDiscountName = discountName != null ? discountName : "None";
        this.currentDiscountPercentage = discountPercentage;
        updateTotal();
    }

    public void setDiscountInfo(BigDecimal discountAmount, String discountName) {
        setDiscountInfo(discountAmount, discountName, 0.0);
    }

    public void scanItem(String id) {
        id = id.trim();
        if (id.isEmpty()) return;

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(id)) {
                int currentQty = Integer.parseInt(model.getValueAt(i, 2).toString());
                int newQty = currentQty + 1;
                model.setValueAt(newQty, i, 2);
                updateTotal();
                JournalService.log(id, newQty, "Quantity Increased");
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
            JournalService.log(itemId, qty, "Item Voided");
            model.removeRow(selectedRow);
            updateTotal();
        }
    }

    public void clearBasket(boolean isPayment) {
        if (!isPayment && model.getRowCount() > 0) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String itemId = model.getValueAt(i, 0).toString();
                int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
                JournalService.log(itemId, qty, "Void Transaction");
            }
        }
        model.setRowCount(0);
        currentDiscountAmount = BigDecimal.ZERO;
        currentDiscountName = "None";
        currentDiscountPercentage = 0.0;
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
        BigDecimal subtotal = calculateTotal();

        // Calculate totals without showing discount in the UI
        BigDecimal discountedSubtotal = subtotal.subtract(currentDiscountAmount).max(BigDecimal.ZERO);
        BigDecimal tax = TaxCalculator.calculateTax(discountedSubtotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = discountedSubtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        // Update item count
        itemCountLabel.setText(model.getRowCount() + " items");

        // Update financial labels - discount is not shown in UI but still calculated
        subtotalAmountLabel.setText("$" + String.format("%.2f", subtotal.doubleValue()));
        taxAmountLabel.setText("$" + String.format("%.2f", tax.doubleValue()));
        totalAmountLabel.setText("$" + String.format("%.2f", finalTotal.doubleValue()));

        animateValueChange(totalAmountLabel);
    }

    private void animateValueChange(JLabel label) {
        // Simple color flash animation
        Timer timer = new Timer(100, null);
        timer.addActionListener(e -> {
            label.setForeground(new Color(59, 130, 246));
            timer.stop();
        });
        label.setForeground(new Color(34, 197, 94));
        timer.start();
    }

    public void logTransactionPayment(BigDecimal payment, BigDecimal change, BigDecimal totalWithTax) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String itemId = model.getValueAt(i, 0).toString();
            int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
            JournalService.log(itemId, qty, "Payment");
        }
    }

    public void clearDiscountsOnServiceFailure() {
        // Clear all discount-related state
        currentDiscountAmount = BigDecimal.ZERO;
        currentDiscountName = "None";
        currentDiscountPercentage = 0.0;
        
        // Update the UI immediately
        updateTotal();
        
        // Log the service failure
        System.out.println("Discount service unavailable - discounts cleared");
    }
}
