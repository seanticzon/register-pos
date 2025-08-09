package org.example.components;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TitleBarPanel extends JPanel {
    private final JLabel dateTimeLabel;

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

        // Right: Date + Time (real-time)
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateTimeLabel.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(dateTimeLabel);

        // Start clock
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
        updateDateTime();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd yyyy | hh:mm:ss a");
        dateTimeLabel.setText(sdf.format(new Date()));
    }
}
