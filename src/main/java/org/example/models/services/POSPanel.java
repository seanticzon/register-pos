package org.example.models.services;

import org.example.components.*;
import javax.swing.*;
import java.awt.*;

public class POSPanel {

    private JFrame frame;
    private BasketPanel basketPanel;
    private ProductGridPanel productGridPanel;
    private BottomBarPanel bottomBarPanel;
    private JournalService journalService;

    public POSPanel() {

        // ✅ Load data from TSV into DB
        PricebookService.loadFromTSV();

        // ✅ Populate fast in-memory cache from DB
        PricebookService.loadCacheFromDatabase();

        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("POS System");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        journalService = new JournalService();

        // Panels
        basketPanel = new BasketPanel(journalService);
        productGridPanel = new ProductGridPanel(code -> scanItem(code, "Panel"), journalService);
        bottomBarPanel = new BottomBarPanel(basketPanel, frame);
        ManualEntryPanel manualEntryPanel = new ManualEntryPanel(code -> scanItem(code, "Keyboard"), journalService);

        // ✅ Install Global Scanner so barcode scans work anywhere
        GlobalKeyScanner.install(this); // 🔹 removed journalService from call

        // Load icon from resources
        ImageIcon posIcon = null;
        java.net.URL iconURL = getClass().getClassLoader().getResource("images/cash-machine.png");
        if (iconURL != null) {
            posIcon = new ImageIcon(iconURL);
        } else {
            System.err.println("Icon not found at images/cash-machine.png!");
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

    // New method to specify the source ("Panel" or "Scanner")
    public void scanItem(String code, String source) {
        JTable table = basketPanel.getTable();

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        table.clearSelection();

        // Log with source
        journalService.log(code, 1, "Added (" + source + ")");
        basketPanel.scanItem(code);
    }

    // Kept for backward compatibility (defaults to Panel)
    public void scanItem(String code) {
        scanItem(code, "Panel");
    }
}
