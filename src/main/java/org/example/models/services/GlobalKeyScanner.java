package org.example.models.services;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class GlobalKeyScanner {
    private static StringBuilder buffer = new StringBuilder();

    public static void install(POSPanel posPanel, JournalService journalService) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                // ✅ STEP 1: Check if the user is typing in a JTextField (manual input)
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner instanceof JTextField || focusOwner instanceof JTextArea) {
                    return false; // Let normal typing happen
                }

                // ✅ STEP 2: Handle scanner input (no text field focused)
                if (e.getID() == KeyEvent.KEY_TYPED) {
                    char c = e.getKeyChar();
                    if (c == '\n' || c == '\r') { // Enter key
                        String code = buffer.toString().trim();
                        if (!code.isEmpty()) {
                            // 📜 Log scanner action
                            journalService.log(code, 1, "Added (Scanner)");

                            // Process the scanned item
                            posPanel.scanItem(code);
                        }
                        buffer.setLength(0);
                    } else {
                        buffer.append(c);
                    }
                }
                return false;
            }
        });
    }
}
