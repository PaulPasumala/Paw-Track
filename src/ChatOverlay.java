import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;
import javax.imageio.ImageIO;

public class ChatOverlay extends JPanel {

    private enum ThemeType { DARK, LIGHT }
    private ThemeType currentThemeType = ThemeType.DARK;

    private class Theme {
        Color bg, headerBg, textMain, textSub, inputBg, hover, selfBubble, otherBubble, accent, selfText, otherText;
    }

    private final Theme darkTheme = new Theme();
    private final Theme lightTheme = new Theme();
    private Theme currentTheme;

    private JButton fabButton;
    private JPanel chatWindow;
    private JPanel userListPanel;
    private JPanel conversationPanel;

    // Fixed Scroll Variable
    private JScrollPane msgScrollPane;

    private CardLayout cardLayout;
    private JTextField inputField;
    private JLabel chatHeaderTitle;
    private JButton themeToggleBtn;
    private JButton backBtn;

    private Image chatIconImage;
    private static final String ICON_FILENAME = "Gemini_Generated_Image_55spsm55spsm55sp.png";

    private boolean isExpanded = false;
    private String currentUser;
    private String currentChatPartner = null;
    private Timer poller;

    private static final String AI_USERNAME = "paw";
    private static final String AI_DISPLAY_NAME = "Paw Assistant";

    private Map<String, List<String>> aiHistory = new HashMap<>();

    public ChatOverlay(String currentUser) {
        this.currentUser = currentUser;

        loadCustomIcon();
        initThemes();
        currentTheme = darkTheme;

        setLayout(null);
        setOpaque(false);

        createChatWindow();
        createFabButton();

        poller = new Timer(3000, e -> {
            if (isExpanded && currentChatPartner != null && !currentChatPartner.equals(AI_USERNAME)) {
                loadRealMessages(currentChatPartner);
            }
        });
        poller.start();
    }

    private void loadCustomIcon() {
        try {
            URL url = getClass().getResource("/image/" + ICON_FILENAME);
            if (url != null) chatIconImage = ImageIO.read(url);
        } catch (Exception e) { }
    }

    private void initThemes() {
        darkTheme.bg = new Color(24, 25, 26);
        darkTheme.headerBg = new Color(36, 37, 38);
        darkTheme.textMain = new Color(228, 230, 235);
        darkTheme.textSub = new Color(176, 179, 184);
        darkTheme.inputBg = new Color(58, 59, 60);
        darkTheme.hover = new Color(58, 59, 60);
        darkTheme.selfBubble = new Color(0, 132, 255);
        darkTheme.otherBubble = new Color(62, 64, 66);
        darkTheme.accent = new Color(45, 136, 255);
        darkTheme.selfText = Color.WHITE;
        darkTheme.otherText = new Color(228, 230, 235);

        lightTheme.bg = new Color(255, 255, 255);
        lightTheme.headerBg = new Color(255, 255, 255);
        lightTheme.textMain = new Color(5, 5, 5);
        lightTheme.textSub = new Color(101, 103, 107);
        lightTheme.inputBg = new Color(240, 242, 245);
        lightTheme.hover = new Color(242, 242, 242);
        lightTheme.selfBubble = new Color(0, 132, 255);
        lightTheme.otherBubble = new Color(228, 230, 235);
        lightTheme.accent = new Color(0, 132, 255);
        lightTheme.selfText = Color.WHITE;
        lightTheme.otherText = Color.BLACK;
    }

