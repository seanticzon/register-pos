
package org.example;
import org.example.models.services.DatabaseManager;
import org.example.models.services.GlobalKeyScanner;
import org.example.models.services.JournalService;
import org.example.models.services.POSPanel;



public class Main {

    public static void main(String[] args) {
        DatabaseManager.init(); // Starts H2 and sets up DB

        // Connect to the journal server
        JournalService.connectToServer();

        // Add shutdown hook to properly close connections
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down POS system...");
            JournalService.disconnectFromServer();
        }));

        javax.swing.SwingUtilities.invokeLater(() -> {
            POSPanel posPanel = new POSPanel();
        });
    }
}
