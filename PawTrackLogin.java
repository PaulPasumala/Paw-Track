import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.net.URL;
import javax.swing.SwingWorker;
import javax.swing.JPasswordField;
import javax.imageio.ImageIO;

public class PawTrackLogin extends JFrame {

    private static final Color COLOR_BACKGROUND = new Color(30, 41, 59);
    private static final Color COLOR_PANEL = new Color(255, 255, 255, 250);
    private static final Color COLOR_BUTTON = new Color(99, 102, 241);
    private static final Color COLOR_BUTTON_HOVER = new Color(79, 70, 229);
    private static final Color COLOR_TEXT = new Color(30, 41, 59);
    private static final Color COLOR_PLACEHOLDER = new Color(156, 163, 175);
    private static final Color COLOR_ACCENT = new Color(139, 92, 246);

    private static final String BACKGROUND_IMAGE_PATH = "/image/family.png";

    private ModernGradientButton loginButton;

    public PawTrackLogin() {
        setTitle("PawTrack Login");
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

        // This must be after loginButton is initialized in createLoginPanel()
        if (loginButton != null) {
            this.getRootPane().setDefaultButton(loginButton);
        }
    }

    private JPanel createLoginPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new GridBagLayout());

        GlassmorphicPanel formPanel = new GlassmorphicPanel(30);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(420, 540));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        CircularImageComponent logoLabel = new CircularImageComponent("/image/Logo.png");
        logoLabel.setPreferredSize(new Dimension(120, 120));
        logoLabel.setMinimumSize(new Dimension(96, 96));
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 40, 25, 40);
        formPanel.add(logoLabel, gbc);

        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 24));
        welcomeLabel.setForeground(COLOR_TEXT);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 40, 20, 40);
        formPanel.add(welcomeLabel, gbc);

        gbc.insets = new Insets(8, 40, 8, 40);

        JLabel userLabel = new JLabel("USERNAME:");
        userLabel.setFont(new Font("Inter", Font.BOLD, 11));
        userLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(userLabel, gbc);

        RoundedTextFieldCustom usernameField = new RoundedTextFieldCustom(20);
        usernameField.setText("Enter your username");
        addPlaceholderStyle(usernameField);
        gbc.gridy = 3;
        formPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("PASSWORD:");
        passLabel.setFont(new Font("Inter", Font.BOLD, 11));
        passLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 4;
        formPanel.add(passLabel, gbc);

        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);

        RoundedPasswordFieldCustom passwordField = new RoundedPasswordFieldCustom(20);
        passwordField.setText("Enter your password");
        addPlaceholderStyle(passwordField);

        JButton eyeButton = createEyeToggleButton(passwordField);

        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(eyeButton, BorderLayout.EAST);

        gbc.gridy = 5;
        formPanel.add(passwordPanel, gbc);

        loginButton = new ModernGradientButton("LOGIN");

        loginButton.addActionListener(_ -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty() ||
                    username.equals("Enter your username") || password.equals("Enter your password")) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            loginButton.setEnabled(false);
            loginButton.setText("LOGGING IN...");

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

                private String fullName = "";
                private String errorMessage = "An unexpected error occurred.";

                @Override
                protected String doInBackground() throws Exception {
                    String sql = "SELECT full_name, password FROM user_accounts WHERE username = ?";

                    try (Connection conn = DBConnector.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            String passwordFromDB = rs.getString("password");
                            fullName = rs.getString("full_name");

                            if (password.equalsIgnoreCase(passwordFromDB)) {
                                return "SUCCESS";
                            } else {
                                return "WRONG_PASSWORD";
                            }
                        } else {
                            return "WRONG_USERNAME";
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        errorMessage = "Database Error: " + ex.getMessage();
                        return "DB_ERROR";
                    }
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        switch (result) {
                            case "SUCCESS":
                                JOptionPane.showMessageDialog(PawTrackLogin.this, "Welcome, " + fullName + "!");
                                PawTrackLogin.this.dispose();
                                SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
                                break;
                            case "WRONG_PASSWORD":
                                JOptionPane.showMessageDialog(PawTrackLogin.this, "Incorrect password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                                break;
                            case "WRONG_USERNAME":
                                JOptionPane.showMessageDialog(PawTrackLogin.this, "Username not found.", "Login Error", JOptionPane.ERROR_MESSAGE);
                                break;
                            case "DB_ERROR":
                                JOptionPane.showMessageDialog(PawTrackLogin.this, errorMessage, "Login Error", JOptionPane.ERROR_MESSAGE);
                                break;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PawTrackLogin.this,
                                "An unexpected error occurred: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");
                }
            };

            worker.execute();
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(25, 40, 15, 40);
        formPanel.add(loginButton, gbc);

        JPanel linksPanel = new JPanel(new BorderLayout(10, 0));
        linksPanel.setOpaque(false);
        JButton createAccountButton = createLinkButton("CREATE ACCOUNT");
        createAccountButton.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> new CreateAccount().setVisible(true));
        });

        JButton forgotPasswordButton = createLinkButton("FORGOT PASSWORD");
        forgotPasswordButton.addActionListener(e -> {
            handleForgotPassword();
        });

        linksPanel.add(createAccountButton, BorderLayout.WEST);
        linksPanel.add(forgotPasswordButton, BorderLayout.EAST);

        gbc.gridy = 7;
        gbc.insets = new Insets(5, 40, 15, 40);
        formPanel.add(linksPanel, gbc);

        JLabel watermarkLabel = new JLabel("© All Right Reserved");
        watermarkLabel.setFont(new Font("Inter", Font.PLAIN, 10));
        watermarkLabel.setForeground(COLOR_PLACEHOLDER);
        watermarkLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 8;
        formPanel.add(watermarkLabel, gbc);

        leftPanel.add(formPanel);
        return leftPanel;
    }

    private void handleForgotPassword() {
        String username = JOptionPane.showInputDialog(this, "Please enter your username:", "Forgot Password", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        String email = JOptionPane.showInputDialog(this, "Please enter the email you registered with:", "Forgot Password", JOptionPane.PLAIN_MESSAGE);
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        SwingWorker<Boolean, Void> checker = new SwingWorker<Boolean, Void>() {
            private String errorMessage = "Username and Email do not match.";

            @Override
            protected Boolean doInBackground() throws Exception {
                String sql = "SELECT COUNT(*) FROM user_accounts WHERE username = ? AND email = ?";
                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, username);
                    stmt.setString(2, email);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMessage = "Database Error: " + ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        askForNewPassword(username, email);
                    } else {
                        JOptionPane.showMessageDialog(PawTrackLogin.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PawTrackLogin.this, "An unexpected error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        checker.execute();
    }

    private void askForNewPassword(String username, String email) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Enter your new password:"), BorderLayout.NORTH);
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(this, panel, "Reset Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String newPassword = new String(passwordField.getPassword());
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SwingWorker<Boolean, Void> updater = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    String sql = "UPDATE user_accounts SET password = ? WHERE username = ? AND email = ?";
                    try (Connection conn = DBConnector.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, newPassword);
                        stmt.setString(2, username);
                        stmt.setString(3, email);

                        int rowsUpdated = stmt.executeUpdate();
                        return rowsUpdated > 0;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(PawTrackLogin.this, "Password updated successfully! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(PawTrackLogin.this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PawTrackLogin.this, "An unexpected error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            updater.execute();
        }
    }


    private JButton createEyeToggleButton(RoundedPasswordFieldCustom passwordField) {
        JButton eyeButton = new JButton() {
            private boolean isPasswordVisible = false;
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(229, 231, 235));
                } else if (isHovered) {
                    g2d.setColor(new Color(243, 244, 246));
                } else {
                    g2d.setColor(new Color(255, 255, 255, 0));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                Color iconColor = isHovered ? new Color(79, 70, 229) : new Color(107, 114, 128);
                g2d.setColor(iconColor);
                g2d.setStroke(new BasicStroke(2f));

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                if (isPasswordVisible) {
                    drawEyeIcon(g2d, centerX, centerY, iconColor);
                    g2d.drawLine(centerX - 8, centerY - 8, centerX + 8, centerY + 8);
                } else {
                    drawEyeIcon(g2d, centerX, centerY, iconColor);
                }

                g2d.dispose();
            }

            private void drawEyeIcon(Graphics2D g2d, int centerX, int centerY, Color color) {
                java.awt.geom.Path2D.Float eyeShape = new java.awt.geom.Path2D.Float();
                eyeShape.moveTo(centerX - 10, centerY);
                eyeShape.curveTo(centerX - 10, centerY - 6, centerX - 6, centerY - 8, centerX, centerY - 8);
                eyeShape.curveTo(centerX + 6, centerY - 8, centerX + 10, centerY - 6, centerX + 10, centerY);
                eyeShape.curveTo(centerX + 10, centerY + 6, centerX + 6, centerY + 8, centerX, centerY + 8);
                eyeShape.curveTo(centerX - 6, centerY + 8, centerX - 10, centerY + 6, centerX - 10, centerY);
                eyeShape.closePath();

                g2d.setColor(color);
                g2d.draw(eyeShape);

                g2d.fillOval(centerX - 3, centerY - 3, 6, 6);
            }
        };

        eyeButton.setPreferredSize(new Dimension(40, 40));
        eyeButton.setFocusPainted(false);
        eyeButton.setBorderPainted(false);
        eyeButton.setContentAreaFilled(false);
        eyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeButton.setToolTipText("Show/Hide password");

        eyeButton.addActionListener(e -> {
            try {
                java.lang.reflect.Field field = eyeButton.getClass().getDeclaredField("isPasswordVisible");
                field.setAccessible(true);
                boolean currentState = field.getBoolean(eyeButton);
                field.setBoolean(eyeButton, !currentState);

                if (!currentState) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('•');
                }

                eyeButton.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        eyeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    java.lang.reflect.Field field = eyeButton.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(eyeButton, true);
                    eyeButton.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                try {
                    java.lang.reflect.Field field = eyeButton.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(eyeButton, false);
                    eyeButton.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return eyeButton;
    }

    private JPanel createInfoPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        GlassmorphicPanel infoPanel = new GlassmorphicPanel(30);
        infoPanel.setLayout(new BorderLayout(15, 15));
        infoPanel.setPreferredSize(new Dimension(420, 540));
        infoPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel imageLabel = new RoundedImageComponent("image/family.png", 20);
        imageLabel.setPreferredSize(new Dimension(360, 240));
        infoPanel.add(imageLabel, BorderLayout.NORTH);

        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 10));
        descriptionPanel.setOpaque(false);

        JLabel descTitle = new JLabel("About PawTrack");
        descTitle.setFont(new Font("Inter", Font.BOLD, 18));
        descTitle.setForeground(COLOR_TEXT);
        descriptionPanel.add(descTitle, BorderLayout.NORTH);

        JTextArea descriptionArea = new JTextArea(
                "The Paw Track System is a digital platform designed to help abandoned and " +
                        "surrendered pets find new, loving homes. It connects individuals who can no " +
                        "longer care for their pets with responsible adopters who are ready to welcome " +
                        "them as part of their family. Through the system, users can view available pets, " +
                        "learn about their background and needs, and apply for adoption."
        );
        descriptionArea.setFont(new Font("Inter", Font.PLAIN, 13));
        descriptionArea.setForeground(COLOR_TEXT);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setEditable(false);
        descriptionPanel.add(descriptionArea, BorderLayout.CENTER);
        infoPanel.add(descriptionPanel, BorderLayout.CENTER);

        rightPanel.add(infoPanel);
        return rightPanel;
    }

    private void addPlaceholderStyle(JTextField textField) {
        Font placeholderFont = new Font("Inter", Font.ITALIC, textField.getFont().getSize());
        Font defaultFont = new Font("Inter", Font.PLAIN, textField.getFont().getSize());
        String placeholder = textField.getText();

        textField.setFont(placeholderFont);
        textField.setForeground(COLOR_PLACEHOLDER);

        if (textField instanceof JPasswordField jPasswordField) {
            jPasswordField.setEchoChar((char) 0);
        }

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getForeground() == COLOR_PLACEHOLDER) {
                    textField.setText("");
                    textField.setFont(defaultFont);
                    textField.setForeground(COLOR_TEXT);
                    if (textField instanceof JPasswordField jPasswordField) {
                        jPasswordField.setEchoChar('•');
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setFont(placeholderFont);
                    textField.setForeground(COLOR_PLACEHOLDER);
                    textField.setText(placeholder);
                    if (textField instanceof JPasswordField jPasswordField) {
                        jPasswordField.setEchoChar((char) 0);
                    }
                }
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 45));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(COLOR_BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(COLOR_BUTTON);
            }
        });

        return button;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 11));
        button.setForeground(COLOR_ACCENT);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(COLOR_BUTTON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(COLOR_ACCENT);
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PawTrackLogin().setVisible(true));
    }
}


