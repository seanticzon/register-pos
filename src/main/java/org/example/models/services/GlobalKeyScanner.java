package org.example.models.services;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GlobalKeyScanner {
    private static StringBuilder buffer = new StringBuilder();

    public static void install(POSPanel posPanel) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_TYPED) {
                    char c = e.getKeyChar();
                    if (c == '\n' || c == '\r') { // Enter
                        String code = buffer.toString().trim();
                        if (!code.isEmpty()) {
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

