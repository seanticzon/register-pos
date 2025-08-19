package org.example.components;

import org.example.models.services.SocketService;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TitleBarPanel extends JPanel {
    private final JLabel dateTimeLabel;
    private final JLabel welcomeLabel;
    private final JTextField hostField;
    private final JTextField portField;
    private final JLabel connectionStatusLabel;
    private final JButton connectBtn;
    private final String[] welcomeMessages = {
            "✨ Welcome to Your POS",
            "🚀 Ready to Start Selling?",
            "💼 Let's Make Some Sales!",
            "🎯 Your Success Starts Here!",
            "⚡ Power Up Your Business!",
            "🌟 Have an Amazing Day!"
    };
    private int welcomeIndex = 0;

    public TitleBarPanel(String title) {
        this(title, loadIcon("images/cash-machine.png"));
    }

    public TitleBarPanel(String title, ImageIcon icon) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 90)); // Reduced height
        setBackground(new Color(45, 55, 72));
        
        // Initialize components directly in constructor
        SocketService socketService = SocketService.getInstance();
        welcomeLabel = new JLabel(welcomeMessages[welcomeIndex]);
        dateTimeLabel = new JLabel();
        connectionStatusLabel = new JLabel("⚪ Disconnected");
        hostField = new JTextField(socketService.getServerHost(), 8); // Reduced width
        portField = new JTextField(String.valueOf(socketService.getServerPort()), 4); // Reduced width
        connectBtn = new JButton("🔌 Connect");
        
        setupComponents(title, icon);
        setupLayout();
        startTimers();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Modern gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(79, 70, 229), // Indigo
            getWidth(), 0, new Color(147, 51, 234) // Purple
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Add subtle overlay
        g2d.setColor(new Color(255, 255, 255, 5));
        for (int i = 0; i < getWidth(); i += 40) {
            for (int j = 0; j < getHeight(); j += 40) {
                g2d.fillOval(i, j, 1, 1);
            }
        }
    }

    private void setupComponents(String title, ImageIcon icon) {
        styleComponents(title, icon);
    }

    private void styleComponents(String title, ImageIcon icon) {
        // Style welcome label
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Reduced size
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Style date/time label
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateTimeLabel.setForeground(new Color(226, 232, 240));
        
        // Style connection status
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        connectionStatusLabel.setForeground(new Color(248, 113, 113));
        
        // Style input fields
        styleTextField(hostField);
        styleTextField(portField);
        
        // Style connect button with fixed approach
        styleButton(connectBtn, new Color(34, 197, 94), new Color(22, 163, 74));
        connectBtn.setPreferredSize(new Dimension(80, 24)); // Smaller button
        
        // Add action listeners
        hostField.addActionListener(e -> updateConnectionSettings());
        portField.addActionListener(e -> updateConnectionSettings());
        connectBtn.addActionListener(e -> handleConnect());
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        field.setBackground(new Color(255, 255, 255, 200));
        field.setForeground(new Color(30, 41, 59));
        field.setBorder(BorderFactory.createCompoundBorder(
            new ModernFieldBorder(),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        field.setOpaque(false);
    }

    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true); // Make sure it's opaque
        button.setBorderPainted(false);
        button.setContentAreaFilled(true); // Ensure content area is filled
        
        // Use a simple rounded border instead of the problematic custom border
        button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        // Custom painting for rounded corners without interfering with text
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            protected void paintButtonPressed(Graphics g, AbstractButton b) {
                paintRoundedBackground(g, b);
            }
            
            @Override
            public void paint(Graphics g, JComponent c) {
                paintRoundedBackground(g, (AbstractButton) c);
                super.paint(g, c); // Let default UI paint the text
            }
            
            private void paintRoundedBackground(Graphics g, AbstractButton button) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Paint rounded background
                g2.setColor(button.getBackground());
                g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 6, 6);
                g2.dispose();
            }
        });
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private void setupLayout() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Left section - Logo and Title
        JPanel leftSection = createLeftSection();
        
        // Center section - Welcome and Connection
        JPanel centerSection = createCenterSection();
        
        // Right section - Time and Actions
        JPanel rightSection = createRightSection();
        
        mainContainer.add(leftSection, BorderLayout.WEST);
        mainContainer.add(centerSection, BorderLayout.CENTER);
        mainContainer.add(rightSection, BorderLayout.EAST);
        
        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createLeftSection() {
        JPanel section = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        section.setOpaque(false);
        section.setPreferredSize(new Dimension(280, 0));
        
        // Icon
        ImageIcon icon = loadIcon("images/cash-machine.png");
        if (icon != null) {
            JLabel iconLabel = new JLabel(new ImageIcon(
                icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)
            ));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            section.add(iconLabel);
        }
        
        // Title area
        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My POS System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Professional Point of Sale");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(226, 232, 240));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titleArea.add(titleLabel);
        titleArea.add(Box.createVerticalStrut(2));
        titleArea.add(subtitleLabel);
        
        section.add(titleArea);
        
        return section;
    }

    private JPanel createCenterSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        // Welcome message
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(welcomeLabel);
        section.add(Box.createVerticalStrut(8));
        
        // Connection controls in a compact layout
        JPanel connectionPanel = createCompactConnectionPanel();
        connectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(connectionPanel);
        
        return section;
    }

    private JPanel createCompactConnectionPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glass effect background
                g2d.setColor(new Color(255, 255, 255, 120));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                // Subtle border
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
            }
        };
        
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        
        // Add components in compact arrangement
        panel.add(connectionStatusLabel);
        panel.add(createMiniSeparator());
        
        // Host field with label
        JLabel hostLbl = new JLabel("Host:");
        hostLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        hostLbl.setForeground(new Color(51, 65, 85));
        panel.add(hostLbl);
        panel.add(hostField);
        
        // Port field with label
        JLabel portLbl = new JLabel("Port:");
        portLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        portLbl.setForeground(new Color(51, 65, 85));
        panel.add(portLbl);
        panel.add(portField);
        
        panel.add(connectBtn);
        
        return panel;
    }

    private JPanel createRightSection() {
        JPanel section = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        section.setOpaque(false);
        section.setPreferredSize(new Dimension(200, 0));
        
        // Action button - Style it the same way as connect button
        JButton journalBtn = new JButton("📊 Journal");
        styleButton(journalBtn, new Color(99, 102, 241), new Color(79, 70, 229));
        journalBtn.setPreferredSize(new Dimension(80, 24));
        journalBtn.addActionListener(e -> openJournalDialog());
        
        // Time display
        JPanel timePanel = createCompactTimePanel();
        
        section.add(journalBtn);
        section.add(Box.createHorizontalStrut(10));
        section.add(timePanel);
        
        return section;
    }

    private JPanel createCompactTimePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
            }
        };
        
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        JLabel clockIcon = new JLabel("🕐");
        clockIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(clockIcon);
        panel.add(dateTimeLabel);
        
        return panel;
    }

    private Component createMiniSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 16));
        sep.setForeground(new Color(203, 213, 225));
        return sep;
    }

    private void handleConnect() {
        updateConnectionSettings();
        SocketService socketService = SocketService.getInstance();
        
        connectBtn.setText("🔄");
        connectBtn.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return socketService.connect();
            }
            
            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        connectionStatusLabel.setText("🟢 Connected");
                        connectionStatusLabel.setForeground(new Color(34, 197, 94));
                        showToast("Connected!", false);
                    } else {
                        connectionStatusLabel.setText("🔴 Failed");
                        connectionStatusLabel.setForeground(new Color(248, 113, 113));
                        showToast("Failed!", true);
                    }
                } catch (Exception e) {
                    connectionStatusLabel.setText("🔴 Error");
                    connectionStatusLabel.setForeground(new Color(248, 113, 113));
                    showToast("Error!", true);
                }
                
                connectBtn.setText("🔌 Connect");
                connectBtn.setEnabled(true);
            }
        };
        
        worker.execute();
    }

    private void showToast(String message, boolean isError) {
        JWindow toast = new JWindow();
        toast.setAlwaysOnTop(true);
        
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bg = isError ? new Color(239, 68, 68) : new Color(34, 197, 94);
                g2d.setColor(bg);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
            }
        };
        
        content.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        content.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        msgLabel.setForeground(Color.WHITE);
        content.add(msgLabel);
        
        toast.setContentPane(content);
        toast.pack();
        
        Point loc = getLocationOnScreen();
        toast.setLocation(
            loc.x + getWidth() / 2 - toast.getWidth() / 2,
            loc.y + getHeight() + 5
        );
        
        toast.setVisible(true);
        
        Timer hideTimer = new Timer(2000, e -> toast.dispose());
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    private void startTimers() {
        Timer clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();
        updateDateTime();
        
        Timer welcomeTimer = new Timer(4000, e -> cycleWelcomeMessage());
        welcomeTimer.start();
    }

    private void updateConnectionSettings() {
        SocketService socketService = SocketService.getInstance();
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            
            if (!host.isEmpty()) {
                socketService.setServerHost(host);
            }
            socketService.setServerPort(port);
        } catch (NumberFormatException e) {
            showToast("Invalid port!", true);
            portField.setText(String.valueOf(socketService.getServerPort()));
        }
    }

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd\nhh:mm a");
        String[] parts = sdf.format(new Date()).split("\n");
        dateTimeLabel.setText("<html><div style='text-align: center; font-size: 9px;'>" 
                             + parts[0] + "<br>" + parts[1] + "</div></html>");
    }

    private void cycleWelcomeMessage() {
        welcomeIndex = (welcomeIndex + 1) % welcomeMessages.length;
        
        Timer fadeOut = new Timer(40, null);
        fadeOut.addActionListener(e -> {
            Color current = welcomeLabel.getForeground();
            int alpha = Math.max(0, current.getAlpha() - 25);
            welcomeLabel.setForeground(new Color(255, 255, 255, alpha));
            
            if (alpha == 0) {
                fadeOut.stop();
                welcomeLabel.setText(welcomeMessages[welcomeIndex]);
                
                Timer fadeIn = new Timer(40, null);
                fadeIn.addActionListener(e2 -> {
                    Color curr = welcomeLabel.getForeground();
                    int newAlpha = Math.min(255, curr.getAlpha() + 25);
                    welcomeLabel.setForeground(new Color(255, 255, 255, newAlpha));
                    
                    if (newAlpha == 255) {
                        ((Timer) e2.getSource()).stop();
                    }
                });
                fadeIn.start();
            }
        });
        fadeOut.start();
    }

    private static ImageIcon loadIcon(String path) {
        URL imgURL = TitleBarPanel.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private void openJournalDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "📊 Transaction Journal", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JournalPanel(), BorderLayout.CENTER);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Keep only the field border - remove the problematic ModernButtonBorder
    private static class ModernFieldBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(203, 213, 225, 150));
            g2d.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, 4, 4));
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}