class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    private Image blurredImage;

    // CONSTRUCTOR (Changed to accept String imagePath)
    public BackgroundPanel(String imagePath) {
        setOpaque(true);
        loadBackgroundImage(imagePath);
    }

    private void loadBackgroundImage(String imagePath) {
        try {
            String resourcePath = "/" + imagePath;
            java.net.URL imageUrl = getClass().getResource(resourcePath);

            if (imageUrl != null) {
                this.backgroundImage = ImageIO.read(imageUrl); // Load stream from resource
                this.blurredImage = createBlurredImage(this.backgroundImage);
            } else {
                System.err.println("Could not load background image resource: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }

    private Image createBlurredImage(Image source) {
        if (source == null) return null;

        int width = source.getWidth(null);
        int height = source.getHeight(null);

        if (width <= 0 || height <= 0) return source;

        java.awt.image.BufferedImage buffered = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();

        float[] matrix = new float[121];
        for (int i = 0; i < 121; i++) {
            matrix[i] = 1.0f / 121.0f;
        }

        java.awt.image.BufferedImageOp op = new java.awt.image.ConvolveOp(
                new java.awt.image.Kernel(11, 11, matrix),
                java.awt.image.ConvolveOp.EDGE_NO_OP, null);

        return op.filter(buffered, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (blurredImage != null) {
            g2d.drawImage(blurredImage, 0, 0, getWidth(), getHeight(), this);
        } else if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 41, 59),
                    getWidth(), getHeight(), new Color(51, 65, 85)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        g2d.dispose();
    }
}

class GlassmorphicPanel extends JPanel {
    private final int cornerRadius;

    public GlassmorphicPanel(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(4, 4, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2d.setColor(new Color(255, 255, 255, 245));
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2d.dispose();
    }
}

class ModernGradientButton extends JButton {
    private boolean isHovered = false;
    private final Color color1 = new Color(99, 102, 241);
    private final Color color2 = new Color(139, 92, 246);
    private final Color hoverColor1 = new Color(79, 70, 229);
    private final Color hoverColor2 = new Color(109, 40, 217);

    public ModernGradientButton(String text) {
        super(text);
        setFont(new Font("Inter", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(100, 50));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 3, 25, 25);

        GradientPaint gradient = new GradientPaint(
                0, 0, isHovered ? hoverColor1 : color1,
                getWidth(), getHeight(), isHovered ? hoverColor2 : color2
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);

        g2d.dispose();
        super.paintComponent(g);
    }
}

class CustomRoundedPanel extends JPanel {
    private final int cornerRadius;
    private final Color backgroundColor;

    public CustomRoundedPanel(int radius, Color bgColor) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2d.dispose();
    }
}

class RoundedTextFieldCustom extends JTextField {
    public RoundedTextFieldCustom(int size) {
        super(size);
        setOpaque(false);
        setBackground(new Color(243, 244, 246));
        setForeground(new Color(30, 41, 59));
        setBorder(new EmptyBorder(14, 20, 14, 20));
        setFont(new Font("Inter", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, 25, 25);

        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);

        super.paintComponent(g2d);
        g2d.dispose();
    }
}

class RoundedPasswordFieldCustom extends JPasswordField {
    public RoundedPasswordFieldCustom(int size) {
        super(size);
        setOpaque(false);
        setBackground(new Color(243, 244, 246));
        setForeground(new Color(30, 41, 59));
        setBorder(new EmptyBorder(14, 20, 14, 20));
        setFont(new Font("Inter", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, 25, 25);

        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);

        super.paintComponent(g2d);
        g2d.dispose();
    }
}

class CircularImageComponent extends JLabel {
    private Image originalImage;

    public CircularImageComponent(String imagePath) {
        setOpaque(false);
        loadImage(imagePath);
    }


    private void loadImage(String imagePath) {
        try {

            String resourcePath = "/" + imagePath;
            java.net.URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                this.originalImage = ImageIO.read(imageUrl);
                repaint();
            } else {
                setText("Image not found");
                setHorizontalAlignment(SwingConstants.CENTER);
                System.err.println("Could not find image resource at: " + resourcePath); // Enhanced error
            }
        } catch (Exception e) {
            setText("Image error");
            setHorizontalAlignment(SwingConstants.CENTER);
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        int dimension = Math.max(size.width, size.height);
        return new Dimension(dimension, dimension);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (originalImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2d.setColor(new Color(0, 0, 0, 40));
            java.awt.geom.Ellipse2D.Float shadowCircle = new java.awt.geom.Ellipse2D.Float(x + 2, y + 3, size, size);
            g2d.fill(shadowCircle);

            java.awt.geom.Ellipse2D.Float circle = new java.awt.geom.Ellipse2D.Float(x, y, size, size);

            g2d.setClip(circle);

            int imgWidth = originalImage.getWidth(this);
            int imgHeight = originalImage.getHeight(this);
            double scale = Math.max((double) size / imgWidth, (double) size / imgHeight);
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);
            int imgX = x + (size - scaledWidth) / 2;
            int imgY = y + (size - scaledHeight) / 2;

            g2d.drawImage(originalImage, imgX, imgY, scaledWidth, scaledHeight, this);

            g2d.setClip(null);
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.setStroke(new BasicStroke(3f));
            g2d.draw(circle);

            g2d.dispose();
        } else {
            super.paintComponent(g);
        }
    }
}

class RoundedImageComponent extends JLabel {
    private Image originalImage;
    private final int cornerRadius;
    public RoundedImageComponent(String imagePath, int cornerRadius) {
        this.cornerRadius = cornerRadius;
        setOpaque(false);
        loadImage(imagePath);
    }
    private void loadImage(String imagePath) {
        try {
            // Correctly form the classpath path by ensuring it starts with /
            String resourcePath = "/" + imagePath;
            java.net.URL imageUrl = getClass().getResource(resourcePath);

            if (imageUrl != null) {
                // Use ImageIO.read(URL) for reliable classpath resource loading
                this.originalImage = javax.imageio.ImageIO.read(imageUrl);
                repaint();
            } else {
                setText("Image not found");
                setHorizontalAlignment(SwingConstants.CENTER);
                System.err.println("Could not find image resource at: " + resourcePath);
            }
        } catch (Exception e) {
            setText("Image error");
            setHorizontalAlignment(SwingConstants.CENTER);
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (originalImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int componentWidth = getWidth();
            int componentHeight = getHeight();

            if (componentWidth <= 0 || componentHeight <= 0) {
                g2d.dispose();
                return;
            }

            int imageWidth = originalImage.getWidth(this);
            int imageHeight = originalImage.getHeight(this);

            if (imageWidth <= 0 || imageHeight <= 0) {
                g2d.dispose();
                super.paintComponent(g);
                return;
            }

            double imageAspect = (double) imageWidth / (double) imageHeight;
            double componentAspect = (double) componentWidth / (double) componentHeight;
            int newWidth, newHeight, x, y;

            if (imageAspect > componentAspect) {
                newWidth = componentWidth;
                newHeight = (int) (newWidth / imageAspect);
                x = 0;
                y = (componentHeight - newHeight) / 2;
            } else {
                newHeight = componentHeight;
                newWidth = (int) (newHeight * imageAspect);
                y = 0;
                x = (componentWidth - newWidth) / 2;
            }

            g2d.setColor(new Color(0, 0, 0, 20));
            RoundRectangle2D shadowRect = new RoundRectangle2D.Float(1, 2, componentWidth - 2, componentHeight - 2, cornerRadius, cornerRadius);
            g2d.fill(shadowRect);

            RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, componentWidth, componentHeight, cornerRadius, cornerRadius);
            g2d.setClip(roundRect);
            g2d.drawImage(originalImage, x, y, newWidth, newHeight, this);

            g2d.setClip(null);
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(roundRect);

            g2d.dispose();
        } else {
            super.paintComponent(g);
        }
    }
}
