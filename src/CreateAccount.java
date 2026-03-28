// src/CreateAccount.java
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class CreateAccount extends JFrame {

    private static final Color COLOR_TEXT = new Color(30, 41, 59);
    private static final Color COLOR_ACCENT = new Color(139, 92, 246);
    private static final String BACKGROUND_IMAGE_PATH = "image/background.png";

    // inline error label to display validation messages
    private JLabel errorLabel = new JLabel("");

    public CreateAccount() {
        setTitle("Create Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Start window normally and then maximize to full-screen
        setSize(960, 720);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        BackgroundPanel backgroundPanel = new BackgroundPanel(BACKGROUND_IMAGE_PATH);
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        GlassmorphicPanel formContainer = new GlassmorphicPanel(36);
        formContainer.setLayout(new GridBagLayout());
        formContainer.setPreferredSize(new Dimension(920, 720));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.BOTH;

        // --- Title + avatar row ---
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel avatar = new JLabel("👤", SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(72, 72));
        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        avatar.setForeground(COLOR_ACCENT);
        avatar.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel titleText = new JPanel(new GridLayout(2,1));
        titleText.setOpaque(false);
        JLabel lblTitle = new JLabel("Create Your Account", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT);

        JLabel sub = new JLabel("Simple, secure and fast — create your PawTrack account", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(90, 100, 120));

        titleText.add(lblTitle);
        titleText.add(sub);

        titleRow.add(titleText, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 4, 20);
        formContainer.add(titleRow, gbc);

        // inline error label right below title
        errorLabel.setForeground(new Color(185, 40, 40));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setVisible(false);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 12, 20);
        formContainer.add(errorLabel, gbc);

        // Reset constraints for main form
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.gridwidth = 1;
        gbc.gridy = 2;

        // --- Input form (two-column grid) ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints ip = new GridBagConstraints();
        ip.insets = new Insets(8, 8, 8, 8);
        ip.fill = GridBagConstraints.HORIZONTAL;

        // --- Form Fields ---
        RoundedTextFieldCustom lastName = new RoundedTextFieldCustom(20, "e.g. Garcia");
        RoundedTextFieldCustom firstName = new RoundedTextFieldCustom(20, "e.g. Juan");
        RoundedTextFieldCustom middleName = new RoundedTextFieldCustom(20, "Optional");
        RoundedTextFieldCustom contactNumber = new RoundedTextFieldCustom(20, "09xxxxxxxxx");
        ((AbstractDocument) contactNumber.getDocument()).setDocumentFilter(new NumericDocumentFilter(11));
        RoundedTextFieldCustom emailAddress = new RoundedTextFieldCustom(20, "you@example.com");
        ((AbstractDocument) emailAddress.getDocument()).setDocumentFilter(new LowerCaseDocumentFilter());
        RoundedTextFieldCustom user = new RoundedTextFieldCustom(20, "Choose a username");
        RoundedPasswordFieldCustom pass = new RoundedPasswordFieldCustom(20, "Create password");
        RoundedPasswordFieldCustom repeatPass = new RoundedPasswordFieldCustom(20, "Repeat password");

        emailAddress.setToolTipText("We'll use this email for password recovery.");
        contactNumber.setToolTipText("11 digits only (e.g., 09123456789)");
        pass.setToolTipText("At least 8 characters with numbers and special characters.");

        JCheckBox showPasswords = new JCheckBox("Show Passwords");
        showPasswords.setOpaque(false);
        showPasswords.setFocusPainted(false);
        showPasswords.setForeground(new Color(90, 100, 120));
        showPasswords.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswords.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswords.addActionListener(e -> {
            boolean isSelected = showPasswords.isSelected();
            pass.setEchoChar(isSelected ? (char)0 : '•');
            repeatPass.setEchoChar(isSelected ? (char)0 : '•');
        });

        // layout fields in two columns
        addLabeledField(inputPanel, ip, "Last Name:", lastName, 0, 0);
        addLabeledField(inputPanel, ip, "First Name:", firstName, 1, 0);
        addLabeledField(inputPanel, ip, "Middle Name:", middleName, 0, 2);
        addLabeledField(inputPanel, ip, "Contact Number:", contactNumber, 1, 2);
        addLabeledField(inputPanel, ip, "Email Address:", emailAddress, 0, 4);
        addLabeledField(inputPanel, ip, "Username:", user, 1, 4);
        addLabeledField(inputPanel, ip, "Password:", pass, 0, 6);
        addLabeledField(inputPanel, ip, "Repeat Password:", repeatPass, 1, 6);

        ip.gridx = 0; ip.gridy = 8; ip.gridwidth = 2; ip.anchor = GridBagConstraints.WEST;
        inputPanel.add(showPasswords, ip);

        ModernGradientButton submit = new ModernGradientButton("CREATE ACCOUNT");
        submit.setPreferredSize(new Dimension(260, 44));
        submit.addActionListener(e -> performRegister(
                lastName.getText().trim(), firstName.getText().trim(), middleName.getText().trim(),
                contactNumber.getText().trim(), emailAddress.getText().trim(), user.getText().trim(),
                new String(pass.getPassword()), new String(repeatPass.getPassword())));

        getRootPane().setDefaultButton(submit);

        JButton back = createLinkButton("BACK TO LOGIN");
        back.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { back.setText("<html><u>BACK TO LOGIN</u></html>"); }
            public void mouseExited(MouseEvent e) { back.setText("BACK TO LOGIN"); }
        });
        back.addActionListener(e -> { dispose(); new PawTrackLogin().setVisible(true); });

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnRow.add(back, BorderLayout.WEST);
        btnRow.add(submit, BorderLayout.EAST);

        ip.gridy = 10; ip.gridx = 0; ip.gridwidth = 2;
        inputPanel.add(btnRow, ip);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formContainer.add(inputPanel, gbc);

        backgroundPanel.add(formContainer);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(COLOR_TEXT);
        return label;
    }

    private JButton createLinkButton(String text) {
        JButton b = new JButton(text);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setForeground(COLOR_ACCENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // --- UPDATED REGISTER LOGIC: DUAL WRITE ---
    private void performRegister(String lastName, String firstName, String middleName, String contact, String email, String user, String pass, String repeatPass) {
        errorLabel.setVisible(false);
        errorLabel.setText("");

        if (lastName.isEmpty() || firstName.isEmpty() || contact.isEmpty() || email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            showError("Please complete all required fields.");
            return;
        }
        
        // Contact number validation: exactly 11 digits
        if (!contact.matches("\\d{11}")) {
            showError("Contact number must be exactly 11 digits.");
            return;
        }
        
        // Username validation: 6-20 characters
        if (user.length() < 6 || user.length() > 20) {
            showError("Username must be 6-20 characters long.");
            return;
        }
        
        // Password validation: minimum 8 characters, must contain numbers and special characters
        if (pass.length() < 8) {
            showError("Password must be at least 8 characters.");
            return;
        }
        if (!pass.matches(".*\\d.*")) {
            showError("Password must contain at least one number.");
            return;
        }
        if (!pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            showError("Password must contain at least one special character.");
            return;
        }
        
        if (!pass.equals(repeatPass)) {
            showError("Passwords do not match.");
            return;
        }

        boolean savedOnline = false;
        boolean savedLocal = false;
        String hashedPassword;

        try {
            hashedPassword = PasswordUtils.hash(pass);
        } catch (Exception e) {
            showError("Encryption Error: " + e.getMessage());
            return;
        }

        String sql = "INSERT INTO user_accounts (last_name, first_name, middle_name, contact_number, email_address, username, password) VALUES (?,?,?,?,?,?,?)";

        // 1. Try Online
        try (Connection onlineConn = DBConnector.getOnlineConnection()) {
            if (onlineConn != null) {
                try (PreparedStatement s = onlineConn.prepareStatement(sql)) {
                    s.setString(1, lastName); s.setString(2, firstName); s.setString(3, middleName);
                    s.setString(4, contact); s.setString(5, email); s.setString(6, user);
                    s.setString(7, hashedPassword);
                    s.executeUpdate();
                    savedOnline = true;
                    System.out.println("DEBUG: Account saved to Online DB");
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Could not save to Online DB: " + e.getMessage());
        }

        // 2. Try Local
        try (Connection localConn = DBConnector.getLocalConnection()) {
            if (localConn != null) {
                try (PreparedStatement s = localConn.prepareStatement(sql)) {
                    s.setString(1, lastName); s.setString(2, firstName); s.setString(3, middleName);
                    s.setString(4, contact); s.setString(5, email); s.setString(6, user);
                    s.setString(7, hashedPassword);
                    s.executeUpdate();
                    savedLocal = true;
                    System.out.println("DEBUG: Account saved to Local DB");
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Could not save to Local DB: " + e.getMessage());
        }

        // 3. Result Handling
        if (savedOnline || savedLocal) {
            String msg = "Account Created Successfully!";
            if (!savedLocal) msg += "\n(Saved Online only - Connect to internet to sync later)";
            if (!savedOnline) msg += "\n(Saved Locally only - Will not appear on other devices)";

            JOptionPane.showMessageDialog(this, msg);
            dispose();
            new PawTrackLogin().setVisible(true);
        } else {
            showError("Network Error: Could not connect to any database.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void addLabeledField(JPanel panel, GridBagConstraints ip, String labelText, JComponent comp, int col, int startRow) {
        ip.gridx = col;
        ip.gridy = startRow;
        ip.gridwidth = 1;
        panel.add(createLabel(labelText), ip);

        ip.gridy = startRow + 1;
        ip.weightx = 0.5;
        panel.add(comp, ip);
    }

    // --- UI Components ---
    static class BackgroundPanel extends JPanel {
        private Image img;
        public BackgroundPanel(String path) { try { URL u = CreateAccount.class.getResource("/" + path); if(u!=null) img = ImageIO.read(u); } catch(Exception e){} }
        protected void paintComponent(Graphics g) { super.paintComponent(g); if(img!=null) g.drawImage(img, 0,0,getWidth(),getHeight(),this); else { g.setColor(new Color(30,41,59)); g.fillRect(0,0,getWidth(),getHeight()); } }
    }

    static class GlassmorphicPanel extends JPanel {
        int r;
        public GlassmorphicPanel(int r) { this.r=r; setOpaque(false); setBorder(new EmptyBorder(20,20,20,20)); }
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(20, 25, 35, 45));
            g2.fillRoundRect(6, 8, w-6, h-6, r+8, r+8);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(242, 236, 255, 240),
                    0, h, new Color(235, 245, 255, 230)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w-12, h-12, r, r);

            g2.setColor(new Color(200,200,210,120));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w-12, h-12, r, r);

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
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c1 = hover ? new Color(86,86,240) : new Color(99,102,241);
            Color c2 = hover ? new Color(160,100,245) : new Color(139,92,246);
            if (getModel().isPressed()) { c1 = c1.darker(); c2 = c2.darker(); }
            g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedTextFieldCustom extends JTextField {
        private String placeholder;
        public RoundedTextFieldCustom(int c, String placeholder) {
            super(c);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12,14,12,14));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setCaretColor(COLOR_TEXT);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(245,246,249));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);

            if (isFocusOwner()) {
                g2.setColor(new Color(139,92,246,180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,20,20);
            }

            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && placeholder != null && !isFocusOwner()) {
                Graphics2D g3 = (Graphics2D) getGraphics();
                if (g3 != null) {
                    g3.setColor(new Color(125, 130, 150));
                    g3.setFont(getFont());
                    FontMetrics fm = g3.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g3.drawString(placeholder, 14, y);
                    g3.dispose();
                }
            }
        }
    }

    static class RoundedPasswordFieldCustom extends JPasswordField {
        private String placeholder;
        public RoundedPasswordFieldCustom(int c, String placeholder) {
            super(c);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12,14,12,14));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setCaretColor(COLOR_TEXT);
            setEchoChar('•');
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(245,246,249));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);

            if (isFocusOwner()) {
                g2.setColor(new Color(139,92,246,180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,20,20);
            }

            g2.dispose();
            super.paintComponent(g);

            if (getPassword().length == 0 && placeholder != null && !isFocusOwner()) {
                Graphics2D g3 = (Graphics2D) getGraphics();
                if (g3 != null) {
                    g3.setColor(new Color(125, 130, 150));
                    g3.setFont(getFont());
                    FontMetrics fm = g3.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g3.drawString(placeholder, 14, y);
                    g3.dispose();
                }
            }
        }
    }

    // Document filter to convert input to lowercase
    static class LowerCaseDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null) {
                super.insertString(fb, offset, string.toLowerCase(), attr);
            }
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null) {
                super.replace(fb, offset, length, text.toLowerCase(), attrs);
            }
        }
    }

    // Document filter to allow only numeric input with max length
    static class NumericDocumentFilter extends DocumentFilter {
        private int maxLength;
        
        public NumericDocumentFilter(int maxLength) {
            this.maxLength = maxLength;
        }
        
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            if (isNumeric(string) && (fb.getDocument().getLength() + string.length()) <= maxLength) {
                super.insertString(fb, offset, string, attr);
            }
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            if (isNumeric(text) && (fb.getDocument().getLength() - length + text.length()) <= maxLength) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
        
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }
        
        private boolean isNumeric(String text) {
            return text.matches("\\d*");
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new CreateAccount().setVisible(true)); }
}