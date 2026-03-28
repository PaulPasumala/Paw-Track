import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PawTrackLogin extends JFrame {

    // --- 1. NETWORK CONFIGURATION ---
    private static final String[] KNOWN_HOSTS = {
            "192.168.100.94",
            "10.152.21.60",
            "26.115.66.121"
    };

    private static final String UPDATE_URL_TEMPLATE = "http://%s:8081/PawTrack/";
    private static final String APP_JAR_NAME = "Paw-Track.jar";
    private static final String VERSION_FILE = "version.txt";

    // --- UI COLORS ---

    private static final Color COLOR_TEXT = new Color(30, 41, 59);
    private static final Color COLOR_PLACEHOLDER = new Color(156, 163, 175);
    private static final Color COLOR_ACCENT = new Color(99, 102, 241);
    private static final Color COLOR_ACCENT2 = new Color(139, 92, 246);
    private static final String BACKGROUND_IMAGE_PATH = "image/background.png";

    private ModernGradientButton loginButton;
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    public PawTrackLogin() {
        setTitle("PawTrack Login");

        try {
            URL iconUrl = getClass().getResource("/image/Logo.png");
            if (iconUrl != null) setIconImage(ImageIO.read(iconUrl));
        } catch (Exception e) {
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setResizable(true);

        BackgroundPanel backgroundPanel = new BackgroundPanel(BACKGROUND_IMAGE_PATH);
        backgroundPanel.setLayout(new GridLayout(1, 2, 40, 0));
        backgroundPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        setContentPane(backgroundPanel);

        backgroundPanel.add(createLoginPanel());
        backgroundPanel.add(createInfoPanel());

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        SwingUtilities.invokeLater(() -> {
            if (loginButton != null) getRootPane().setDefaultButton(loginButton);
        });

        ensureVersionFile();
        attemptAutoDiscovery();
    }

    private void ensureVersionFile() {
        File vFile = new File(VERSION_FILE);
        if (!vFile.exists()) {
            try {
                Files.writeString(vFile.toPath(), "1.0");
            } catch (IOException e) {
            }
        }
    }

    private void attemptAutoDiscovery() {
        new Thread(() -> {
            System.out.println("🤖 Auto-Discovery: Checking network role...");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pawpatrol_db?useSSL=false&connectTimeout=2000", "root", "")) {
                System.out.println("✅ Local Database Found! I am the HOST.");
                isConnected.set(true);
                DatabaseDiscovery.startServerMode();
                checkForUpdates("localhost");
            } catch (Exception e) {
                System.out.println("❌ No Local Database. I am a CLIENT.");
                scanKnownHosts();
                startBroadcastListener();
            }
        }).start();
    }

    private void scanKnownHosts() {
        new Thread(() -> {
            for (String ip : KNOWN_HOSTS) {
                if (isConnected.get()) return;
                if (isServerReachable(ip)) {
                    connectToHost(ip);
                    return;
                }
            }
        }).start();
    }

    private void startBroadcastListener() {
        DatabaseDiscovery.findServer(new DatabaseDiscovery.DiscoveryListener() {
            @Override
            public void onServerFound(String ipAddress) {
                if (!isConnected.get()) {
                    connectToHost(ipAddress);
                }
            }

            @Override
            public void onDiscoveryFailed() {
                if (!isConnected.get()) {
                    SwingUtilities.invokeLater(() -> {
                        String manualIP = JOptionPane.showInputDialog(PawTrackLogin.this,
                                "Could not find Host automatically.\n\nEnter Host IP:",
                                "Manual Connect", JOptionPane.QUESTION_MESSAGE);
                        if (manualIP != null && !manualIP.trim().isEmpty()) {
                            connectToHost(manualIP.trim());
                        }
                    });
                }
            }
        });
    }

    private boolean isServerReachable(String ip) {
        try {
            URL url = new URL(String.format(UPDATE_URL_TEMPLATE, ip) + "version.txt");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1500);
            return (conn.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

    private void connectToHost(String ipAddress) {
        if (isConnected.getAndSet(true)) return;
        DBConnector.setServerIP(ipAddress);
        checkForUpdates(ipAddress);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(PawTrackLogin.this, "✅ Connected to Host: " + ipAddress);
            performBackgroundSync();
        });
    }

    private void checkForUpdates(String serverIp) {
        new Thread(() -> {
            String baseUrl = String.format(UPDATE_URL_TEMPLATE, serverIp);
            String versionUrl = baseUrl + "version.txt";
            String jarUrl = baseUrl + APP_JAR_NAME;

            try {
                String onlineVersion = readStringFromURL(versionUrl + "?t=" + System.currentTimeMillis()).trim();
                String localVersion = Files.readString(Paths.get(VERSION_FILE)).trim();

                if (!onlineVersion.isEmpty() && !onlineVersion.equals(localVersion)) {
                    SwingUtilities.invokeLater(() -> {
                        int choice = JOptionPane.showConfirmDialog(this,
                                "New Update Available (" + onlineVersion + ")!\nDownload and Restart now?",
                                "Update Found", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            performSelfUpdate(jarUrl, onlineVersion);
                        }
                    });
                }
            } catch (Exception e) {
            }
        }).start();
    }

    // --- [UPDATED] SILENT UPDATE LOGIC (No CMD Window) ---
    private void performSelfUpdate(String downloadUrl, String newVersion) {
        JDialog dialog = new JDialog(this, "Updating...", true);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);
        dialog.add(new JLabel("Downloading update...", SwingConstants.CENTER));

        new Thread(() -> {
            try {
                URL url = new URL(downloadUrl);
                File tempUpdate = new File("update.tmp");

                // 1. Download the new JAR
                try (InputStream in = url.openStream()) {
                    Files.copy(in, tempUpdate.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                // 2. Create the Batch Script (The Worker)
                File script = new File("updater.bat");
                FileWriter fw = new FileWriter(script);
                fw.write("@echo off\n");
                // Wait 2 seconds for app to close
                fw.write("timeout /t 2 /nobreak > NUL\n");
                // Delete old JAR
                fw.write("del \"" + APP_JAR_NAME + "\"\n");
                // Move new JAR
                fw.write("move /y update.tmp \"" + APP_JAR_NAME + "\"\n");
                // Update version file
                fw.write("echo " + newVersion + " > " + VERSION_FILE + "\n");
                // Launch the new JAR (using javaw to avoid console)
                fw.write("start \"\" \"javaw\" -jar \"" + APP_JAR_NAME + "\"\n");
                // Clean up VBS runner
                fw.write("del \"runner.vbs\"\n");
                // Delete self
                fw.write("del \"%~f0\"\n");
                fw.close();

                // 3. Create VBScript (The Silent Launcher)
                // This script runs the batch file with '0' as the window style (Hidden)
                File vbs = new File("runner.vbs");
                FileWriter vbsWriter = new FileWriter(vbs);
                vbsWriter.write("Set WshShell = CreateObject(\"WScript.Shell\")\n");
                vbsWriter.write("WshShell.Run chr(34) & \"updater.bat\" & chr(34), 0\n");
                vbsWriter.write("Set WshShell = Nothing\n");
                vbsWriter.close();

                // 4. Run VBScript and Exit
                Runtime.getRuntime().exec("wscript runner.vbs");
                System.exit(0);

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Update Failed: " + e.getMessage());
                });
            }
        }).start();
        dialog.setVisible(true);
    }

    private String readStringFromURL(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        try (InputStream in = conn.getInputStream(); Scanner scanner = new Scanner(in)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private void performBackgroundSync() {
        new Thread(() -> System.out.println("🔄 Syncing data...")).start();
    }

    private JPanel createLoginPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        GlassmorphicPanel formPanel = new GlassmorphicPanel(30);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(520, 680));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        CircularImageComponent logoLabel = new CircularImageComponent("image/Logo.png");
        logoLabel.setPreferredSize(new Dimension(120, 120));
        gbc.gridy = 0;
        gbc.insets = new Insets(28, 40, 18, 40);
        formPanel.add(logoLabel, gbc);

        JLabel welcomeLabel = new JLabel("Welcome Back!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 40, 22, 40);
        formPanel.add(welcomeLabel, gbc);

        gbc.insets = new Insets(8, 40, 8, 40);
        JLabel userLabel = new JLabel("USERNAME:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 2;
        formPanel.add(userLabel, gbc);

        RoundedTextFieldCustom usernameField = new RoundedTextFieldCustom(20);
        addPlaceholderStyle(usernameField, "Enter username");
        gbc.gridy = 3;
        formPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("PASSWORD:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 4;
        formPanel.add(passLabel, gbc);

        JPanel passwordPanel = new JPanel(new BorderLayout(6, 0));
        passwordPanel.setOpaque(false);
        RoundedPasswordFieldCustom passwordField = new RoundedPasswordFieldCustom(20);
        addPlaceholderStyle(passwordField, "Enter your password");
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(createEyeToggleButton(passwordField), BorderLayout.EAST);
        gbc.gridy = 5;
        formPanel.add(passwordPanel, gbc);

        loginButton = new ModernGradientButton("LOGIN");
        loginButton.setPreferredSize(new Dimension(220, 44));
        loginButton.addActionListener(e -> performLogin(trimPlaceholder(usernameField), trimPasswordPlaceholder(passwordField)));
        gbc.gridy = 6;
        gbc.insets = new Insets(25, 40, 15, 40);
        formPanel.add(loginButton, gbc);

        JPanel linksPanel = new JPanel(new BorderLayout(10, 0));
        linksPanel.setOpaque(false);
        JButton createAccountButton = createLinkButton("CREATE ACCOUNT");
        createAccountButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new CreateAccount().setVisible(true));
        });
        linksPanel.add(createAccountButton, BorderLayout.CENTER);
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 40, 25, 40);
        formPanel.add(linksPanel, gbc);

        leftPanel.add(formPanel);
        return leftPanel;
    }

    private void performLogin(String username, String password) {
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter credentials.");
            return;
        }

        String sql = "SELECT first_name, last_name, password, role, trusted_devices FROM user_accounts WHERE username = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (PasswordUtils.verify(password, rs.getString("password"))) {
                    String role = rs.getString("role");
                    String sessionToken = java.util.UUID.randomUUID().toString();

                    // --- 🔥 SYNC TRIGGER ---
                    // This pulls all Online Data to Local DB immediately on login
                    new Thread(() -> DataSync.syncUserData(username)).start();
                    // -----------------------

                    JOptionPane.showMessageDialog(this, "Welcome, " + rs.getString("first_name") + "!");
                    dispose();
                    if ("ADMIN".equalsIgnoreCase(role)) new DashboardAdmin(username, sessionToken).setVisible(true);
                    else new Dashboard(username).setVisible(true);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid Login", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Failover logic if Local DB fails (Try Cloud directly)
            try (Connection cloudConn = DBConnector.getOnlineConnection();
                 PreparedStatement cloudStmt = cloudConn.prepareStatement(sql)) {
                cloudStmt.setString(1, username);
                ResultSet rs = cloudStmt.executeQuery();
                if (rs.next() && PasswordUtils.verify(password, rs.getString("password"))) {
                    String role = rs.getString("role");
                    String sessionToken = java.util.UUID.randomUUID().toString();

                    // Sync even if we connected via failover
                    new Thread(() -> DataSync.syncUserData(username)).start();

                    JOptionPane.showMessageDialog(this, "Welcome, " + rs.getString("first_name") + "! (Cloud Mode)");
                    dispose();
                    if ("ADMIN".equalsIgnoreCase(role)) new DashboardAdmin(username, sessionToken).setVisible(true);
                    else new Dashboard(username).setVisible(true);
                    return;
                }
            } catch (Exception e2) {
                JOptionPane.showMessageDialog(this, "Login Failed. Check Internet Connection.");
            }
        }
    }

    // --- Helper UI Methods ---
    private JPanel createInfoPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        GlassmorphicPanel infoPanel = new GlassmorphicPanel(30);
        infoPanel.setLayout(new BorderLayout(15, 15));
        infoPanel.setPreferredSize(new Dimension(520, 680));
        infoPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        JLabel imageLabel = new RoundedImageComponent("image/background.png", 20);
        imageLabel.setPreferredSize(new Dimension(420, 300));
        infoPanel.add(imageLabel, BorderLayout.NORTH);
        JTextArea desc = new JTextArea("The Paw Track System is a digital platform designed to help abandoned and surrendered pets find new, loving homes.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        desc.setForeground(COLOR_TEXT);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setOpaque(false);
        desc.setEditable(false);
        infoPanel.add(desc, BorderLayout.CENTER);
        JPanel features = new JPanel(new GridLayout(3, 1, 8, 8));
        features.setOpaque(false);
        features.add(featureLabel("Fast adoptions", "Find pets quickly"));
        features.add(featureLabel("Vet & Care", "Schedule appointments"));
        features.add(featureLabel("Secure accounts", "Keep info safe"));
        infoPanel.add(features, BorderLayout.SOUTH);
        rightPanel.add(infoPanel);
        return rightPanel;
    }

    private JPanel featureLabel(String title, String sub) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(COLOR_ACCENT2);
        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(COLOR_TEXT);
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    private void addPlaceholderStyle(JTextField tf, String placeholder) {
        tf.setForeground(COLOR_PLACEHOLDER);
        tf.setText(placeholder);
        tf.putClientProperty("placeholder", placeholder);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (COLOR_PLACEHOLDER.equals(tf.getForeground()) && placeholder.equals(tf.getText())) {
                    tf.setText("");
                    tf.setForeground(COLOR_TEXT);
                    if (tf instanceof JPasswordField) ((JPasswordField) tf).setEchoChar('•');
                }
            }

            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setForeground(COLOR_PLACEHOLDER);
                    tf.setText(placeholder);
                    if (tf instanceof JPasswordField) ((JPasswordField) tf).setEchoChar((char) 0);
                }
            }
        });
    }

    private String trimPlaceholder(JTextField tf) {
        Object ph = tf.getClientProperty("placeholder");
        String text = tf.getText();
        if (ph != null && ph.equals(text)) return "";
        return text.trim();
    }

    private String trimPasswordPlaceholder(JPasswordField pf) {
        Object ph = pf.getClientProperty("placeholder");
        String text = new String(pf.getPassword());
        if (ph != null && ph.equals(text)) return "";
        return text;
    }

    private JButton createLinkButton(String text) {
        JButton b = new JButton(text);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setForeground(COLOR_ACCENT2);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createEyeToggleButton(JPasswordField pf) {
        JButton b = new JButton("👁");
        b.setFocusable(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            if (pf.getEchoChar() == 0) pf.setEchoChar('•');
            else pf.setEchoChar((char) 0);
            pf.requestFocusInWindow();
        });
        return b;
    }

    // UI Classes
    static class BackgroundPanel extends JPanel {
        private Image img;

        public BackgroundPanel(String path) {
            try {
                URL u = getClass().getResource("/" + path);
                if (u != null) img = ImageIO.read(u);
            } catch (Exception e) {
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            else {
                g.setColor(new Color(245, 246, 250));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    static class GlassmorphicPanel extends JPanel {
        int r;

        public GlassmorphicPanel(int r) {
            this.r = r;
            setOpaque(false);
            setBorder(new EmptyBorder(18, 18, 18, 18));
        }

        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(20, 25, 35, 30));
            g2.fillRoundRect(6, 8, w - 12, h - 12, r + 10, r + 10);
            GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 245), 0, h, new Color(245, 250, 255, 230));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w - 12, h - 12, r, r);
            g2.setColor(new Color(200, 200, 210, 120));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 12, h - 12, r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ModernGradientButton extends JButton {
        private boolean hover = false;

        public ModernGradientButton(String t) {
            super(t);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c1 = hover ? COLOR_ACCENT.brighter() : COLOR_ACCENT;
            Color c2 = hover ? COLOR_ACCENT2.brighter() : COLOR_ACCENT2;
            if (getModel().isPressed()) {
                c1 = c1.darker();
                c2 = c2.darker();
            }
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedTextFieldCustom extends JTextField {
        public RoundedTextFieldCustom(int c) {
            super(c);
            setOpaque(false);
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(249, 250, 252));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedPasswordFieldCustom extends JPasswordField {
        public RoundedPasswordFieldCustom(int c) {
            super(c);
            setOpaque(false);
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setEchoChar((char) 0);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(249, 250, 252));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class CircularImageComponent extends JLabel {
        Image i;

        public CircularImageComponent(String p) {
            try {
                URL u = getClass().getResource("/" + p);
                if (u != null) i = ImageIO.read(u);
            } catch (Exception e) {
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (i != null) {
                int size = Math.min(getWidth(), getHeight());
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape clip = new Ellipse2D.Float((getWidth() - size) / 2f, (getHeight() - size) / 2f, size, size);
                g2.setClip(clip);
                g2.drawImage(i, (getWidth() - size) / 2, (getHeight() - size) / 2, size, size, null);
                g2.setClip(null);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval((getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
                g2.dispose();
            }
        }
    }

    static class RoundedImageComponent extends JLabel {
        Image i;
        int r;

        public RoundedImageComponent(String p, int r) {
            this.r = r;
            try {
                URL u = getClass().getResource("/" + p);
                if (u != null) i = ImageIO.read(u);
            } catch (Exception e) {
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (i != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
                g2.drawImage(i, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        }
    }

    public static void main(String[] args) {
        // 1. Run the Silent Auto-Installer FIRST
        AutoInstaller.runSilentSetup();

        // 2. Then launch the App
        SwingUtilities.invokeLater(() -> new PawTrackLogin().setVisible(true));
    }
}