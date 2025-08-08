package org.example.models.services;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class POSPanel {
    private DefaultTableModel posModel;
    private JLabel totalLabel;
    private JFrame frame;
    private JournalService journalService;

    private JPanel productPanel;
    private JScrollPane productScroll;
    private List<String> productNames;
    private int currentPage = 0;
    private final int ITEMS_PER_PAGE = 50;

    public POSPanel() {
        PricebookService.loadFromTSV();
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("POS System");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // POS table (Basket)
        posModel = new DefaultTableModel(new String[]{"ID", "Name", "Qty", "Price"}, 0);
        JTable posTable = new JTable(posModel);
        JScrollPane posScroll = new JScrollPane(posTable);

        JLabel basketLabel = new JLabel("Basket");
        basketLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel basketPanel = new JPanel(new BorderLayout());
        basketPanel.add(basketLabel, BorderLayout.NORTH);
        basketPanel.add(posScroll, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new GridLayout(1, 1));
        leftPanel.add(basketPanel);
        leftPanel.setPreferredSize(new Dimension(350, 0));

        // Load product names
        productNames = PricebookService.getAllProductNames();

        // Product buttons
        productPanel = new JPanel(new GridLayout(0, 10, 2, 2)); // 10 columns
        productScroll = new JScrollPane(productPanel);
        productScroll.setBorder(BorderFactory.createTitledBorder("Items"));

        // Pagination
        JButton prevBtn = new JButton("Previous");
        JButton nextBtn = new JButton("Next");

        Dimension navButtonSize = new Dimension(120, 40);
        prevBtn.setPreferredSize(navButtonSize);
        nextBtn.setPreferredSize(navButtonSize);
        prevBtn.setFont(new Font("Arial", Font.BOLD, 14));
        nextBtn.setFont(new Font("Arial", Font.BOLD, 14));

        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateProductPanel();
            }
        });

        nextBtn.addActionListener(e -> {
            if ((currentPage + 1) * ITEMS_PER_PAGE < productNames.size()) {
                currentPage++;
                updateProductPanel();
            }
        });

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.add(prevBtn);
        paginationPanel.add(nextBtn);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(productScroll, BorderLayout.CENTER);
        rightPanel.add(paginationPanel, BorderLayout.SOUTH);

        // Buttons: PAY, VOID, VOID ALL
        JButton payBtn = new JButton("PAY");
        JButton voidBtn = new JButton("VOID");
        JButton clearBtn = new JButton("VOID ALL");

        Dimension actionButtonSize = new Dimension(140, 50);
        payBtn.setPreferredSize(actionButtonSize);
        voidBtn.setPreferredSize(actionButtonSize);
        clearBtn.setPreferredSize(actionButtonSize);
        payBtn.setFont(new Font("Arial", Font.BOLD, 16));
        voidBtn.setFont(new Font("Arial", Font.BOLD, 16));
        clearBtn.setFont(new Font("Arial", Font.BOLD, 16));

        journalService = new JournalService();

        payBtn.addActionListener(e -> {
            BigDecimal totalAmount = calculateTotal();
            String input = promptForPayment(totalAmount);
            if (input == null) return;

            try {
                BigDecimal payment = new BigDecimal(input);

                if (payment.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(frame, "Amount must be greater than 0.", "Invalid", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (payment.compareTo(totalAmount) < 0) {
                    JOptionPane.showMessageDialog(frame, "Insufficient payment amount.", "Invalid", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BigDecimal change = payment.subtract(totalAmount).setScale(2, RoundingMode.HALF_UP);
                JOptionPane.showMessageDialog(frame, "Payment Accepted!\nChange: $" + change);

                ReceiptService.saveReceipt(posModel, payment, change);
                ReceiptWindow.showReceipt(posModel, totalAmount, payment, change);

                posModel.setRowCount(0);
                updateTotal();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        clearBtn.addActionListener(e -> {
            posModel.setRowCount(0);
            updateTotal();
        });

        voidBtn.addActionListener(e -> {
            int selectedRow = posTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select an item to void.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String itemId = posModel.getValueAt(selectedRow, 0).toString();
            int qty = Integer.parseInt(posModel.getValueAt(selectedRow, 2).toString());

            journalService.log(itemId, qty, "Voided");
            posModel.removeRow(selectedRow);
            updateTotal();
        });

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(voidBtn);
        buttonPanel.add(payBtn);
        buttonPanel.add(clearBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        frame.setLayout(new BorderLayout());
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        updateProductPanel(); // Load first page
        frame.setVisible(true);
    }

    private void updateProductPanel() {
        productPanel.removeAll();

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, productNames.size());

        for (int i = start; i < end; i++) {
            String name = productNames.get(i);
            JButton btn = new JButton(name);
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setFont(new Font("Arial", Font.PLAIN, 9));
            btn.setMargin(new Insets(0, 0, 0, 0));

            btn.addActionListener(e -> {
                Optional<String> idOpt = PricebookService.getIdByName(name);
                idOpt.ifPresent(this::scanItem);
            });

            productPanel.add(btn);
        }

        productPanel.revalidate();
        productPanel.repaint();
    }

    public void scanItem(String id) {
        id = id.trim();
        if (id.isEmpty()) return;

        for (int i = 0; i < posModel.getRowCount(); i++) {
            if (posModel.getValueAt(i, 0).equals(id)) {
                int qty = Integer.parseInt(posModel.getValueAt(i, 2).toString());
                posModel.setValueAt(qty + 1, i, 2);
                updateTotal();
                journalService.log(id, qty + 1, "Quantity Increased");
                return;
            }
        }

        Item item = PricebookService.getItemById(id);
        if (item != null) {
            posModel.addRow(new Object[]{item.id, item.name, 1, item.price});
            updateTotal();
            journalService.log(item.id, 1, "Added");
        }
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < posModel.getRowCount(); i++) {
            Object qtyObj = posModel.getValueAt(i, 2);
            Object priceObj = posModel.getValueAt(i, 3);

            int qty = Integer.parseInt(qtyObj.toString());
            BigDecimal price = new BigDecimal(priceObj.toString());

            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        totalLabel.setText("Total: $" + total.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < posModel.getRowCount(); i++) {
            int qty = (int) posModel.getValueAt(i, 2);
            BigDecimal price = (BigDecimal) posModel.getValueAt(i, 3);
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private String promptForPayment(BigDecimal total) {
        return JOptionPane.showInputDialog(
                frame,
                "Total is $" + total + "\nEnter payment amount:",
                "Payment",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    public JFrame getFrame() {
        return frame;
    }
}
