
package org.example;
import org.example.models.services.DatabaseManager;
import org.example.models.services.GlobalKeyScanner;
import org.example.models.services.POSPanel;



public class Main {
    public static void main(String[] args) {
        DatabaseManager.init(); // Starts H2 and sets up DB
        javax.swing.SwingUtilities.invokeLater(() -> {
            POSPanel posPanel = new POSPanel();
        });
    }
}
