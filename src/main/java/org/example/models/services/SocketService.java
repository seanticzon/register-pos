package org.example.models.services;

import java.io.*;
import java.net.Socket;

public class SocketService {
    private String serverHost = "localhost";  // Changed from static final to instance variable
    private int serverPort = 1234;           // Changed from static final to instance variable
    private static SocketService instance;
    
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private boolean connected = false;

    private SocketService() {
        // Private constructor for singleton
    }

    public static synchronized SocketService getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    // Add getter and setter methods for dynamic configuration
    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);  // Use instance variables instead of constants
            
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            
            connected = true;
            System.out.println("[SocketService] Connected to server at " + serverHost + ":" + serverPort);
            return true;
            
        } catch (Exception e) {
            System.err.println("[SocketService] Failed to connect to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public void sendLog(String logMessage) {
        if (!connected) {
            System.out.println("[SocketService] Not connected to server. Attempting to reconnect...");
            if (!connect()) {
                System.err.println("[SocketService] Failed to reconnect. Log not sent: " + logMessage);
                return;
            }
        }

        try {
            bufferedWriter.write(logMessage);
            bufferedWriter.newLine(); // Add newline for better server parsing
            bufferedWriter.flush();

        } catch (Exception e) {
            System.err.println("[SocketService] Error sending log: " + e.getMessage());
            connected = false;
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
            connected = false;
            System.out.println("[SocketService] Disconnected from server");
            
        } catch (Exception e) {
            System.err.println("[SocketService] Error during disconnect: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}