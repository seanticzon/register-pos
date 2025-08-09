package org.example.components;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TitleBarPanel extends JPanel {
    private final JLabel dateTimeLabel;
    private final JLabel welcomeLabel;
    private final String[] welcomeMessages = {
            "WELCOME",
            "HELLO THERE",
            "GOOD TO SEE YOU",
            "HAVE A GREAT DAY",
            "READY TO START?",
            "LET'S DO THIS",
            "YOUR POS SYSTEM"
    };
    private int welcomeIndex = 0;

    public TitleBarPanel(String title) {
        this(title, loadIcon("images/cash-machine.png"));
    }

    public TitleBarPanel(String title, ImageIcon icon) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 70));
        setBackground(new Color(30, 136, 229)); // Deep blue

        // Left: Icon + Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        leftPanel.setOpaque(false);

        if (icon != null) {
            JLabel iconLabel = new JLabel(
                    new ImageIcon(icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH))
            );
            leftPanel.add(iconLabel);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        leftPanel.add(titleLabel);

        // Center: Welcome rotating label
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        centerPanel.setOpaque(false);

        welcomeLabel = new JLabel(welcomeMessages[welcomeIndex]);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));  // Increased font size here
        welcomeLabel.setForeground(Color.WHITE);
        centerPanel.add(welcomeLabel);

        // Right: Date + Time (real-time)
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateTimeLabel.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(dateTimeLabel);

        // Start clock
        Timer clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();
        updateDateTime();

        // Start welcome message rotator timer (change message every 4 seconds)
        Timer welcomeTimer = new Timer(4000, e -> cycleWelcomeMessage());
        welcomeTimer.start();

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd yyyy | hh:mm:ss a");
        dateTimeLabel.setText(sdf.format(new Date()));
    }

    private void cycleWelcomeMessage() {
        welcomeIndex = (welcomeIndex + 1) % welcomeMessages.length;
        welcomeLabel.setText(welcomeMessages[welcomeIndex]);
    }

    // Helper method to load icon from resources folder
    private static ImageIcon loadIcon(String path) {
        URL imgURL = TitleBarPanel.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
