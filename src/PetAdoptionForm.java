import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PetAdoptionForm extends JPanel {

    // --- UI Components ---
    private JButton backButton;
    private JLabel titleLabel;
    private JTextField lastNameField, firstNameField, middleNameField, dobField, ageField;
    private JRadioButton maleRadio, femaleRadio, nonBinaryRadio, preferNotToSayRadio;
    private ButtonGroup genderGroup;
    private JTextField occupationField, emailField, contactField;
    private JTextField provinceField, cityField, barangayField, addressField;
    private JRadioButton resSingleRadio, resDuplexRadio, resCondoRadio, resApartmentRadio, resTrailerRadio;
    private ButtonGroup residencyGroup;
    private JRadioButton petsYesRadio, petsNoRadio;
    private ButtonGroup otherPetsGroup;
    private JCheckBox termsCheckbox;
    private JButton submitButton;

    private Dashboard parentDashboard;
    private int petId; // [NEW] Link to specific pet
    private String petName;
    private String currentUser = "Guest";

    // Modern Color Palette - Pink-Blue Gradient Theme
    private final Color primaryColor = new Color(219, 39, 119); // Pink
    private final Color primaryDark = new Color(190, 24, 93); // Darker Pink
    private final Color accentColor = new Color(59, 130, 246); // Blue
    private final Color dangerColor = new Color(239, 68, 68);
    private final Color dangerHover = new Color(220, 38, 38);
    private final Color successColor = new Color(34, 197, 94);
    private final Color backgroundColor = new Color(253, 242, 248); // Light Pink
    private final Color surfaceColor = Color.WHITE;
    private final Color textPrimary = new Color(17, 24, 39);
    private final Color textSecondary = new Color(107, 114, 128);
    private final Color borderColor = new Color(251, 207, 232); // Pink border
    private final Color focusColor = new Color(147, 197, 253); // Light Blue

    // [UPDATED] Constructor accepts petId
    public PetAdoptionForm(int petId, String petName) {
        this.petId = petId;
        this.petName = petName;

        setLayout(new BorderLayout());
        setBackground(backgroundColor);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(backgroundColor);
        mainContainer.add(createGradientHeader(), BorderLayout.NORTH);

        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(backgroundColor);
        formContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        formContainer.add(createSectionCard("👤 Personal Information", createPersonalInfoPanel()));
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(createSectionCard("📧 Contact & Location", createContactPanel()));
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(createSectionCard("🏠 Living Situation", createLivingPanel()));
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(createSectionCard("📋 Terms & Conditions", createTermsPanel()));
        formContainer.add(Box.createVerticalStrut(20));

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainContainer.add(scrollPane, BorderLayout.CENTER);
        mainContainer.add(createModernFooter(), BorderLayout.SOUTH);

        add(mainContainer, BorderLayout.CENTER);

        submitButton.addActionListener(e -> handleSubmit());
        backButton.addActionListener(e -> handleBack());
    }

    public void setParentDashboard(Dashboard dashboard) { this.parentDashboard = dashboard; }

    public void setCurrentUser(String user) {
        this.currentUser = user;
        preloadUserData();
    }

    private void preloadUserData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String fName="", lName="", mName="", dob="", age="", gender="";
            String email="", contact="", occup="", prov="", city="", brgy="", street="";
            String resType="", hasPets="";

            @Override
            protected Void doInBackground() {
                try (Connection conn = DBConnector.getConnection()) {
                    String userSql = "SELECT first_name, last_name, email_address, contact_number FROM user_accounts WHERE username = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                        stmt.setString(1, currentUser);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            fName = rs.getString("first_name");
                            lName = rs.getString("last_name");
                            email = rs.getString("email_address");
                            contact = rs.getString("contact_number");
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }

            @Override
            protected void done() {
                safeSetText(firstNameField, fName);
                safeSetText(lastNameField, lName);
                safeSetText(emailField, email);
                safeSetText(contactField, contact);
            }
        };
        worker.execute();
    }

    private void safeSetText(JTextField field, String text) {
        if (field != null && text != null && !text.isEmpty()) field.setText(text);
    }

    // --- MODIFIED SUBMIT LOGIC ---
    private void handleSubmit() {
        if (!validateForm()) return;

        String genderSelection = getSelectedButtonText(genderGroup);
        String residencySelection = getSelectedButtonText(residencyGroup);
        String hasPetsSelection = getSelectedButtonText(otherPetsGroup);

        // 1. Insert Application with PET ID
        String sqlApp = "INSERT INTO adoption_applications " +
                "(pet_id, applicant_name, pet_name, status, email, contact_number, address, " +
                "middle_name, dob, age, gender, occupation, province, city, barangay, residency_type, has_other_pets) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        DBDual.executeUpdateBoth(sqlApp, stmt -> {
            stmt.setInt(1, petId); // [NEW] Save ID
            stmt.setString(2, currentUser);
            stmt.setString(3, petName);
            stmt.setString(4, "Pending"); // Status
            stmt.setString(5, emailField.getText());
            stmt.setString(6, contactField.getText());
            stmt.setString(7, addressField.getText());
            stmt.setString(8, middleNameField.getText());
            stmt.setString(9, dobField.getText());
            stmt.setString(10, ageField.getText());
            stmt.setString(11, genderSelection);
            stmt.setString(12, occupationField.getText());
            stmt.setString(13, provinceField.getText());
            stmt.setString(14, cityField.getText());
            stmt.setString(15, barangayField.getText());
            stmt.setString(16, residencySelection);
            stmt.setString(17, hasPetsSelection);
        });

        // 2. Update Pet Status using PET ID (Prevent name conflict)
        String sqlUpdate = "UPDATE pets_accounts SET status = 'Pending Adoption' WHERE pet_id = ?";
        DBDual.executeUpdateBoth(sqlUpdate, stmt -> {
            stmt.setInt(1, petId); // [UPDATED] Use ID
        });

        JOptionPane.showMessageDialog(this,
                "Application Submitted Successfully!\n" +
                        "Status: PENDING REVIEW",
                "Request Sent", JOptionPane.INFORMATION_MESSAGE);

        if (parentDashboard != null) {
            parentDashboard.getPawManagementPanel().reloadPets();
            parentDashboard.activateSidebarByPanelName("PAW_PANEL");
        }
    }

    private String getSelectedButtonText(ButtonGroup group) {
        for (java.util.Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) return button.getText();
        }
        return null;
    }

    private JPanel createGradientHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 15)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Pink to Blue gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(236, 72, 153), // Bright Pink
                    getWidth(), getHeight(), new Color(59, 130, 246) // Blue
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
            }
        };
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        header.setOpaque(false);

        backButton = createModernIconButton("← BACK", dangerColor, dangerHover);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        titleLabel = new JLabel("Adopt " + petName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("Complete the form below to start your adoption journey");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(255, 255, 255, 200));
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        header.add(backButton, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createSectionCard(String title, JPanel content) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Subtle pink shadow effect
                g2d.setColor(new Color(236, 72, 153, 15));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 16, 16);
                g2d.setColor(new Color(59, 130, 246, 10));
                g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 16, 16);
            }
        };
        card.setBackground(surfaceColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16, borderColor),
                new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        // Use a font that supports emoji - Segoe UI Emoji is available on Windows
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        titleLabel.setForeground(textPrimary);
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(surfaceColor);

        // Row 1: Names
        JPanel row1 = new JPanel(new GridLayout(1, 3, 10, 0));
        row1.setBackground(surfaceColor);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row1.add(createFloatingLabelField("Last Name *", lastNameField = createStyledTextField()));
        row1.add(createFloatingLabelField("First Name *", firstNameField = createStyledTextField()));
        row1.add(createFloatingLabelField("Middle Name", middleNameField = createStyledTextField()));
        panel.add(row1);
        panel.add(Box.createVerticalStrut(10));

        // Row 2: DOB, Age
        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setBackground(surfaceColor);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row2.add(createFloatingLabelField("Date of Birth (MM/DD/YYYY)", dobField = createStyledTextField()));
        row2.add(createFloatingLabelField("Age", ageField = createStyledTextField()));
        panel.add(row2);
        panel.add(Box.createVerticalStrut(10));

        // Row 3: Gender
        genderGroup = new ButtonGroup();
        maleRadio = createChipRadio("Male");
        femaleRadio = createChipRadio("Female");
        preferNotToSayRadio = createChipRadio("Prefer Not to Say");
        JPanel genderPanel = createChipRadioPanel("Gender", genderGroup, maleRadio, femaleRadio, preferNotToSayRadio);
        genderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel.add(genderPanel);

        return panel;
    }

    private JPanel createContactPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(surfaceColor);

        // Row 1: Occupation, Email, Contact
        JPanel row1 = new JPanel(new GridLayout(1, 3, 10, 0));
        row1.setBackground(surfaceColor);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row1.add(createFloatingLabelField("Occupation", occupationField = createStyledTextField()));
        row1.add(createFloatingLabelField("Email Address *", emailField = createStyledTextField()));
        row1.add(createFloatingLabelField("Contact Number *", contactField = createStyledTextField()));
        panel.add(row1);
        panel.add(Box.createVerticalStrut(10));

        // Row 2: Province, City, Barangay
        JPanel row2 = new JPanel(new GridLayout(1, 3, 10, 0));
        row2.setBackground(surfaceColor);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row2.add(createFloatingLabelField("Province", provinceField = createStyledTextField()));
        row2.add(createFloatingLabelField("City", cityField = createStyledTextField()));
        row2.add(createFloatingLabelField("Barangay", barangayField = createStyledTextField()));
        panel.add(row2);
        panel.add(Box.createVerticalStrut(10));

        // Row 3: Street Address
        JPanel row3 = new JPanel(new BorderLayout());
        row3.setBackground(surfaceColor);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row3.add(createFloatingLabelField("Street Address", addressField = createStyledTextField()));
        panel.add(row3);

        return panel;
    }

    private JPanel createLivingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(surfaceColor);

        residencyGroup = new ButtonGroup();
        resSingleRadio = createChipRadio("Single Family");
        resDuplexRadio = createChipRadio("Duplex");
        resCondoRadio = createChipRadio("Condo");
        resApartmentRadio = createChipRadio("Apartment");
        resTrailerRadio = createChipRadio("Trailer");
        
        JPanel residencyPanel = createChipRadioPanel("Residency Type", residencyGroup, resSingleRadio, resDuplexRadio, resCondoRadio, resApartmentRadio, resTrailerRadio);
        residencyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.add(residencyPanel);
        panel.add(Box.createVerticalStrut(10));

        otherPetsGroup = new ButtonGroup();
        petsYesRadio = createChipRadio("Yes");
        petsNoRadio = createChipRadio("No");
        
        JPanel petsPanel = createChipRadioPanel("Do you have other pets?", otherPetsGroup, petsYesRadio, petsNoRadio);
        petsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel.add(petsPanel);

        return panel;
    }

    private JPanel createTermsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(surfaceColor);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextArea termsText = new JTextArea(
            "By submitting this application, you agree to:\n" +
            "• Provide a safe and loving home for the pet\n" +
            "• Cover all veterinary expenses and proper care\n" +
            "• Allow home visits if required\n" +
            "• Return the pet if unable to care for them"
        );
        termsText.setEditable(false);
        termsText.setBackground(new Color(249, 250, 251));
        termsText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        termsText.setForeground(textSecondary);
        termsText.setBorder(new EmptyBorder(10, 10, 10, 10));
        termsText.setLineWrap(true);
        termsText.setWrapStyleWord(true);
        
        p.add(termsText, BorderLayout.CENTER);
        
        termsCheckbox = new JCheckBox("I agree to the terms and conditions *");
        termsCheckbox.setBackground(surfaceColor);
        termsCheckbox.setFont(new Font("Segoe UI", Font.BOLD, 13));
        termsCheckbox.setForeground(textPrimary);
        p.add(termsCheckbox, BorderLayout.SOUTH);
        
        return p;
    }

    private JPanel createModernFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        f.setBackground(surfaceColor);
        f.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        
        submitButton = createGradientButton("SUBMIT APPLICATION");
        submitButton.setPreferredSize(new Dimension(200, 42));
        f.add(submitButton);
        return f;
    }

    private void handleBack() { if(parentDashboard!=null) parentDashboard.activateSidebarByPanelName("PAW_PANEL"); }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, borderColor),
            new EmptyBorder(10, 15, 10, 15)
        ));
        f.setBackground(new Color(255, 250, 253)); // Very light pink
        
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, new Color(236, 72, 153)), // Pink focus
                    new EmptyBorder(10, 15, 10, 15)
                ));
                f.setBackground(Color.WHITE);
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, borderColor),
                    new EmptyBorder(10, 15, 10, 15)
                ));
                f.setBackground(new Color(255, 250, 253));
            }
        });
        
        return f;
    }

    private JPanel createFloatingLabelField(String l, JTextField f) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(surfaceColor);
        
        JLabel label = new JLabel(l);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(textSecondary);
        
        p.add(label, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        return p;
    }

    private JRadioButton createChipRadio(String t) {
        JRadioButton r = new JRadioButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected()) {
                    // Pink-Blue gradient for selected state
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(236, 72, 153),
                        getWidth(), 0, new Color(59, 130, 246)
                    );
                    g2d.setPaint(gradient);
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(251, 207, 232)); // Light pink hover
                } else {
                    g2d.setColor(backgroundColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                if (isSelected()) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(textPrimary);
                }
                
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        
        r.setFont(new Font("Segoe UI", Font.BOLD, 11));
        r.setOpaque(false);
        r.setFocusPainted(false);
        r.setBorderPainted(false);
        r.setContentAreaFilled(false);
        r.setPreferredSize(new Dimension(110, 34));
        r.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return r;
    }

    private JPanel createChipRadioPanel(String l, ButtonGroup g, JRadioButton... btns) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(surfaceColor);
        
        JLabel label = new JLabel(l);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(textSecondary);
        label.setBorder(new EmptyBorder(0, 0, 8, 0));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(label);
        
        JPanel sub = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        sub.setBackground(surfaceColor);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        for (JRadioButton b : btns) {
            g.add(b);
            sub.add(b);
        }
        p.add(sub);
        return p;
    }

    private JButton createModernIconButton(String t, Color bgColor, Color hoverColor) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(hoverColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(hoverColor);
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 40));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return b;
    }

    private JButton createGradientButton(String t) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, primaryDark, getWidth(), 0, new Color(37, 99, 235));
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(219, 39, 119), getWidth(), 0, new Color(37, 99, 235));
                } else {
                    gradient = new GradientPaint(0, 0, new Color(236, 72, 153), getWidth(), 0, new Color(59, 130, 246));
                }
                
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Add subtle shadow
                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(236, 72, 153, 30));
                    g2d.fillRoundRect(0, 2, getWidth(), getHeight() - 2, 12, 12);
                }
                
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return b;
    }

    private GridBagConstraints setGbc(GridBagConstraints c, int x, int w) {
        c.gridx = x; c.gridwidth = w; return c;
    }

    private boolean validateForm() {
        if (lastNameField.getText().trim().isEmpty() || 
            firstNameField.getText().trim().isEmpty() || 
            contactField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this, 
                "Please complete all required fields marked with *", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!termsCheckbox.isSelected()) {
            JOptionPane.showMessageDialog(this, 
                "You must agree to the terms and conditions", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PetAdoptionForm::runDemo);
    }

    private static void runDemo() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* ...existing code... */ }

        // Pick any pet id/name you want for testing
        PetAdoptionForm form = new PetAdoptionForm(1, "Buddy");
        // Optional: preload data if your DB/user_accounts is set up
        // form.setCurrentUser("yourUsername");

        JFrame frame = new JFrame("Pet Adoption Form");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(form);
        frame.setSize(1000, 720);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Helper class for rounded borders
    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
    
    // Modern scrollbar UI
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(236, 72, 153, 150); // Pink thumb
            this.trackColor = new Color(253, 242, 248); // Light pink track
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Pink-blue gradient thumb
            GradientPaint gradient = new GradientPaint(
                thumbBounds.x, thumbBounds.y, new Color(236, 72, 153),
                thumbBounds.x + thumbBounds.width, thumbBounds.y + thumbBounds.height, new Color(147, 197, 253)
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, 
                             thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
        }
    }
}