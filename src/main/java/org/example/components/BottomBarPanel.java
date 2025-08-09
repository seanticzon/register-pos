package org.example.components;

import org.example.models.services.ReceiptService;
import org.example.models.services.ReceiptWindow;
import org.example.models.services.JournalService;
import org.example.utils.TaxCalculator;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BottomBarPanel {
    private final JPanel panel;
    private final BasketPanel basketPanel;

    public BottomBarPanel(BasketPanel basketPanel) {
        this.basketPanel = basketPanel;

        // Create buttons with consistent style
        JButton payBtn = createButton("PAY", new Color(60, 130, 255), Color.WHITE, true);
        JButton exactBtn = createButton("EXACT DOLLAR", new Color(230, 230, 230), new Color(70, 70, 70), false);
        JButton nextDollarBtn = createButton("NEXT DOLLAR", new Color(230, 230, 230), new Color(70, 70, 70), false);
        JButton voidBtn = createButton("VOID", new Color(230, 230, 230), new Color(70, 70, 70), false);
        JButton clearBtn = createButton("VOID ALL", new Color(230, 230, 230), new Color(70, 70, 70), false);

        // Add action listeners exactly as before
        payBtn.addActionListener(e -> processPayment(null));
        exactBtn.addActionListener(e -> processPayment(getExactTotal()));
        nextDollarBtn.addActionListener(e -> processPayment(getNextDollarTotal()));
        voidBtn.addActionListener(e -> basketPanel.removeSelected());
        clearBtn.addActionListener(e -> basketPanel.clearBasket());

        panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 247, 250)); // Light background

        // Add buttons in logical order
        panel.add(voidBtn);
        panel.add(clearBtn);
        panel.add(exactBtn);
        panel.add(nextDollarBtn);
        panel.add(payBtn);
    }

    private JButton createButton(String text, Color bgColor, Color fgColor, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(140, 50));
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setBorderPainted(false);
        btn.setFocusable(false);

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

                // Background
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

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isPrimary) {
                    btn.setBackground(new Color(40, 110, 255));
                } else {
                    btn.setBackground(new Color(210, 210, 210));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void processPayment(BigDecimal prefilledAmount) {
        BigDecimal subtotal = basketPanel.calculateTotal();
        if (subtotal.compareTo(BigDecimal.ZERO) == 0) {
            JOptionPane.showMessageDialog(null, "Nothing in basket!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] paymentOptions = {"Cash", "Credit"};
        String paymentType = (String) JOptionPane.showInputDialog(
                null,
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

        BigDecimal tax = TaxCalculator.calculateTax(subtotal);
        BigDecimal totalWithTax = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        BigDecimal payment = prefilledAmount;

        if (payment == null) {
            String input = JOptionPane.showInputDialog(null,
                    String.format("Payment Method: %s\nSubtotal: $%.2f\nTax (7%%): $%.2f\nTotal: $%.2f\n\nEnter payment amount:",
                            paymentType, subtotal, tax, totalWithTax),
                    "Payment", JOptionPane.PLAIN_MESSAGE);

            if (input == null || input.trim().isEmpty()) return;

            try {
                payment = new BigDecimal(input);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid number input.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (payment.compareTo(totalWithTax) < 0) {
            JOptionPane.showMessageDialog(null, "Insufficient amount!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal change = payment.subtract(totalWithTax);

        // Load the accepted.gif icon from resources/images/
        ImageIcon acceptedIcon = null;
        java.net.URL gifURL = getClass().getClassLoader().getResource("images/accepted.gif");
        if (gifURL != null) {
            acceptedIcon = new ImageIcon(gifURL);
        } else {
            System.err.println("Accepted GIF icon not found!");
        }

        JOptionPane.showMessageDialog(
                null,
                String.format("Payment Accepted!\nChange: $%.2f", change),
                "Payment Success",
                JOptionPane.INFORMATION_MESSAGE,
                acceptedIcon
        );

        String description = String.format(
                "Payment Type: %s | Subtotal: $%.2f | Tax: $%.2f | Total: $%.2f | Tendered: $%.2f | Change: $%.2f",
                paymentType, subtotal, tax, totalWithTax, payment, change
        );
        JournalService.log("SYS_PAYMENT", 1, description, tax);

        ReceiptService.saveReceipt(basketPanel.getModel(), payment, change);
        ReceiptWindow.showReceipt(basketPanel.getModel(), totalWithTax, payment, change);

        basketPanel.clearBasket(true);
    }


    private BigDecimal getExactTotal() {
        BigDecimal subtotal = basketPanel.calculateTotal();
        BigDecimal tax = TaxCalculator.calculateTax(subtotal);
        return subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getNextDollarTotal() {
        BigDecimal total = getExactTotal();
        return new BigDecimal(Math.ceil(total.doubleValue())).setScale(0, RoundingMode.UP);
    }

    public JPanel getPanel() {
        return panel;
    }
}