    private void createFabButton() {
        fabButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight();
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(2, 4, w - 4, h - 4);
                if (chatIconImage != null) {
                    g2.setClip(new Ellipse2D.Float(0, 0, w - 4, h - 4));
                    g2.drawImage(chatIconImage, 0, 0, w - 4, h - 4, null);
                } else {
                    g2.setColor(currentTheme.accent);
                    g2.fillOval(0, 0, w - 4, h - 4);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(15, 18, 40, 30, 10, 10);
                    int[] xPoints = {25, 25, 15};
                    int[] yPoints = {48, 58, 52};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }
                g2.dispose();
            }
        };
        fabButton.setBorderPainted(false);
        fabButton.setContentAreaFilled(false);
        fabButton.setFocusPainted(false);
        fabButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fabButton.addActionListener(e -> toggleChat());
        add(fabButton);
    }

    private void createChatWindow() {
        chatWindow = new JPanel(new BorderLayout());
        chatWindow.setOpaque(false);
        chatWindow.setVisible(false);

        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentTheme.bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0,0,0,50));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        chatWindow.add(mainContainer, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 15, 10, 15));

        JPanel titleArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleArea.setOpaque(false);

        backBtn = new JButton("❮");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        backBtn.setForeground(currentTheme.textMain);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setVisible(false);
        backBtn.addActionListener(e -> showUserList());

        chatHeaderTitle = new JLabel("Chats");
        chatHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        chatHeaderTitle.setForeground(currentTheme.textMain);

        titleArea.add(backBtn);
        titleArea.add(chatHeaderTitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actions.setOpaque(false);

        themeToggleBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentTheme.textMain);
                int w = getWidth(); int h = getHeight();
                int cx = w/2; int cy = h/2;
                if (currentThemeType == ThemeType.DARK) {
                    g2.fillOval(cx-4, cy-4, 8, 8);
                    for(int i=0; i<360; i+=45) {
                        double rad = Math.toRadians(i);
                        int x1 = (int)(cx + Math.cos(rad)*6);
                        int y1 = (int)(cy + Math.sin(rad)*6);
                        int x2 = (int)(cx + Math.cos(rad)*9);
                        int y2 = (int)(cy + Math.sin(rad)*9);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    Area moon = new Area(new Ellipse2D.Double(cx-7, cy-7, 14, 14));
                    Area shadow = new Area(new Ellipse2D.Double(cx-3, cy-7, 14, 14));
                    moon.subtract(shadow);
                    g2.fill(moon);
                }
                g2.dispose();
            }
        };
        themeToggleBtn.setPreferredSize(new Dimension(30, 30));
        setupIconBtn(themeToggleBtn);
        themeToggleBtn.addActionListener(e -> toggleTheme());

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        setupIconBtn(closeBtn);
        closeBtn.addActionListener(e -> toggleChat());

        actions.add(themeToggleBtn);
        actions.add(closeBtn);

        header.add(titleArea, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        mainContainer.add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        JPanel contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        JPanel userListView = new JPanel(new BorderLayout());
        userListView.setOpaque(false);

        JButton newChatBtn = new JButton("+ Start New Chat");
        newChatBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newChatBtn.setForeground(Color.WHITE);
        newChatBtn.setBackground(currentTheme.accent);
        newChatBtn.setFocusPainted(false);
        newChatBtn.setBorder(new EmptyBorder(10, 0, 10, 0));
        newChatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newChatBtn.addActionListener(e -> promptNewChat());

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(0, 15, 10, 15));
        topWrapper.add(newChatBtn, BorderLayout.CENTER);

        userListView.add(topWrapper, BorderLayout.NORTH);

        userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setOpaque(false);

        JScrollPane userScroll = new JScrollPane(userListPanel);
        userScroll.setBorder(null);
        userScroll.setOpaque(false);
        userScroll.getViewport().setOpaque(false);
        userListView.add(userScroll, BorderLayout.CENTER);

        contentCards.add(userListView, "USERS");

        JPanel chatView = new JPanel(new BorderLayout());
        chatView.setOpaque(false);

        conversationPanel = new JPanel();
        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        conversationPanel.setOpaque(false);
        conversationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel conversationWrapper = new JPanel(new BorderLayout());
        conversationWrapper.setOpaque(false);
        conversationWrapper.add(conversationPanel, BorderLayout.NORTH);

        msgScrollPane = new JScrollPane(conversationWrapper);
        msgScrollPane.setBorder(null);
        msgScrollPane.setOpaque(false);
        msgScrollPane.getViewport().setOpaque(false);
        msgScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(new EmptyBorder(10, 15, 10, 15));
        inputField.setOpaque(false);
        inputField.setForeground(currentTheme.textMain);
        inputField.setCaretColor(currentTheme.textMain);

        JPanel inputPill = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentTheme.inputBg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 20,20);
            }
        };
        inputPill.setOpaque(false);
        inputPill.add(inputField);

        JButton sendBtn = new JButton("➤");
        sendBtn.setForeground(currentTheme.accent);
        sendBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        sendBtn.setBorderPainted(false);
        sendBtn.setContentAreaFilled(false);
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        inputPanel.add(inputPill, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        chatView.add(msgScrollPane, BorderLayout.CENTER);
        chatView.add(inputPanel, BorderLayout.SOUTH);
        contentCards.add(chatView, "CHAT");

        mainContainer.add(contentCards, BorderLayout.CENTER);
        add(chatWindow);
    }

    private void toggleTheme() {
        if (currentThemeType == ThemeType.DARK) {
            currentThemeType = ThemeType.LIGHT;
            currentTheme = lightTheme;
        } else {
            currentThemeType = ThemeType.DARK;
            currentTheme = darkTheme;
        }
        applyThemeColors();
        repaint();
    }

    private void applyThemeColors() {
        chatHeaderTitle.setForeground(currentTheme.textMain);
        backBtn.setForeground(currentTheme.textMain);
        inputField.setForeground(currentTheme.textMain);
        inputField.setCaretColor(currentTheme.textMain);
    }

    public void updateComponentPositions() {
        int w = getWidth(); int h = getHeight();
        if (w == 0 || h == 0) return;
        int fabSize = 65;
        int padding = 25;
        fabButton.setBounds(w - fabSize - padding, h - fabSize - padding, fabSize, fabSize);
        int winW = 360;
        int winH = 520;
        chatWindow.setBounds(w - winW - padding, h - fabSize - padding - winH - 15, winW, winH);
    }

    private void toggleChat() {
        isExpanded = !isExpanded;
        chatWindow.setVisible(isExpanded);
        if (isExpanded) {
            loadRecentChats();
            showUserList();
        }
    }

    private void showUserList() {
        currentChatPartner = null;
        chatHeaderTitle.setText("Chats");
        backBtn.setVisible(false);
        cardLayout.show((Container) ((JPanel)chatWindow.getComponent(0)).getComponent(1), "USERS");
    }

    private void openConversation(String partnerName) {
        currentChatPartner = partnerName;
        chatHeaderTitle.setText(partnerName.equals(AI_USERNAME) ? AI_DISPLAY_NAME : partnerName);
        backBtn.setVisible(true);
        cardLayout.show((Container) ((JPanel)chatWindow.getComponent(0)).getComponent(1), "CHAT");

        conversationPanel.removeAll();
        conversationPanel.revalidate();
        conversationPanel.repaint();

        if (partnerName.equals(AI_USERNAME)) {
            loadAIMessages();
        } else {
            loadRealMessages(partnerName);
        }
    }

    private void setupIconBtn(JButton b) {
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(currentTheme.textMain);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- FIX: VALIDATE USERNAME EXISTENCE ---
    private void promptNewChat() {
        String partner = JOptionPane.showInputDialog(this, "Enter Username to Chat:", "New Chat", JOptionPane.PLAIN_MESSAGE);
        if (partner != null && !partner.trim().isEmpty()) {
            partner = partner.trim();
            if (partner.equalsIgnoreCase(currentUser)) {
                JOptionPane.showMessageDialog(this, "You cannot chat with yourself.");
                return;
            }

            // Check Database if user exists
            if (checkUserExists(partner)) {
                openConversation(partner);
            } else {
                JOptionPane.showMessageDialog(this, "User '" + partner + "' not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean checkUserExists(String username) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM user_accounts WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadRecentChats() {
        userListPanel.removeAll();
        userListPanel.add(createUserRow(AI_USERNAME, AI_DISPLAY_NAME));

        String sql = "SELECT DISTINCT CASE " +
                "WHEN sender = ? THEN receiver " +
                "ELSE sender END AS partner " +
                "FROM messages " +
                "WHERE sender = ? OR receiver = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currentUser);
            stmt.setString(2, currentUser);
            stmt.setString(3, currentUser);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String partner = rs.getString("partner");
                if (partner == null || partner.trim().equals("0") || partner.trim().equals("0 0")) continue;
                if (partner.equalsIgnoreCase(currentUser)) continue;

                userListPanel.add(createUserRow(partner, partner));
            }
        } catch (Exception e) { e.printStackTrace(); }

        userListPanel.revalidate();
        userListPanel.repaint();
    }

    private void loadRealMessages(String partner) {
        conversationPanel.removeAll();

        String sql = "SELECT sender, message FROM messages " +
                "WHERE (sender = ? AND receiver = ?) " +
                "OR (sender = ? AND receiver = ?) " +
                "ORDER BY sent_at ASC";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currentUser);
            stmt.setString(2, partner);
            stmt.setString(3, partner);
            stmt.setString(4, currentUser);

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                String sender = rs.getString("sender");
                String msg = rs.getString("message");

                boolean isMe = false;
                if (sender != null && currentUser != null) {
                    isMe = sender.trim().equalsIgnoreCase(currentUser.trim());
                }

                addMessageBubble(msg, isMe);
            }
        } catch (Exception e) { e.printStackTrace(); }

        scrollToBottom();
    }

    private void loadAIMessages() {
        List<String> history = aiHistory.getOrDefault(currentUser, new ArrayList<>());
        if (history.isEmpty()) {
            history.add("AI:Hello! I am Paw, your AI Assistant. How can I help you today? 🐾");
            aiHistory.put(currentUser, history);
        }
        for (String record : history) {
            String[] parts = record.split(":", 2);
            boolean isMe = parts[0].equals("ME");
            addMessageBubble(parts[1], isMe);
        }
        scrollToBottom();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || currentChatPartner == null) return;

        inputField.setText("");

        if (currentChatPartner.equals(AI_USERNAME)) {
            List<String> history = aiHistory.getOrDefault(currentUser, new ArrayList<>());
            history.add("ME:" + text);
            aiHistory.put(currentUser, history);
            addMessageBubble(text, true);
            scrollToBottom();

            Timer delay = new Timer(500, e1 -> {
                PawTrackAIService.askAI(text).thenAccept(response -> {
                    SwingUtilities.invokeLater(() -> {
                        history.add("AI:" + response);
                        addMessageBubble(response, false);
                        scrollToBottom();
                    });
                });
            });
            delay.setRepeats(false);
            delay.start();
            return;
        }

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO messages (sender, receiver, message) VALUES (?, ?, ?)")) {

            stmt.setString(1, currentUser);
            stmt.setString(2, currentChatPartner);
            stmt.setString(3, text);
            stmt.executeUpdate();

            loadRealMessages(currentChatPartner);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage());
        }
    }

    private JPanel createUserRow(String username, String displayName) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 15, 8, 15));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel avatar = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (username.equals(AI_USERNAME)) {
                    GradientPaint aiGrad = new GradientPaint(0, 0, new Color(13, 110, 253), 40, 40, new Color(220, 53, 69));
                    g2.setPaint(aiGrad);
                    g2.fillOval(0,0,40,40);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    g2.drawString("AI", 12, 25);
                } else {
                    int hash = username.hashCode();
                    // --- FIX: Use bitwise AND to ensure 0-255 range and avoid negative overflow ---
                    int r = (hash * 123) & 0xFF;
                    int gVal = (hash * 321) & 0xFF;
                    int b = (hash * 555) & 0xFF;
                    g2.setColor(new Color(r, gVal, b));

                    g2.fillOval(0,0,40,40);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    String initial = username.isEmpty() ? "?" : username.substring(0,1).toUpperCase();
                    int w = g2.getFontMetrics().stringWidth(initial);
                    g2.drawString(initial, 20 - w/2, 26);
                }
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(40, 40));

        JLabel nameLbl = new JLabel(displayName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLbl.setForeground(currentTheme.textMain);

        row.add(avatar, BorderLayout.WEST);
        row.add(nameLbl, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { row.setOpaque(true); row.setBackground(currentTheme.hover); row.repaint(); }
            public void mouseExited(MouseEvent e) { row.setOpaque(false); row.repaint(); }
            public void mouseClicked(MouseEvent e) { openConversation(username); }
        });

        return row;
    }

    private void addMessageBubble(String text, boolean isMe) {
        JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JTextArea bubble = new JTextArea(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 18, 18);
                super.paintComponent(g);
            }
        };
        bubble.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bubble.setForeground(isMe ? currentTheme.selfText : currentTheme.otherText);
        bubble.setEditable(false);
        bubble.setLineWrap(true);
        bubble.setWrapStyleWord(true);
        bubble.setOpaque(false);
        bubble.setBackground(isMe ? currentTheme.selfBubble : currentTheme.otherBubble);
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        int maxWidth = 260;
        bubble.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
        int prefH = bubble.getPreferredSize().height;
        bubble.setPreferredSize(new Dimension(Math.min(maxWidth, bubble.getPreferredSize().width + 24), prefH));

        wrapper.add(bubble);
        conversationPanel.add(wrapper);
        conversationPanel.add(Box.createVerticalStrut(5));
    }

    private void scrollToBottom() {
        conversationPanel.revalidate();
        conversationPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            if(msgScrollPane != null) {
                JScrollBar vertical = msgScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chat Fix");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 600);
            frame.getContentPane().setBackground(Color.DARK_GRAY);
            ChatOverlay overlay = new ChatOverlay("Admin");
            frame.add(overlay);
            frame.setVisible(true);
            overlay.toggleChat();
        });
    }
}