import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class CreateAccount extends JFrame {

    // Define colors for easy customization
    private static final Color COLOR_BACKGROUND = new Color(64, 64, 64);
    private static final Color COLOR_PANEL = new Color(64, 64, 64, 50);
    private static final Color COLOR_TEXT = new Color(209, 213, 219);
    private static final Color COLOR_BUTTON_SUBMIT = new Color(22, 163, 74);
    private static final Color COLOR_BUTTON_SUBMIT_HOVER = new Color(21, 128, 61);
    private static final Color COLOR_BUTTON_BACK = new Color(220, 38, 38);
    private static final Color COLOR_BUTTON_BACK_HOVER = new Color(185, 28, 28);
    private static final Color COLOR_ERROR = new Color(248, 113, 113);
    private static final Color COLOR_SUCCESS = new Color(74, 222, 128);

    // Form components
    private RoundedTextField fullNameField, usernameField;
    private RoundedPasswordField passwordField, repeatPasswordField;
    private RoundedTextField contactNumberField, emailField;

    // Error labels
    private JLabel fullNameError, usernameError, passwordError, repeatPasswordError, contactNumberError, emailError, successMessage;

    public CreateAccount() {
        setTitle("Create Account - PawTrack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setResizable(true);
        getContentPane().setBackground(COLOR_BACKGROUND);
        
        setLayout(new GridLayout(1, 2, 20, 0));
        getRootPane().setBorder(new EmptyBorder(20, 20, 20, 20));

        addWindowStateListener((java.awt.event.WindowEvent e) -> {
            if ((e.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                System.out.println("Window maximized");
            } else if ((JFrame.NORMAL) == JFrame.NORMAL) {
                System.out.println("Window restored");
            }
            repaint();
            revalidate();
        });

        add(createFormPanel());
        add(createInfoPanel());
        
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JComponent createFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(COLOR_BACKGROUND);
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        RoundedPanel formPanel = new RoundedPanel(32, COLOR_PANEL);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(480, 700));
        formPanel.setMinimumSize(new Dimension(420, 650));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 40, 0, 40);

        // Logo
        String logoPath = "C://Users//Paul//IdeaProjects//Paw-Track/image/Logo.png";
        JLabel logo = new RoundedImageLabel(logoPath, 96);
        logo.setPreferredSize(new Dimension(96, 96));
        logo.setMinimumSize(new Dimension(96, 96));
        logo.setMaximumSize(new Dimension(96, 96));
        
        File logoFile = new File(logoPath);
        System.out.println("Logo file exists: " + logoFile.exists());
        System.out.println("Logo file path: " + logoPath);
        
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 40, 10, 40);
        formPanel.add(logo, gbc);

        gbc.insets = new Insets(2, 40, 0, 40);

        // Full Name
        gbc.gridy++;
        formPanel.add(createLabel("FULL NAME:"), gbc);
        gbc.gridy++;
        fullNameField = new RoundedTextField();
        formPanel.add(fullNameField, gbc);
        gbc.gridy++;
        fullNameError = createErrorLabel();
        formPanel.add(fullNameError, gbc);

        // Username
        gbc.gridy++;
        formPanel.add(createLabel("USERNAME:"), gbc);
        gbc.gridy++;
        usernameField = new RoundedTextField();
        formPanel.add(usernameField, gbc);
        gbc.gridy++;
        usernameError = createErrorLabel();
        formPanel.add(usernameError, gbc);

        // Password
        gbc.gridy++;
        formPanel.add(createLabel("PASSWORD:"), gbc);
        gbc.gridy++;
        passwordField = new RoundedPasswordField();
        formPanel.add(passwordField, gbc);
        gbc.gridy++;
        passwordError = createErrorLabel();
        formPanel.add(passwordError, gbc);

        // Repeat Password
        gbc.gridy++;
        formPanel.add(createLabel("REPEAT PASSWORD:"), gbc);
        gbc.gridy++;
        repeatPasswordField = new RoundedPasswordField();
        formPanel.add(repeatPasswordField, gbc);
        gbc.gridy++;
        repeatPasswordError = createErrorLabel();
        formPanel.add(repeatPasswordError, gbc);
        
        // Contact Number
        gbc.gridy++;
        formPanel.add(createLabel("CONTACT NUMBER:"), gbc);
        gbc.gridy++;
        contactNumberField = new RoundedTextField();
        formPanel.add(contactNumberField, gbc);
        gbc.gridy++;
        contactNumberError = createErrorLabel();
        formPanel.add(contactNumberError, gbc);

        // Email
        gbc.gridy++;
        formPanel.add(createLabel("EMAIL:"), gbc);
        gbc.gridy++;
        emailField = new RoundedTextField();
        formPanel.add(emailField, gbc);
        gbc.gridy++;
        emailError = createErrorLabel();
        formPanel.add(emailError, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(COLOR_BACKGROUND);
        
        JButton submitButton = createStyledButton("SUBMIT", COLOR_BUTTON_SUBMIT, COLOR_BUTTON_SUBMIT_HOVER);
        submitButton.addActionListener(_ -> handleSubmit());
        
        JButton backButton = createStyledButton("BACK", COLOR_BUTTON_BACK, COLOR_BUTTON_BACK_HOVER);
        backButton.addActionListener(_ -> {
            this.dispose();
            try {
                SwingUtilities.invokeLater(() -> new PawTrackLogin().setVisible(true));
            } catch (Exception a) {
                System.err.println("PawTrackLogin class not found. Creating a basic login window would go here.");
                System.exit(0);
            }
        });
        
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);
        gbc.gridy++;
        gbc.insets = new Insets(15, 40, 0, 40);
        formPanel.add(buttonPanel, gbc);
        
        // Success Message
        successMessage = new JLabel("", SwingConstants.CENTER);
        successMessage.setFont(new Font("Inter", Font.BOLD, 14));
        successMessage.setForeground(COLOR_SUCCESS);
        gbc.gridy++;
        gbc.insets = new Insets(10, 40, 10, 40);
        formPanel.add(successMessage, gbc);

        GridBagConstraints wrapperGbc = new GridBagConstraints();
        wrapperGbc.gridx = 0;
        wrapperGbc.gridy = 0;
        wrapperGbc.weightx = 1.0;
        wrapperGbc.weighty = 1.0;
        wrapperGbc.anchor = GridBagConstraints.CENTER;
        wrapperGbc.fill = GridBagConstraints.NONE;
        wrapper.add(formPanel, wrapperGbc);
        
        return wrapper;
    }

    private JComponent createInfoPanel() {
        JPanel infoWrapper = new JPanel();
        infoWrapper.setOpaque(true);
        infoWrapper.setBackground(COLOR_BACKGROUND);
        infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.Y_AXIS));
        infoWrapper.setBorder(new EmptyBorder(40, 30, 40, 30));

        // Top Panel
        RoundedPanel topPanel = new RoundedPanel(32, COLOR_PANEL);
        topPanel.setLayout(new BorderLayout(0, 16));
        topPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel imageLabel = new RoundedImageLabel("C://Users//Paul//IdeaProjects//Paw-Track/image/family.png", 24);
        imageLabel.setPreferredSize(new Dimension(100, 200));
        imageLabel.setMinimumSize(new Dimension(80, 120));
        topPanel.add(imageLabel, BorderLayout.NORTH);

        JTextArea topText = new JTextArea(
            "The PawPatrol System is a digital platform created to support the adoption of pets that have been abandoned or surrendered by their owners. Its main goal is to provide a safe and reliable way for these animals to find a new family that will love and care for them."
        );
        topText.setEditable(false);
        topText.setOpaque(false);
        topText.setForeground(COLOR_TEXT);
        topText.setFont(new Font("Inter", Font.PLAIN, 14));
        topText.setLineWrap(true);
        topText.setWrapStyleWord(true);
        topPanel.add(topText, BorderLayout.CENTER);

        // Bottom Panel
        RoundedPanel bottomPanel = new RoundedPanel(32, COLOR_PANEL);
        bottomPanel.setLayout(new BorderLayout(0, 16));
        bottomPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        
        JLabel bottomTitle = new JLabel("How the System Works:");
        bottomTitle.setFont(new Font("Inter", Font.BOLD, 20));
        bottomTitle.setForeground(Color.WHITE);
        bottomPanel.add(bottomTitle, BorderLayout.NORTH);

        JTextArea bottomText = new JTextArea(
            """
            1. Pet Registration - Shelters, rescuers, or former owners can register pets in the system by providing details such as age, breed, health status, and personality.
            
            2. Pet Listings - All registered animals are displayed with photos and descriptions so adopters can easily browse.
            
            3. Adoption Application - Interested adopters can submit an adoption request directly through the system.
            
            4. Screening Process - The system ensures that pets are matched with responsible owners by checking applications.
            
            5. Final Adoption - Once approved, the adopter can officially welcome the pet into their home.""");
        bottomText.setEditable(false);
        bottomText.setOpaque(false);
        bottomText.setForeground(COLOR_TEXT);
        bottomText.setFont(new Font("Inter", Font.PLAIN, 14));
        bottomText.setLineWrap(true);
        bottomText.setWrapStyleWord(true);
        bottomPanel.add(bottomText, BorderLayout.CENTER);
        
        infoWrapper.add(topPanel);
        infoWrapper.add(Box.createRigidArea(new Dimension(0, 32)));
        infoWrapper.add(bottomPanel);
        
        return infoWrapper;
    }

    private void handleSubmit() {
        successMessage.setText("");
        if (validateForm()) {
            try {
                //  Load MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                //  Connect to your database (adjust DB name, user, password)
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/pawpatrol_db", "root", "");

                // Insert new user into the database
                String sql = "INSERT INTO users (full_name, username, password, contact_number, email) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, fullNameField.getText().trim());
                stmt.setString(2, usernameField.getText().trim());
                stmt.setString(3, new String(passwordField.getPassword())); // ⚠️ plain text for now
                stmt.setString(4, contactNumberField.getText().trim());
                stmt.setString(5, emailField.getText().trim());

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    successMessage.setText("Account created successfully!");
// After successful account creation, redirect back to login
                    Timer timer = new Timer(2000, _ -> {
                        this.dispose();
                        SwingUtilities.invokeLater(() -> new PawTrackLogin().setVisible(true));
                    });
                    timer.setRepeats(false);
                    timer.start();
                }

                stmt.close();
                conn.close();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving account: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private boolean validateForm() {
        boolean isValid = true;
        clearErrors();

        // Full Name
        if (fullNameField.getText().trim().isEmpty()) {
            fullNameError.setText("Full name is required.");
            isValid = false;
        }

        // Username
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            usernameError.setText("Username is required.");
            isValid = false;
        } else if (username.length() < 8) {
            usernameError.setText("Username must be at least 8 characters.");
            isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameError.setText("Username can only contain letters, numbers, and underscores.");
            isValid = false;
        }

        // Password
        String pass = new String(passwordField.getPassword());
        if (pass.trim().isEmpty()) {
            passwordError.setText("Password is required.");
            isValid = false;
        } else if (pass.length() < 8) {
            passwordError.setText("Password must be at least 8 characters.");
            isValid = false;
        }

        // Repeat Password
        String repeatPass = new String(repeatPasswordField.getPassword());
        if (repeatPass.trim().isEmpty()) {
            repeatPasswordError.setText("Please confirm your password.");
            isValid = false;
        } else if (!pass.equals(repeatPass)) {
            repeatPasswordError.setText("Passwords do not match.");
            isValid = false;
        }

        // Contact Number
        String contact = contactNumberField.getText().trim();
        if (contact.isEmpty()) {
            contactNumberError.setText("Contact number is required.");
            isValid = false;
        } else if (!contact.matches("\\d+")) {
            contactNumberError.setText("Please enter a valid number.");
            isValid = false;
        }
        
        // Email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Email is required.");
            isValid = false;
        } else if (!Pattern.compile("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
                .matcher(email).matches()) {
            emailError.setText("Please enter a valid email address.");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        fullNameError.setText(" ");
        usernameError.setText(" ");
        passwordError.setText(" ");
        repeatPasswordError.setText(" ");
        contactNumberError.setText(" ");
        emailError.setText(" ");
    }
    
    // --- Helper methods for creating components ---
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT);
        return label;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" "); // Start with a space to reserve height
        label.setFont(new Font("Inter", Font.PLAIN, 12));
        label.setForeground(COLOR_ERROR);
        return label;
    }

    private JButton createStyledButton(String text, Color bg, Color hoverBg) {
        return new RoundedButton(text, bg, hoverBg);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CreateAccount().setVisible(true));
    }
}

