package org.example.components;

import org.example.models.services.ReceiptService;
import org.example.models.services.ReceiptWindow;
import org.example.models.services.JournalService;
//import org.example.models.services.DiscountServiceClient;
import org.example.models.services.BasketPricingServiceClient;
import org.example.utils.TaxCalculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BottomBarPanel {
    private final JPanel panel;
    private final BasketPanel basketPanel;
    private final JFrame parentFrame; // Changed to JFrame specifically

    private JLabel statusLabel;

    public BottomBarPanel(BasketPanel basketPanel, JFrame parentFrame) {
        this.basketPanel = basketPanel;
        this.parentFrame = parentFrame; // Store the JFrame directly

        // Create buttons with enhanced styling and colors
        JButton payBtn = createButton("PAY", new Color(34, 139, 34), Color.WHITE, true);
        JButton exactBtn = createButton("EXACT $", new Color(70, 130, 180), Color.WHITE, false);
        JButton nextDollarBtn = createButton("NEXT $", new Color(70, 130, 180), Color.WHITE, false);
        JButton qtyChangeBtn = createButton("QTY CHANGE", new Color(255, 165, 0), Color.WHITE, false);
        JButton voidBtn = createButton("VOID", new Color(220, 20, 60), Color.WHITE, false);
        JButton clearBtn = createButton("VOID ALL", new Color(178, 34, 34), Color.WHITE, false);

        // Add action listeners
        payBtn.addActionListener(e -> processPayment(null));
        exactBtn.addActionListener(e -> processPayment(getExactTotal()));
        nextDollarBtn.addActionListener(e -> processPayment(getNextDollarTotal()));
        qtyChangeBtn.addActionListener(e -> {
            JTable table = basketPanel.getTable();
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(parentFrame, "Please select an item first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            javax.swing.table.DefaultTableModel model = basketPanel.getModel();
            String itemId = model.getValueAt(selectedRow, 0).toString();
            String itemName = model.getValueAt(selectedRow, 1).toString();
            int currentQty;
            try {
                currentQty = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());
            } catch (Exception ex) {
                currentQty = 1;
            }
            String input = JOptionPane.showInputDialog(parentFrame,
                    String.format("Enter new quantity for %s (ID: %s):", itemName, itemId),
                    String.valueOf(currentQty));
            if (input == null) {
                return; // cancelled
            }
            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Quantity cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int newQty;
            try {
                newQty = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Please enter a whole number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newQty < 0) {
                JOptionPane.showMessageDialog(parentFrame, "Negative quantities are not allowed.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newQty == 0) {
                // Void the item
                basketPanel.removeSelected();
                updateStatusLabel();
                return;
            }
            // Update quantity
            model.setValueAt(newQty, selectedRow, 2);
            basketPanel.updateTotal();
            JournalService.log(itemId, newQty, "Quantity Changed");
            updateStatusLabel();
        });
        voidBtn.addActionListener(e -> { basketPanel.removeSelected(); updateStatusLabel(); });
        clearBtn.addActionListener(e -> { basketPanel.clearBasket(); updateStatusLabel(); });

        // Create main panel with BorderLayout for better organization
        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setBackground(new Color(245, 247, 250));

        // Center section - Transaction actions with status
        JPanel centerSection = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        centerSection.setBackground(new Color(245, 247, 250));
        centerSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Actions", 
            javax.swing.border.TitledBorder.CENTER, 
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(70, 70, 70)
        ));

        centerSection.add(qtyChangeBtn);
        centerSection.add(voidBtn);
        centerSection.add(clearBtn);
        centerSection.add(Box.createHorizontalStrut(20)); // Spacer

        // Add status label
        statusLabel = new JLabel("Ready for transaction");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        centerSection.add(statusLabel);

        // Right section - Payment buttons
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightSection.setBackground(new Color(245, 247, 250));
        rightSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Payment", 
            javax.swing.border.TitledBorder.RIGHT, 
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(70, 70, 70)
        ));

        rightSection.add(exactBtn);
        rightSection.add(nextDollarBtn);
        rightSection.add(payBtn);

        panel.add(centerSection, BorderLayout.CENTER);
        panel.add(rightSection, BorderLayout.EAST);
        
        // Update status for initial state
        updateStatusLabel();
    }

    // Constructor overload for backward compatibility
    public BottomBarPanel(BasketPanel basketPanel) {
        this(basketPanel, null);
    }

    // Method to update status label
    private void updateStatusLabel() {
        BigDecimal subtotal = basketPanel.calculateTotal();
        if (subtotal.compareTo(BigDecimal.ZERO) == 0) {
            statusLabel.setText("Add items to basket");
            statusLabel.setForeground(new Color(150, 150, 150));
        } else {
            statusLabel.setText("Ready for payment");
            statusLabel.setForeground(new Color(34, 139, 34));
        }
    }

    private JButton createButton(String text, Color bgColor, Color fgColor, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(150, 45)); // Slightly wider
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bold font
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusable(false);

        // Add subtle shadow effect
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        // Rounded corners via UIManager workaround
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                c.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton b = (JButton) c;

                // Background with slight gradient effect
                g2.setColor(b.getBackground());
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 20, 20);

                // Text
                FontMetrics fm = g2.getFontMetrics();
                Rectangle r = new Rectangle(0, 0, b.getWidth(), b.getHeight());
                String text = b.getText();
                int x = (r.width - fm.stringWidth(text)) / 2;
                int y = (r.height + fm.getAscent()) / 2 - 2;

                g2.setColor(b.getForeground());
                g2.setFont(b.getFont());
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        // Enhanced hover effect with animation feel
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                if (isPrimary) {
                    btn.setBackground(new Color(24, 119, 24)); // Darker green
                } else {
                    Color hoverColor = brightenColor(bgColor, 0.9f);
                    btn.setBackground(hoverColor);
                }
                // Slightly increase size for hover effect
                btn.setPreferredSize(new Dimension(155, 47));
                btn.revalidate();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
                btn.setPreferredSize(new Dimension(150, 45));
                btn.revalidate();
            }
        });

        return btn;
    }

    // Helper method to brighten colors for hover effect
    private Color brightenColor(Color color, float factor) {
        int r = Math.max(0, Math.min(255, (int)(color.getRed() * factor)));
        int g = Math.max(0, Math.min(255, (int)(color.getGreen() * factor)));
        int b = Math.max(0, Math.min(255, (int)(color.getBlue() * factor)));
        return new Color(r, g, b);
    }

    private static class DiscountOutcome {
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountedSubtotal = BigDecimal.ZERO;
        String discountName;
        double discountPercentage;
    }

    private DiscountOutcome computeDiscountedSubtotal(BigDecimal subtotal, String discountName) throws Exception {
        DiscountOutcome o = new DiscountOutcome();
        // 1) Try basket-based endpoint
        try {
            BasketPricingServiceClient bClient = new BasketPricingServiceClient();
            java.util.List<BasketPricingServiceClient.LineItem> items = BasketPricingServiceClient.fromTable(basketPanel.getModel());
            BasketPricingServiceClient.BasketResult br = bClient.applyDiscountToBasket(items, subtotal.doubleValue(), discountName);
            BigDecimal discounted = BigDecimal.valueOf(br.discountedSubtotal);
            if (discounted.compareTo(BigDecimal.ZERO) > 0) {
                o.discountedSubtotal = discounted.setScale(2, RoundingMode.HALF_UP);
                BigDecimal original = BigDecimal.valueOf(br.originalSubtotal > 0 ? br.originalSubtotal : subtotal.doubleValue());
                o.discountAmount = original.subtract(o.discountedSubtotal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                o.discountName = br.discountName;
                o.discountPercentage = br.discountPercentage;
                return o;
            }
        } catch (Exception ignore) {
            // fall through to legacy endpoint
        }
        // 2) Fallback to legacy total-based endpoint
        BasketPricingServiceClient client = new BasketPricingServiceClient();
        BasketPricingServiceClient.BasketResult r = client.applySimpleDiscount(subtotal.doubleValue(), discountName);
        o.discountAmount = BigDecimal.valueOf(r.discountAmount).setScale(2, RoundingMode.HALF_UP);
        o.discountedSubtotal = BigDecimal.valueOf(r.discountedSubtotal).setScale(2, RoundingMode.HALF_UP);
        o.discountName = r.discountName;

        o.discountPercentage = r.discountPercentage;
        return o;
    }

    private void processPayment(BigDecimal prefilledAmount) {
        BigDecimal subtotal = basketPanel.calculateTotal();
        if (subtotal.compareTo(BigDecimal.ZERO) == 0) {
            JOptionPane.showMessageDialog(parentFrame, "Nothing in basket!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show pre-payment summary with discount selection
        PaymentDialogResult dialogResult = showPrePaymentDialog();
        if (!dialogResult.proceed) {
            return;
        }

        String[] paymentOptions = {"Cash", "Credit"};
        String paymentType = (String) JOptionPane.showInputDialog(
                parentFrame,
                "Select payment method:",
                "Payment Method",
                JOptionPane.PLAIN_MESSAGE,
                null,
                paymentOptions,
                paymentOptions[0]
        );

        if (paymentType == null) {
            return;
        }

        // Use the selected discount from the dialog
        String selectedDiscountName = dialogResult.selectedDiscount;
        
        // Compute total with discount from service (basket preferred) and tax locally
        BigDecimal totalWithTax;
        DiscountOutcome outcome = null;
        try {
            outcome = computeDiscountedSubtotal(subtotal, selectedDiscountName);
            BigDecimal tax = TaxCalculator.calculateTax(outcome.discountedSubtotal);
            totalWithTax = outcome.discountedSubtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            // Fallback to local tax calculation without discount
            BigDecimal tax = TaxCalculator.calculateTax(subtotal);
            totalWithTax = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
            JOptionPane.showMessageDialog(parentFrame, "Discount service unavailable. Proceeding without discount.", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        BigDecimal payment;
        BigDecimal change;

        // Handle payment based on type
        if ("Credit".equals(paymentType)) {
            // Credit payments: payment equals total, no change
            payment = totalWithTax;
            change = BigDecimal.ZERO;
        } else {
            // Cash payments: handle as before with change calculation
            payment = prefilledAmount;

            if (payment == null) {
                BigDecimal shownSubtotal = subtotal;
                BigDecimal shownDiscount = BigDecimal.ZERO;
                BigDecimal shownTax;
                if (outcome != null) {
                    shownDiscount = outcome.discountAmount.setScale(2, RoundingMode.HALF_UP);
                    BigDecimal discountedSubtotal = shownSubtotal.subtract(shownDiscount);
                    shownTax = totalWithTax.subtract(discountedSubtotal);
                } else {
                    shownTax = totalWithTax.subtract(shownSubtotal);
                }

                String input = JOptionPane.showInputDialog(parentFrame,
                        String.format("Payment Method: %s\nSubtotal: $%.2f\nDiscount: $%.2f\nTax: $%.2f\nTotal: $%.2f\n\nEnter payment amount:",
                                paymentType, shownSubtotal, shownDiscount, shownTax, totalWithTax),
                        "Payment", JOptionPane.PLAIN_MESSAGE);

                if (input == null || input.trim().isEmpty()) return;

                try {
                    payment = new BigDecimal(input);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parentFrame, "Invalid number input.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (payment.compareTo(totalWithTax) < 0) {
                JOptionPane.showMessageDialog(parentFrame, "Insufficient amount!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            change = payment.subtract(totalWithTax);
        }

        // Load the accepted.gif icon from resources/images/
        ImageIcon acceptedIcon = null;
        java.net.URL gifURL = getClass().getClassLoader().getResource("images/accepted.gif");
        if (gifURL != null) {
            acceptedIcon = new ImageIcon(gifURL);
        } else {
            System.err.println("Accepted GIF icon not found!");
        }

        // Show different success messages based on payment type
        String successMessage;
        if ("Credit".equals(paymentType)) {
            successMessage = String.format("Credit Payment Accepted!\nAmount Charged: $%.2f", payment);
        } else {
            successMessage = String.format("Cash Payment Accepted!\nChange: $%.2f", change);
        }

        JOptionPane.showMessageDialog(
                parentFrame,
                successMessage,
                "Payment Success",
                JOptionPane.INFORMATION_MESSAGE,
                acceptedIcon
        );

        // Decide action label based on which payment button was used
        String actionLabel = "Payment";
        if (prefilledAmount != null && "Cash".equals(paymentType)) {
            try {
                BigDecimal exact = getExactTotal(selectedDiscountName);
                BigDecimal next = getNextDollarTotal(selectedDiscountName);
                if (prefilledAmount.compareTo(exact) == 0) {
                    actionLabel = "Payment (Exact Dollar)";
                } else if (prefilledAmount.compareTo(next) == 0) {
                    actionLabel = "Payment (Next Dollar)";
                }
            } catch (Exception ignore) {
                // Fallback to generic Payment label
            }
        }

        // Log payment per item in the basket with detailed transaction information
        javax.swing.table.DefaultTableModel m = basketPanel.getModel();

        // Calculate all the payment details for logging
        BigDecimal finalSubtotal = subtotal;
        BigDecimal finalDiscountAmount = BigDecimal.ZERO;
        String finalDiscountName = "None";
        double finalDiscountPercentage = 0.0;
        BigDecimal finalTax = TaxCalculator.calculateTax(subtotal);

        if (outcome != null) {
            finalSubtotal = subtotal;
            finalDiscountAmount = outcome.discountAmount;
            finalDiscountName = (outcome.discountName != null && !outcome.discountName.isEmpty()) ? 
                                outcome.discountName : selectedDiscountName;
            finalDiscountPercentage = outcome.discountPercentage;
            finalTax = TaxCalculator.calculateTax(outcome.discountedSubtotal);
        }

        // Enhanced action label with all payment details
        String enhancedActionLabel = String.format(
            "%s | Subtotal: $%.2f | Tax: $%.2f | %s | Discount: %s (%.0f%%) -$%.2f | Total: $%.2f",
            actionLabel,
            finalSubtotal,
            finalTax,
            paymentType,
            finalDiscountName,
            finalDiscountPercentage,
            finalDiscountAmount,
            totalWithTax
        );

        for (int i = 0; i < m.getRowCount(); i++) {
            String itemId = m.getValueAt(i, 0).toString();
            int qty = Integer.parseInt(m.getValueAt(i, 2).toString());
            JournalService.log(itemId, qty, enhancedActionLabel);
        }

        ReceiptService.saveReceipt(basketPanel.getModel(), payment, change);
        ReceiptWindow.showReceipt(parentFrame, basketPanel.getModel(), totalWithTax, payment, change);

        basketPanel.clearBasket(true);
        updateStatusLabel(); // Update status after clearing basket
    }

    // Class to hold dialog results
    private static class PaymentDialogResult {
        boolean proceed;
        String selectedDiscount;
        
        PaymentDialogResult(boolean proceed, String selectedDiscount) {
            this.proceed = proceed;
            this.selectedDiscount = selectedDiscount;
        }
    }

    // Shows a pre-payment dialog with items, discount selection and totals.
    // Returns PaymentDialogResult with proceed flag and selected discount.
    private PaymentDialogResult showPrePaymentDialog() {
        JDialog dialog = new JDialog(parentFrame, "Order Summary", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        // Copy basket items into a non-editable table for display
        javax.swing.table.DefaultTableModel src = basketPanel.getModel();
        String[] cols = new String[]{"ID", "Name", "Qty", "Price"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (int i = 0; i < src.getRowCount(); i++) {
            Object id = src.getValueAt(i, 0);
            Object name = src.getValueAt(i, 1);
            Object qty = src.getValueAt(i, 2);
            Object price = src.getValueAt(i, 3);
            model.addRow(new Object[]{id, name, qty, price});
        }
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(520, 220));
        dialog.add(scroll, BorderLayout.CENTER);

        // Create discount selection panel
        JPanel discountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        discountPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Discount Options", 
            javax.swing.border.TitledBorder.LEFT, 
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(70, 70, 70)
        ));

        JLabel discountLabel = new JLabel("Select Discount:");
        discountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JComboBox<String> discountCombo = new JComboBox<>(new String[] {
                "None (0%)",
                "Cash Payment Discount (1%)",
                "Silver Loyalty Tier (5%)",
                "Gold Loyalty Tier (10%)",
                "Digital Coupon Average (20%)"
        });

        // Style the dropdown
        discountCombo.setPreferredSize(new Dimension(200, 35));
        discountCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        discountCombo.setBackground(Color.WHITE);
        discountCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        discountPanel.add(discountLabel);
        discountPanel.add(discountCombo);

        // Totals panel that will be updated based on discount selection
        JPanel totalsPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        JLabel subtotalValueLabel = new JLabel();
        JLabel discountValueLabel = new JLabel();
        JLabel taxValueLabel = new JLabel();
        JLabel totalValueLabel = new JLabel();
        JLabel discountNameLabel = new JLabel("Discount:");

        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(subtotalValueLabel);
        totalsPanel.add(discountNameLabel);
        totalsPanel.add(discountValueLabel);
        totalsPanel.add(new JLabel("Tax:"));
        totalsPanel.add(taxValueLabel);
        totalsPanel.add(new JLabel("Total:"));
        totalsPanel.add(totalValueLabel);

        // Method to update totals based on selected discount
        Runnable updateTotals = () -> {
            BigDecimal subtotal = basketPanel.calculateTotal();
            String selectedDiscountText = (String) discountCombo.getSelectedItem();
            String discountName = getDiscountNameFromText(selectedDiscountText);
            
            subtotalValueLabel.setText("$" + subtotal);
            
            try {
                DiscountOutcome out = computeDiscountedSubtotal(subtotal, discountName);
                BigDecimal discountAmountBD = out.discountAmount.setScale(2, RoundingMode.HALF_UP);
                BigDecimal discountedSubtotal = out.discountedSubtotal;
                BigDecimal tax = TaxCalculator.calculateTax(discountedSubtotal).setScale(2, RoundingMode.HALF_UP);
                BigDecimal finalTotalBD = discountedSubtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
                
                String dn = (out.discountName != null && !out.discountName.isEmpty()) ? out.discountName : discountName;
                discountNameLabel.setText(String.format("Discount (%s %.0f%%):", dn, out.discountPercentage));
                discountValueLabel.setText("-$" + discountAmountBD);
                taxValueLabel.setText("$" + tax);
                totalValueLabel.setText("$" + finalTotalBD);
            } catch (Exception ex) {
                // Fallback: no discount, local tax
                BigDecimal tax = TaxCalculator.calculateTax(subtotal);
                BigDecimal finalTotalBD = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
                discountNameLabel.setText("Discount:");
                discountValueLabel.setText("-$0.00");
                taxValueLabel.setText("$" + tax);
                totalValueLabel.setText("$" + finalTotalBD);
            }
        };

        // Initial update and add listener for discount changes
        updateTotals.run();
        discountCombo.addActionListener(e -> updateTotals.run());

        // Create south panel with discount selection and totals
        JPanel south = new JPanel(new BorderLayout());
        south.add(discountPanel, BorderLayout.NORTH);
        south.add(totalsPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton proceedBtn = new JButton("Proceed");
        JButton cancelBtn = new JButton("Cancel");
        buttons.add(cancelBtn);
        buttons.add(proceedBtn);
        south.add(buttons, BorderLayout.SOUTH);

        dialog.add(south, BorderLayout.SOUTH);

        final PaymentDialogResult[] result = {new PaymentDialogResult(false, "None")};

        // Wire up actions
        cancelBtn.addActionListener(e -> {
            result[0] = new PaymentDialogResult(false, "None");
            dialog.dispose();
        });
        proceedBtn.addActionListener(e -> {
            String selectedDiscountText = (String) discountCombo.getSelectedItem();
            String discountName = getDiscountNameFromText(selectedDiscountText);
            result[0] = new PaymentDialogResult(true, discountName);
            dialog.dispose();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame); // Use parentFrame directly
        dialog.setVisible(true);

        return result[0];
    }

    private String getDiscountNameFromText(String discountText) {
        if (discountText == null) return "None";
        int idx = discountText.indexOf(" (");
        return idx > 0 ? discountText.substring(0, idx).trim() : discountText.trim();
    }

    private BigDecimal getExactTotal() {
        return getExactTotal("None"); // Default to no discount for button calculations
    }

    private BigDecimal getExactTotal(String discountName) {
        BigDecimal subtotal = basketPanel.calculateTotal();
        try {
            DiscountOutcome out = computeDiscountedSubtotal(subtotal, discountName);
            BigDecimal tax = TaxCalculator.calculateTax(out.discountedSubtotal);
            return out.discountedSubtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            BigDecimal tax = TaxCalculator.calculateTax(subtotal);
            return subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal getNextDollarTotal() {
        return getNextDollarTotal("None"); // Default to no discount for button calculations
    }

    private BigDecimal getNextDollarTotal(String discountName) {
        BigDecimal total = getExactTotal(discountName);
        return new BigDecimal(Math.ceil(total.doubleValue())).setScale(0, RoundingMode.UP);
    }

    public JPanel getPanel() {
        return panel;
    }
}
