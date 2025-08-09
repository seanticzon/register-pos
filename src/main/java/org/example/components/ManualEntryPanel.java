package org.example.components;

import org.example.models.services.JournalService;

import javax.swing.*;
import java.awt.*;
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
        addButton = new JButton("Add");

        addButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            if (!code.isEmpty()) {
                // ✅ Log the action before scanning
                journalService.log(code, 1, "Added (Keyboard)");

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