// --- Custom Rounded Components ---

class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private final Color backgroundColor;

    public RoundedPanel(int radius, Color bgColor) {
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

class RoundedTextField extends JTextField {
    public RoundedTextField() {
        super();
        setOpaque(false);
        setBackground(new Color(209, 213, 219));
        setForeground(new Color(31, 41, 55));
        setBorder(new EmptyBorder(12, 18, 12, 18));
        setFont(new Font("Inter", Font.PLAIN, 14));
        setPreferredSize(new Dimension(300, 45));
        setMinimumSize(new Dimension(250, 40));
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

class RoundedPasswordField extends JPasswordField {
    public RoundedPasswordField() {
        super();
        setOpaque(false);
        setBackground(new Color(209, 213, 219));
        setForeground(new Color(31, 41, 55));
        setBorder(new EmptyBorder(12, 18, 12, 18));
        setFont(new Font("Inter", Font.PLAIN, 14));
        setPreferredSize(new Dimension(300, 45));
        setMinimumSize(new Dimension(250, 40));
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

class RoundedImageLabel extends JLabel {
    @SuppressWarnings("FieldMayBeFinal")
    private Image image;
    private final int cornerRadius;

    public RoundedImageLabel(String path, int cornerRadius) {
        this.cornerRadius = cornerRadius;
        try {
            if (path.startsWith("http")) {
                this.image = ImageIO.read(java.net.URI.create(path).toURL());
            } else {
                this.image = ImageIO.read(new File(path));
            }
            if (image != null) {
                System.out.println("Image loaded successfully: " + path +
                    " (Size: " + image.getWidth(null) + "x" + image.getHeight(null) + ")");
            }
        } catch (IOException e) {
            setText("🐾 PT");
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 24));
            System.err.println("Could not load image from: " + path);
            this.image = null;
        }
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // Get component dimensions
            int componentWidth = getWidth();
            int componentHeight = getHeight();
            
            if (componentWidth > 0 && componentHeight > 0) {
                // Calculate scaling to fit within component while maintaining aspect ratio
                int imageWidth = image.getWidth(null);
                int imageHeight = image.getHeight(null);
                
                double scaleX = (double) componentWidth / imageWidth;
                double scaleY = (double) componentHeight / imageHeight;
                double scale = Math.min(scaleX, scaleY);
                
                int newWidth = (int) (imageWidth * scale);
                int newHeight = (int) (imageHeight * scale);
                int x = (componentWidth - newWidth) / 2;
                int y = (componentHeight - newHeight) / 2;
                
                // Create rounded rectangle clip
                RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, componentWidth, componentHeight, cornerRadius, cornerRadius);
                g2d.setClip(roundRect);
                
                // Draw the scaled and centered image
                g2d.drawImage(image, x, y, newWidth, newHeight, this);
            }
            g2d.dispose();
        } else {
            super.paintComponent(g);
        }
    }
}

class RoundedButton extends JButton {
    private final Color backgroundColor;
    private final Color hoverBackgroundColor;

    public RoundedButton(String text, Color bg, Color hoverBg) {
        super(text);
        this.backgroundColor = bg;
        this.hoverBackgroundColor = hoverBg;

        // Style the button
        setFont(new Font("Inter", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(120, 45)); // Slightly wider for better text fit
        setMinimumSize(new Dimension(100, 40));
        setMaximumSize(new Dimension(150, 50));
        
        // Remove default button painting
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Use ButtonModel to check for hover state
        if (getModel().isRollover()) {
            g2d.setColor(hoverBackgroundColor);
        } else {
            g2d.setColor(backgroundColor);
        }

        // Paint the rounded background
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25); // More rounded corners
        
        // Draw the text manually for better control
        g2d.setColor(getForeground());
        g2d.setFont(getFont());
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(getText(), textX, textY);
        
        g2d.dispose();
    }
}

