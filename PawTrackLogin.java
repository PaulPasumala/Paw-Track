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

public class PawTrackLogin extends JFrame {

    private static final Color COLOR_BACKGROUND = new Color(30, 41, 59);
    private static final Color COLOR_PANEL = Color.WHITE;
    private static final Color COLOR_BUTTON = new Color(30, 41, 59);
    private static final Color COLOR_BUTTON_HOVER = new Color(55, 65, 81);
    private static final Color COLOR_TEXT = new Color(55, 65, 81);
    private static final Color COLOR_PLACEHOLDER = new Color(156, 163, 175);

    public PawTrackLogin() {
        setTitle("PawTrack Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setResizable(true); // Changed to true to allow maximizing
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new GridLayout(1, 2, 0, 0));

        add(createLoginPanel());
        add(createInfoPanel());
        
        // Maximize the window
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel createLoginPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(COLOR_BACKGROUND);
        leftPanel.setLayout(new GridBagLayout());

        CustomRoundedPanel formPanel = new CustomRoundedPanel(40, COLOR_PANEL);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(380, 500));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 35, 10, 35);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel logoLabel = new RoundedImageComponent("C://Users//Paul//IdeaProjects//Paw-Track/image/Logo.png", 30);
        logoLabel.setPreferredSize(new Dimension(150, 150));
        logoLabel.setMinimumSize(new Dimension(96, 96));
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 35, 20, 35);
        formPanel.add(logoLabel, gbc);

        gbc.insets = new Insets(5, 35, 5, 35);

        JLabel userLabel = new JLabel("USERNAME:");
        userLabel.setFont(new Font("Inter", Font.BOLD, 12));
        userLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(userLabel, gbc);

        RoundedTextFieldCustom usernameField = new RoundedTextFieldCustom(20);
        usernameField.setText("Enter your username");
        addPlaceholderStyle(usernameField);
        gbc.gridy = 2;
        formPanel.add(usernameField, gbc);
        
        JLabel passLabel = new JLabel("PASSWORD:");
        passLabel.setFont(new Font("Inter", Font.BOLD, 12));
        passLabel.setForeground(COLOR_TEXT);
        gbc.gridy = 3;
        formPanel.add(passLabel, gbc);
        
        RoundedPasswordFieldCustom passwordField = new RoundedPasswordFieldCustom(20);
        passwordField.setText("Enter your password");
        addPlaceholderStyle(passwordField);
        gbc.gridy = 4;
        formPanel.add(passwordField, gbc);

        JButton loginButton = createStyledButton("LOGIN");
        loginButton.addActionListener(_ -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty() ||
                    username.equals("Enter your username") || password.equals("Enter your password")) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/pawpatrol_db", "root", "");

                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Welcome, " + rs.getString("full_name") + "!");
                    this.dispose();
                    SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password",
                            "Login Error", JOptionPane.ERROR_MESSAGE);
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error connecting to database: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 35, 10, 35);
        formPanel.add(loginButton, gbc);

        JPanel linksPanel = new JPanel(new BorderLayout());
        linksPanel.setOpaque(false);
        JButton createAccountButton = createLinkButton("CREATE ACCOUNT");
        createAccountButton.addActionListener(e -> {

            this.dispose();
            SwingUtilities.invokeLater(() -> new CreateAccount().setVisible(true));
        });
        JButton forgotPasswordButton = createLinkButton("FORGOT PASSWORD");
        forgotPasswordButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Forgot password functionality not implemented yet.",
                "Feature Not Available", JOptionPane.INFORMATION_MESSAGE);
        });
        linksPanel.add(createAccountButton, BorderLayout.WEST);
        linksPanel.add(forgotPasswordButton, BorderLayout.EAST);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 35, 10, 35);
        formPanel.add(linksPanel, gbc);

        JLabel watermarkLabel = new JLabel(" © All Right Reserved");
        watermarkLabel.setFont(new Font("Inter", Font.PLAIN, 10));
        watermarkLabel.setForeground(COLOR_PLACEHOLDER);
        watermarkLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        formPanel.add(watermarkLabel, gbc);

        leftPanel.add(formPanel); 
        return leftPanel;
    }

    private JPanel createInfoPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(COLOR_BACKGROUND);
        
        
        CustomRoundedPanel infoPanel = new CustomRoundedPanel(40, COLOR_PANEL);
        infoPanel.setLayout(new BorderLayout(10, 10));
        infoPanel.setPreferredSize(new Dimension(380, 500));
        infoPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        
        JLabel imageLabel = new RoundedImageComponent("C://Users//Paul//IdeaProjects//Paw-Track//image/family.png", 20);
        imageLabel.setPreferredSize(new Dimension(330, 220));
        infoPanel.add(imageLabel, BorderLayout.NORTH);

        
        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 5));
        descriptionPanel.setOpaque(false);

        JLabel descTitle = new JLabel("Description:");
        descTitle.setFont(new Font("Inter", Font.BOLD, 14));
        descTitle.setForeground(COLOR_TEXT);
        descriptionPanel.add(descTitle, BorderLayout.NORTH);

        JTextArea descriptionArea = new JTextArea(
            "The Paw Track System is a digital platform designed to help abandoned and " +
            "surrendered pets find new, loving homes. It connects individuals who can no " +
            "longer care for their pets with responsible adopters who are ready to welcome " +
            "them as part of their family. Through the system, users can view available pets, " +
            "learn about their background and needs, and apply for adoption."
        );
        descriptionArea.setFont(new Font("Inter", Font.PLAIN, 12));
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
        button.setFont(new Font("Inter", Font.PLAIN, 12));
        button.setForeground(COLOR_TEXT.darker());
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PawTrackLogin().setVisible(true));
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
        setBackground(new Color(229, 231, 235));
        setForeground(new Color(30, 41, 59));
        setBorder(new EmptyBorder(12, 18, 12, 18));
        setFont(new Font("Inter", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        super.paintComponent(g2d);
        g2d.dispose();
    }
}

class RoundedPasswordFieldCustom extends JPasswordField {
    public RoundedPasswordFieldCustom(int size) {
        super(size);
        setOpaque(false);
        setBackground(new Color(229, 231, 235));
        setForeground(new Color(30, 41, 59));
        setBorder(new EmptyBorder(12, 18, 12, 18));
        setFont(new Font("Inter", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        super.paintComponent(g2d);
        g2d.dispose();
    }
}

class RoundedImageComponent extends JLabel {
    private Image originalImage;
    private final int cornerRadius;

    public RoundedImageComponent(String imagePath, int cornerRadius) {
        this.cornerRadius = cornerRadius;
        
        SwingUtilities.invokeLater(() -> {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imagePath);
                    this.originalImage = icon.getImage().getScaledInstance(
                        getWidth() > 0 ? getWidth() : 150,
                        getHeight() > 0 ? getHeight() : 150,
                        Image.SCALE_FAST
                    );
                    repaint();
                } else {
                    setText("Image not found");
                    setHorizontalAlignment(SwingConstants.CENTER);
                    System.err.println("Could not find image: " + imagePath);
                }
            } catch (Exception e) {
                setText("Image error");
                setHorizontalAlignment(SwingConstants.CENTER);
                System.err.println("Error loading image: " + e.getMessage());
            }
        });
        
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (originalImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int componentWidth = getWidth();
            int componentHeight = getHeight();
            int imageWidth = originalImage.getWidth(this);
            int imageHeight = originalImage.getHeight(this);
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
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2d.setClip(roundRect);
            g2d.drawImage(originalImage, x, y, newWidth, newHeight, this);
            g2d.dispose();
        } else {
            super.paintComponent(g);
        }
    }
}

