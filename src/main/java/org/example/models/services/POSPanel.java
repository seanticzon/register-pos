package org.example.models.services;

import org.example.components.*;
import javax.swing.*;
import java.awt.*;

public class POSPanel {

    private JFrame frame;
    private BasketPanel basketPanel;
    private ProductGridPanel productGridPanel;
    private BottomBarPanel bottomBarPanel;

    public POSPanel() {
        PricebookService.loadFromTSV();  // Load TSV on startup
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("POS System");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JournalService journalService = new JournalService();

        basketPanel = new BasketPanel(journalService);
        productGridPanel = new ProductGridPanel(basketPanel::scanItem, journalService);
        bottomBarPanel = new BottomBarPanel(basketPanel);
        ManualEntryPanel manualEntryPanel = new ManualEntryPanel(basketPanel::scanItem, journalService);

        // Load icon from resources/images/example.png
        ImageIcon posIcon = null;
        java.net.URL iconURL = getClass().getClassLoader().getResource("images/cash-machine.png");
        if (iconURL != null) {
            posIcon = new ImageIcon(iconURL);
        } else {
            System.err.println("Icon not found at images/example.png!");
        }

        TitleBarPanel titleBar = new TitleBarPanel("My POS System", posIcon);

        // Combine title bar + manual entry
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(titleBar, BorderLayout.NORTH);
        topContainer.add(manualEntryPanel.getPanel(), BorderLayout.SOUTH);

        frame.add(topContainer, BorderLayout.NORTH);
        frame.add(basketPanel.getPanel(), BorderLayout.WEST);
        frame.add(productGridPanel.getPanel(), BorderLayout.CENTER);
        frame.add(bottomBarPanel.getPanel(), BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public void scanItem(String code) {
        basketPanel.scanItem(code);
    }
}
