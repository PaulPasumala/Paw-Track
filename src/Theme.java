import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI; // [ADDED]
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class Theme {
    // --- PALETTE ---
    public static final Color NAV_BG = new Color(25, 42, 86);
    public static final Color MAIN_BG = new Color(248, 250, 252);
    public static final Color CARD_BG = Color.WHITE;

    public static final Color PRIMARY = new Color(79, 70, 229);
    public static final Color ACCENT = new Color(236, 72, 153);
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color WARNING = new Color(245, 158, 11);

    public static final Color TEXT_DARK = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    // --- FONTS ---
    public static final Font HEADER = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBHEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    // --- UTILITIES ---
    public static JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(15, 40, 15, 40)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(HEADER);
        t.setForeground(TEXT_DARK);

        JLabel s = new JLabel(subtitle);
        s.setFont(BODY);
        s.setForeground(TEXT_MUTED);

        textPanel.add(t);
        textPanel.add(s);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    // --- REUSABLE COMPONENTS ---

    public static class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0,10));
            g2.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, radius, radius);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class ModernButton extends JButton {
        private Color baseColor;
        private Color hoverColor;

        public ModernButton(String text) {
            this(text, PRIMARY);
        }

        public ModernButton(String text, Color bg) {
            super(text);
            this.baseColor = bg;
            this.hoverColor = bg.brighter();
            setFont(BODY_BOLD);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { repaint(); }
                public void mouseExited(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) g2.setColor(baseColor.darker());
            else if (getModel().isRollover()) g2.setColor(hoverColor);
            else g2.setColor(baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- [NEW] MODERN SCROLLBAR UI ---
    public static class ModernScrollBarUI extends BasicScrollBarUI {
        private final Dimension d = new Dimension();

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new JButton() {
                @Override public Dimension getPreferredSize() { return d; }
            };
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new JButton() {
                @Override public Dimension getPreferredSize() { return d; }
            };
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Optional: Draw a very light background if desired, currently transparent
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color color;
            if (isDragging) color = new Color(100, 100, 100, 200);
            else if (isThumbRollover()) color = new Color(100, 100, 100, 150);
            else color = new Color(180, 180, 180, 180);

            g2.setPaint(color);
            int padding = 4; // Makes the scrollbar look thinner
            g2.fillRoundRect(thumbBounds.x + padding, thumbBounds.y, thumbBounds.width - (padding * 2), thumbBounds.height, 10, 10);
            g2.dispose();
        }
    }
}