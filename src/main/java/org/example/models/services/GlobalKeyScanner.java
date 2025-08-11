package org.example.models.services;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GlobalKeyScanner {
    private static StringBuilder buffer = new StringBuilder();
    private static long lastEventTime = 0;

    public static void install(POSPanel posPanel) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            long now = System.currentTimeMillis();
            if (now - lastEventTime > 300) {
                buffer.setLength(0);
            }
            lastEventTime = now;

            if (e.getID() == KeyEvent.KEY_PRESSED) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER) {
                    String code = buffer.toString().trim();
                    if (!code.isEmpty()) {
                        posPanel.scanItem(code, "Scanner"); // ✅ now marks source
                    }
                    buffer.setLength(0);
                    return true; // consume
                }
            } else if (e.getID() == KeyEvent.KEY_TYPED) {
                char c = e.getKeyChar();
                if (!Character.isISOControl(c)) {
                    buffer.append(c);
                }
            }
            return false;
        });
    }
}