package org.example.components;

import org.example.models.services.JournalService;
import org.example.models.services.PricebookService;
import org.example.models.services.PopularItemsService;
import org.example.models.services.Item;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ProductGridPanel {
    private final JPanel container;
    private final JPanel grid;
    private final JPanel popularPanel;
    private final JLabel popularLabel;
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

        // Popular Items label (keep this, remove title border duplication)
        popularLabel = new JLabel("Popular Items");
        popularLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        popularLabel.setForeground(new Color(50, 50, 50));
        popularLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));

        JButton reloadBtn = new JButton("Reload Popular");
        reloadBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reloadBtn.setFocusPainted(false);
        reloadBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reloadBtn.addActionListener(e -> reloadPopularItems(onItemClicked));

        JPanel popularHeaderPanel = new JPanel(new BorderLayout());
        popularHeaderPanel.setOpaque(false);
        popularHeaderPanel.add(popularLabel, BorderLayout.WEST);
        popularHeaderPanel.add(reloadBtn, BorderLayout.EAST);

        popularPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        popularPanel.setOpaque(false);

        // --- popularContainer without title text to avoid duplicate label ---
        JPanel popularContainer = new JPanel();
        popularContainer.setLayout(new BorderLayout());
        popularContainer.setOpaque(true);
        popularContainer.setBackground(new Color(255, 250, 240)); // subtle cream background for popular
        popularContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 120), 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        popularContainer.add(popularHeaderPanel, BorderLayout.NORTH);
        popularContainer.add(popularPanel, BorderLayout.CENTER);

        grid = new JPanel(new GridLayout(ROWS, COLUMNS, 15, 15));
        grid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1)
        ));
        scrollPane.getViewport().setBackground(new Color(245, 245, 245)); // Light gray background

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

        // --- gridContainer with title ---
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(true);
        gridContainer.setBackground(new Color(250, 250, 255)); // subtle bluish background for grid
        gridContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(150, 170, 220), 2, true),
                        "Product Catalog",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI Semibold", Font.BOLD, 16),
                        new Color(50, 80, 150)
                ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        gridContainer.add(scrollPane, BorderLayout.CENTER);
        gridContainer.add(navPanel, BorderLayout.SOUTH);

        container = new JPanel();
        container.setLayout(new BorderLayout(15, 15));
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        container.setBackground(new Color(245, 245, 245));

        container.add(popularContainer, BorderLayout.NORTH);
        container.add(gridContainer, BorderLayout.CENTER);

        reloadPopularItems(onItemClicked);
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

    private void reloadPopularItems(Consumer<String> onItemClicked) {
        popularPanel.removeAll();

        List<Item> updatedPopularItems = PopularItemsService.getPopularItems(10);
        for (Item item : updatedPopularItems) {
            JButton btn = new JButton("<html><center>" + item.name + "</center></html>");
            btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            btn.setPreferredSize(new Dimension(120, 80));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(255, 235, 205));
            btn.setForeground(new Color(80, 50, 20));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 180, 80)),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            btn.setOpaque(true);

            btn.addActionListener(e -> {
                onItemClicked.accept(item.id);
            });

            popularPanel.add(btn);
        }

        popularPanel.revalidate();
        popularPanel.repaint();
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

            btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(2, c.getHeight() - 6, c.getWidth() - 4, 6, 8, 8);
                    super.paint(g2, c);
                    g2.dispose();
                }
            });

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
