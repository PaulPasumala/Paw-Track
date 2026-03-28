// src/Adopt.java
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class Adopt extends JPanel {

    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color ACCENT_COLOR = new Color(236, 72, 153);
    private final Color TEXT_COLOR = new Color(30, 41, 59);
    private final Color TEXT_SECONDARY = new Color(100, 116, 139);

    private final Color GRADIENT_START = new Color(99, 102, 241);
    private final Color GRADIENT_END = new Color(168, 85, 247);

    // [ADDED]: Background gradient colors (blue to pink)
    private final Color BG_GRADIENT_START = new Color(147, 197, 253); // Light blue
    private final Color BG_GRADIENT_END = new Color(244, 114, 182);   // Pink

    public Adopt() {
        setLayout(new BorderLayout());
        add(createGradientHeader(), BorderLayout.NORTH);
        add(createScrollableContent(), BorderLayout.CENTER);
    }

    // [ADDED]: Override paintComponent to paint gradient background
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gp = new GradientPaint(0, 0, BG_GRADIENT_START, 0, getHeight(), BG_GRADIENT_END);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private JPanel createGradientHeader() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, getWidth(), 0, GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 40, 0, 40));

        JLabel titleLabel = new JLabel(" Adopt a Pet");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JScrollPane createScrollableContent() {
        JPanel contentPanel = new JPanel() {
            // [ADDED]: Override to make transparent for gradient visibility
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super to keep transparent
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // [UPDATED]: Make transparent
        contentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JTextArea subtitle = new JTextArea(
                "Welcome to the first step of finding your forever friend. We ensure a seamless matching process " +
                        "to connect wonderful pets with loving families. Follow the steps below to begin your adoption journey."
        );
        subtitle.setWrapStyleWord(true);
        subtitle.setLineWrap(true);
        subtitle.setEditable(false);
        subtitle.setOpaque(false);
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        subtitle.setForeground(Color.BLACK);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setMaximumSize(new Dimension(800, 60));

        contentPanel.add(subtitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        JPanel processPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        processPanel.setOpaque(false);
        processPanel.setMaximumSize(new Dimension(1000, 220));

        processPanel.add(createStepCard("1. Browse", "🔍", "View available pets profiles.", new Color(79, 70, 229)));
        processPanel.add(createStepCard("2. Apply", "📝", "Submit your digital application.", new Color(236, 72, 153)));
        processPanel.add(createStepCard("3. Meet", "🤝", "Schedule a visit with the pet.", new Color(16, 185, 129)));
        processPanel.add(createStepCard("4. Home", "🏠", "Finalize and bring them home!", new Color(245, 158, 11)));

        contentPanel.add(processPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        JPanel ctaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        ctaPanel.setOpaque(false);

        JButton browseBtn = createActionButton("Browse Gallery", new Color(79, 70, 229));
        browseBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof Dashboard dash) {
                dash.getPawManagementPanel().reloadPets();
                // [UPDATED]: Highlight sidebar and switch
                dash.activateSidebarByPanelName("PAW_PANEL");
            }
        });

        JButton registerBtn = createActionButton("Register a Pet", new Color(236, 72, 153));
        registerBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof Dashboard dash) {
                dash.handleAddNew();
            }
        });

        ctaPanel.add(browseBtn);
        ctaPanel.add(registerBtn);

        contentPanel.add(ctaPanel);
        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false); // [ADDED]: Make scrollpane transparent
        scrollPane.getViewport().setOpaque(false); // [ADDED]: Make viewport transparent

        scrollPane.getVerticalScrollBar().setUI(new Theme.ModernScrollBarUI());

        return scrollPane;
    }

    private JPanel createStepCard(String title, String icon, String desc, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(accent);
                g2d.fillRoundRect(0, 0, getWidth(), 6, 20, 20);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descLabel = new JTextArea(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setEditable(false);
        descLabel.setOpaque(false);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descLabel);

        return card;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(bg.darker());
                else if (getModel().isRollover()) g2.setColor(bg.brighter());
                else g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 50));
        return btn;
    }

    // [ADDED]: Custom Scrollbar Class
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private final Dimension d = new Dimension();
        @Override protected JButton createDecreaseButton(int orientation) { return new JButton() { @Override public Dimension getPreferredSize() { return d; } }; }
        @Override protected JButton createIncreaseButton(int orientation) { return new JButton() { @Override public Dimension getPreferredSize() { return d; } }; }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(245, 245, 245));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = isDragging ? new Color(130, 130, 130) : isThumbRollover() ? new Color(150, 150, 150) : new Color(180, 180, 180);
            g2.setPaint(color);
            int padding = 4;
            g2.fillRoundRect(thumbBounds.x + padding, thumbBounds.y, thumbBounds.width - (padding * 2), thumbBounds.height, 10, 10);
            g2.dispose();
        }
    }
}