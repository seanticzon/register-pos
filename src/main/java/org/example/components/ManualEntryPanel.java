package org.example.components;

import org.example.models.services.JournalService;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.function.Consumer;

public class ManualEntryPanel {
    private JPanel panel;
    private JTextField codeField;
    private JButton addButton;
    private JournalService journalService;

    public ManualEntryPanel(Consumer<String> onScan, JournalService journalService) {
        this.journalService = journalService;

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codeField = new JTextField(20);

        // ✅ Prevent IntelliJ-specific clipboard formats from causing errors
        codeField.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(TransferSupport support) {
                try {
                    if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                        codeField.replaceSelection(data);
                        return true;
                    }
                } catch (Exception ex) {
                    // Swallow exceptions so IntelliJ formats don't spam logs
                }
                return false;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }
        });

        addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            if (!code.isEmpty()) {
                onScan.accept(code);
                codeField.setText("");
            }
        });

        panel.add(new JLabel("Enter Code:"));
        panel.add(codeField);
        panel.add(addButton);
    }

    public JPanel getPanel() {
        return panel;
    }
}
