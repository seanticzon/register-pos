package org.example.components;

import org.example.models.services.JournalService;
import org.example.models.services.PricebookService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ProductGridPanel {
    private final JPanel container;
    private final JPanel grid;
    private final JLabel pageLabel;
    private final List<String> productNames;
    private final JournalService journalService;
    private int currentPage = 0;

    private final int COLUMNS = 5;
    private final int ROWS = 4;
    private final int ITEMS_PER_PAGE = COLUMNS * ROWS;

    public ProductGridPanel(Consumer<String> onItemClicked, JournalService journalService) {
        this.journalService = journalService;
        productNames = PricebookService.getAllProductNames();

        // Grid panel with padding around buttons
        grid = new JPanel(new GridLayout(ROWS, COLUMNS, 15, 15));
        grid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(250, 250, 250)); // Light background

        // Navigation controls with modern look
        JButton prevBtn = createNavButton("◀ Previous");
        JButton nextBtn = createNavButton("Next ▶");

        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateGrid(onItemClicked);
            }
        });

        nextBtn.addActionListener(e -> {
            if ((currentPage + 1) * ITEMS_PER_PAGE < productNames.size()) {
                currentPage++;
                updateGrid(onItemClicked);
            }
        });

        pageLabel = new JLabel("", SwingConstants.CENTER);
        pageLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        pageLabel.setForeground(new Color(80, 80, 80));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        navButtons.setOpaque(false);
        navButtons.add(prevBtn);
        navButtons.add(nextBtn);

        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);
        navPanel.add(navButtons, BorderLayout.CENTER);
        navPanel.add(pageLabel, BorderLayout.SOUTH);

        container = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // subtle vertical gradient background
                Graphics2D g2d = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                Color c1 = new Color(245, 247, 250);
                Color c2 = new Color(230, 235, 242);
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };

        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(navPanel, BorderLayout.SOUTH);

        updateGrid(onItemClicked);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(60, 130, 255));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(40, 110, 255));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 130, 255));
            }
        });

        return btn;
    }

    private void updateGrid(Consumer<String> onItemClicked) {
        grid.removeAll();

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, productNames.size());

        for (int i = start; i < end; i++) {
            String name = productNames.get(i);

            JButton btn = new JButton("<html><center>" + name + "</center></html>");
            btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            btn.setPreferredSize(new Dimension(150, 90));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(45, 45, 45));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            btn.setOpaque(true);

            // Subtle shadow effect - paint after
            btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw subtle shadow
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(2, c.getHeight() - 6, c.getWidth() - 4, 6, 8, 8);

                    super.paint(g2, c);
                    g2.dispose();
                }
            });

            // Hover effect: brighten background & border
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(240, 245, 255));
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(60, 130, 255), 2),
                            BorderFactory.createEmptyBorder(9, 9, 9, 9)
                    ));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(200, 200, 200)),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                }
            });

            btn.addActionListener(e -> {
                Optional<String> id = PricebookService.getIdByName(name);
                id.ifPresent(itemId -> {
                    onItemClicked.accept(itemId);
                    journalService.log(itemId, 1, "Added (Panel)");
                });
            });

            grid.add(btn);
        }

        int totalPages = (int) Math.ceil((double) productNames.size() / ITEMS_PER_PAGE);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);

        grid.revalidate();
        grid.repaint();
    }

    public JPanel getPanel() {
        return container;
    }
}
