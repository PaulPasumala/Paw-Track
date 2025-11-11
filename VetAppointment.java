// src/VetAppointment.java

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Date;

public class VetAppointment extends JPanel {

    // Enhanced color palette with gradients and modern colors
    private static final Color BG_COLOR = new Color(248, 250, 252); // Light blue-gray
    private static final Color SECTION_BG_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(30, 41, 59); // Slate gray
    private static final Color BORDER_COLOR = new Color(203, 213, 225); // Light slate
    private static final Color INPUT_BG_COLOR = Color.WHITE;
    private static final Color BLUE_ACCENT = new Color(59, 130, 246); // Modern blue
    private static final Color BLUE_HOVER = new Color(37, 99, 235); // Darker blue for hover
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94); // Modern green
    private static final Color GRAY_BUTTON_BG = new Color(241, 245, 249); // Very light gray
    private static final Color GRAY_HOVER = new Color(226, 232, 240); // Slightly darker gray
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 20); // Subtle shadow
    private static final Color HEADER_GRADIENT_START = new Color(99, 102, 241); // Purple
    private static final Color HEADER_GRADIENT_END = new Color(139, 92, 246); // Violet

    // Enhanced fonts with better hierarchy
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 36);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_CARD_NAME = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.ITALIC, 13);

    // Form components to be accessed for clearing
    private JTextField petNameField, petTypeField, ageBreedField;
    private JTextField ownerNameField, petWeightField, emergencyContactField;
    private JComboBox<String> vetSelector, genderComboBox;
    private JFormattedTextField dateField, contactNumberField, lastVaccinationField;
    private JSpinner timeSpinner;
    private JTextArea medicalHistoryArea, allergiesArea;

    // Add current vet index tracking
    private int currentVetIndex = 0;
    private final String[][] vetData = {
            {"Dr. Marlon Paul Agustino", "0912-345-6789", "Dr.MpA@vetelinnic.com", "Happy Paws Veterinary Center", "Brgy. San Isidro, Quezon City"},
            {"Dr. Anna Kendrick", "0923-456-7890", "anna.kendrick@vetclinic.com", "City Animal Hospital", "Makati City, Metro Manila"},
            {"Dr. Peter Jones", "0917-234-5678", "peter.jones@animalcare.ph", "Jones Animal Care Center", "BGC, Taguig City"},
            {"Dr. Peter Griffin", "0908-345-6789", "p.griffin@petmedical.com", "Griffin Pet Medical Center", "Pasig City, Metro Manila"},
            {"Dr. Sarah Connor", "0915-678-9012", "s.connor@animalhealth.ph", "Connor Animal Health Clinic", "Ortigas, Pasig City"},
            {"Dr. Michael Johnson", "0920-123-4567", "m.johnson@vetcare.ph", "Johnson Veterinary Clinic", "Alabang, Muntinlupa City"},
            {"Dr. Emma Watson", "0918-765-4321", "e.watson@animalwelfare.com", "Watson Animal Welfare Center", "Eastwood, Quezon City"},
            {"Dr. Robert Brown", "0916-987-6543", "r.brown@petclinic.ph", "Brown Pet Medical Clinic", "Rockwell, Makati City"},
            {"Dr. Lisa Garcia", "0921-234-5678", "l.garcia@vetservices.com", "Garcia Veterinary Services", "Greenhills, San Juan City"},
            {"Dr. David Wilson", "0919-876-5432", "d.wilson@animalcare.ph", "Wilson Animal Care Center", "Kapitolyo, Pasig City"}
    };

    private JPanel currentVetCard;

    public VetAppointment() {
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(24, 0));
        setBorder(new EmptyBorder(32, 32, 32, 32));

        // Enhanced header with gradient
        add(createEnhancedHeaderPanel(), BorderLayout.NORTH);

        // Main content area with two columns
        add(createMainContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createEnhancedHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(0, 0, HEADER_GRADIENT_START, getWidth(), 0, HEADER_GRADIENT_END);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Main title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel("");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 16));

        JLabel titleLabel = new JLabel("Veterinary Appointment System");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Professional Pet Healthcare Scheduling");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));
        subtitleLabel.setBorder(new EmptyBorder(8, 48, 0, 0));

        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);
        headerPanel.setBorder(new EmptyBorder(0, 0, 32, 0));

        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Left column: Form panel (increased width, aligned left)
        gbc.gridx = 0;
        gbc.weightx = 0.75;
        gbc.insets = new Insets(0, 0, 0, 16);
        contentPanel.add(createFormPanel(), gbc);

        // Right column: Vet info panel (decreased width)
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createVetInfoPanel(), gbc);

        return contentPanel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create Pet Details section with scrolling
        JPanel petDetailsPanel = createPetDetailsFields();
        JPanel petDetailsSection = createScrollableSectionPanel("Pet Details", petDetailsPanel, 300);
        panel.add(petDetailsSection);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(createSectionPanel("Appointment Details", createAppointmentDetailsFields()));

        panel.add(Box.createVerticalGlue());
        panel.add(createActionButtons());

        return panel;
    }

    private JPanel createSectionPanel(String title, JPanel fieldsPanel) {
        JPanel section = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw subtle shadow
                g2d.setColor(SHADOW_COLOR);
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);

                // Draw main background
                g2d.setColor(SECTION_BG_COLOR);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);

                g2d.dispose();
            }
        };
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        Border emptyBorder = new EmptyBorder(20, 20, 20, 20);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                emptyBorder,
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FONT_TITLE,
                BLUE_ACCENT
        );
        section.setBorder(titledBorder);
        section.add(fieldsPanel, BorderLayout.CENTER);
        return section;
    }

    private JPanel createScrollableSectionPanel(String title, JPanel fieldsPanel, int maxHeight) {
        JPanel section = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw subtle shadow
                g2d.setColor(SHADOW_COLOR);
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);

                // Draw main background
                g2d.setColor(SECTION_BG_COLOR);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);

                g2d.dispose();
            }
        };
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        Border emptyBorder = new EmptyBorder(20, 20, 20, 20);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                emptyBorder,
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FONT_TITLE,
                BLUE_ACCENT
        );
        section.setBorder(titledBorder);

        // Create enhanced scroll pane
        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setBackground(SECTION_BG_COLOR);
        scrollPane.getViewport().setBackground(SECTION_BG_COLOR);

        // Style the scroll bar
        styleScrollPane(scrollPane);
        configureScrollPaneForSmoothScrolling(scrollPane);

        scrollPane.setPreferredSize(new Dimension(0, maxHeight));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));

        section.add(scrollPane, BorderLayout.CENTER);
        return section;
    }

    private JPanel createPetDetailsFields() {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = createGbc();

        // Owner Name (first row, full width)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        ownerNameField = createTextField();
        addField(fields, gbc, "Owner Name", ownerNameField);

        // Pet Name (second row, full width)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        petNameField = createTextField();
        addField(fields, gbc, "Pet Name", petNameField);

        // Pet Type & Age/Breed (third row)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        petTypeField = createTextField();
        addField(fields, gbc, "Pet Type", petTypeField);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        ageBreedField = createTextField();
        addField(fields, gbc, "Age", ageBreedField);

        // Gender & Pet Weight (fourth row)
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        String[] genders = {"Male", "Female", "Neutered Male", "Spayed Female"};
        genderComboBox = new JComboBox<>(genders);
        styleComboBox(genderComboBox);
        addField(fields, gbc, "Gender", genderComboBox);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        petWeightField = createTextField();
        addField(fields, gbc, "Pet Weight (kg)", petWeightField);

        // Last Vaccination Date & Emergency Contact (fifth row)
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        lastVaccinationField = createDateField();
        addField(fields, gbc, "Last Vaccination", createFieldWithIcon(lastVaccinationField, "VAC"));

        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        emergencyContactField = createTextField();
        addField(fields, gbc, "Emergency Contact", emergencyContactField);

        // Medical History (sixth row, full width)
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        medicalHistoryArea = createTextArea(3);
        JScrollPane medicalHistoryScroll = new JScrollPane(medicalHistoryArea);
        medicalHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        medicalHistoryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        medicalHistoryScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        configureScrollPaneForSmoothScrolling(medicalHistoryScroll);
        addField(fields, gbc, "Medical History", medicalHistoryScroll);

        // Allergies (seventh row, full width)
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        allergiesArea = createTextArea(2);
        JScrollPane allergiesScroll = new JScrollPane(allergiesArea);
        allergiesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        allergiesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        allergiesScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        configureScrollPaneForSmoothScrolling(allergiesScroll);
        addField(fields, gbc, "Known Allergies", allergiesScroll);

        return fields;
    }

    private JPanel createAppointmentDetailsFields() {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = createGbc();

        // Select Vet & Date (first row, properly aligned)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        String[] vets = {"Dr. Marlon Paul Agustino", "Dr. Anna Kendrick", "Dr. Peter Jones", "Dr. Peter Griffin", "Dr. Sarah Connor",
                "Dr. Michael Johnson", "Dr. Emma Watson", "Dr. Robert Brown", "Dr. Lisa Garcia", "Dr. David Wilson"};
        vetSelector = new JComboBox<>(vets);
        styleComboBox(vetSelector);

        // Add action listener to synchronize vet selector with vet card
        vetSelector.addActionListener(e -> {
            currentVetIndex = vetSelector.getSelectedIndex();
            updateVetCard();
        });

        addField(fields, gbc, "Select Vet", vetSelector);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        dateField = createDateField();
        addField(fields, gbc, "Date", createFieldWithIcon(dateField, "DATE"));

        // Time & Contact Number (second row, properly aligned)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        timeSpinner = createTimeField();
        addField(fields, gbc, "Time", createFieldWithIcon(timeSpinner, "TIME"));

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        contactNumberField = createContactField();
        addField(fields, gbc, "Contact Number", contactNumberField);

        return fields;
    }

    private JPanel createActionButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton bookButton = createEnhancedButton("Book Appointment", SUCCESS_GREEN, Color.WHITE);
        bookButton.addActionListener(e -> {
            showSuccessDialog();
        });

        JButton clearButton = createEnhancedButton("Clear Form", GRAY_BUTTON_BG, TEXT_COLOR);
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(bookButton);
        buttonPanel.add(clearButton);
        return buttonPanel;
    }

    private JButton createEnhancedButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2d.setColor(SHADOW_COLOR);
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 8, 8);

                // Draw button background
                Color currentBg = getBackground();
                if (getModel().isPressed()) {
                    currentBg = currentBg.darker();
                } else if (getModel().isRollover()) {
                    if (currentBg.equals(SUCCESS_GREEN)) {
                        currentBg = SUCCESS_GREEN.brighter();
                    } else if (currentBg.equals(GRAY_BUTTON_BG)) {
                        currentBg = GRAY_HOVER;
                    } else {
                        currentBg = BLUE_HOVER;
                    }
                }

                g2d.setColor(currentBg);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 8, 8);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    private void showSuccessDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Appointment Confirmed", true);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(32, 32, 32, 32));

        JLabel successIcon = new JLabel("SUCCESS");
        successIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        successIcon.setForeground(SUCCESS_GREEN);
        successIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Appointment Successfully Booked!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(SUCCESS_GREEN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("You will receive a confirmation email shortly.");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = createEnhancedButton("OK", BLUE_ACCENT, Color.WHITE);
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> dialog.dispose());

        contentPanel.add(successIcon);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 24)));
        contentPanel.add(okButton);

        dialog.add(contentPanel);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createVetInfoPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw subtle shadow
                g2d.setColor(SHADOW_COLOR);
                g2d.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 16, 16);

                // Draw main background
                g2d.setColor(SECTION_BG_COLOR);
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 16, 16);

                g2d.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Create initial vet card
        currentVetCard = createEnhancedVetCard(
                vetData[currentVetIndex][0], vetData[currentVetIndex][1], vetData[currentVetIndex][2],
                vetData[currentVetIndex][3], vetData[currentVetIndex][4]
        );
        panel.add(currentVetCard);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Enhanced navigation buttons
        JPanel navigationPanel = createEnhancedNavigationButtons();
        panel.add(navigationPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Enhanced Google Maps container
        JPanel mapsContainer = createEnhancedGoogleMapsContainer();
        panel.add(mapsContainer);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JButton createNavigationButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = getModel().isRollover() ? BLUE_HOVER : BLUE_ACCENT;
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                super.paintComponent(g);
                g2d.dispose();
            }
        };

        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(85, 32));

        return button;
    }

    private JPanel createEnhancedNavigationButtons() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        navPanel.setOpaque(false);
        navPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton previousButton = createNavigationButton("< Previous");
        previousButton.addActionListener(e -> showPreviousVet());

        JButton nextButton = createNavigationButton("Next >");
        nextButton.addActionListener(e -> showNextVet());

        // Enhanced vet counter
        JLabel vetCounterLabel = new JLabel((currentVetIndex + 1) + "/" + vetData.length) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(BLUE_ACCENT);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                super.paintComponent(g);
                g2d.dispose();
            }
        };
        vetCounterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vetCounterLabel.setForeground(Color.WHITE);
        vetCounterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        vetCounterLabel.setPreferredSize(new Dimension(50, 32));
        vetCounterLabel.setOpaque(false);
        vetCounterLabel.setName("vetCounter");

        navPanel.add(previousButton);
        navPanel.add(vetCounterLabel);
        navPanel.add(nextButton);

        return navPanel;
    }

    private JPanel createEnhancedGoogleMapsContainer() {
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(248, 250, 252), 0, getHeight(), Color.WHITE);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2d.dispose();
            }
        };
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Enhanced header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);

        JLabel mapIconLabel = new JLabel("MAP");
        mapIconLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mapIconLabel.setForeground(BLUE_ACCENT);

        JLabel titleLabel = new JLabel("  Clinic Location");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);

        headerPanel.add(mapIconLabel);
        headerPanel.add(titleLabel);
        container.add(headerPanel);
        container.add(Box.createRigidArea(new Dimension(0, 12)));

        // Enhanced map display area
        JPanel mapDisplayArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw map-like background
                g2d.setColor(new Color(230, 245, 255));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Draw grid pattern
                g2d.setColor(new Color(200, 220, 240));
                for (int i = 0; i < getWidth(); i += 20) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 20) {
                    g2d.drawLine(0, i, getWidth(), i);
                }

                g2d.dispose();
            }
        };
        mapDisplayArea.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        mapDisplayArea.setPreferredSize(new Dimension(0, 300));
        mapDisplayArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        mapDisplayArea.setLayout(new BorderLayout());

        // Enhanced placeholder
        JPanel placeholderPanel = new JPanel();
        placeholderPanel.setOpaque(false);
        placeholderPanel.setLayout(new BoxLayout(placeholderPanel, BoxLayout.Y_AXIS));

        JLabel mapIcon = new JLabel("CLINIC");
        mapIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mapIcon.setForeground(BLUE_ACCENT);
        mapIcon.setHorizontalAlignment(SwingConstants.CENTER);
        mapIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel placeholderLabel = new JLabel("Interactive Map View");
        placeholderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        placeholderLabel.setForeground(BLUE_ACCENT);
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Location will be displayed here");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(120, 120, 120));
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        placeholderPanel.add(Box.createVerticalGlue());
        placeholderPanel.add(mapIcon);
        placeholderPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        placeholderPanel.add(placeholderLabel);
        placeholderPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        placeholderPanel.add(subLabel);
        placeholderPanel.add(Box.createVerticalGlue());

        mapDisplayArea.add(placeholderPanel, BorderLayout.CENTER);
        container.add(mapDisplayArea);

        return container;
    }

    private JPanel createEnhancedVetCard(String name, String phone, String email, String clinic, String address) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 16, 16);

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(248, 250, 252));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 16, 16);

                // Draw border
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 16, 16);

                g2d.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Enhanced vet name with professional styling
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(FONT_CARD_NAME);
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel ratingLabel = new JLabel("5 STARS");
        ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        ratingLabel.setForeground(new Color(255, 193, 7));

        headerPanel.add(nameLabel, BorderLayout.CENTER);
        headerPanel.add(ratingLabel, BorderLayout.EAST);
        card.add(headerPanel);

        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // Professional credentials with better styling
        JLabel dvmLabel = new JLabel("DVM, Veterinary Medicine");
        dvmLabel.setFont(FONT_CARD_TITLE);
        dvmLabel.setForeground(BLUE_ACCENT);
        dvmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dvmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(dvmLabel);

        JLabel licenseLabel = new JLabel("Licensed Veterinarian - PRC #12345");
        licenseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        licenseLabel.setForeground(new Color(100, 100, 100));
        licenseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        licenseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(licenseLabel);

        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // Enhanced two-column layout with icons
        JPanel twoColumnPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        twoColumnPanel.setOpaque(false);

        // Left column with icons
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);

        leftColumn.add(createIconLabel("SPEC", "Specializations:", FONT_LABEL));
        leftColumn.add(createBulletPoint("Small Animal Medicine"));
        leftColumn.add(createBulletPoint("Emergency Care"));
        leftColumn.add(createBulletPoint("Preventive Medicine"));
        leftColumn.add(Box.createRigidArea(new Dimension(0, 8)));

        leftColumn.add(createIconLabel("EXP", "Experience:", FONT_LABEL));
        leftColumn.add(createBulletPoint("8+ Years Practice"));
        leftColumn.add(createBulletPoint("UP Graduate"));

        // Right column with icons
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);

        rightColumn.add(createIconLabel("TEL", "Contact:", FONT_LABEL));
        rightColumn.add(createContactLabel(phone));
        rightColumn.add(createContactLabel(email));
        rightColumn.add(Box.createRigidArea(new Dimension(0, 8)));

        rightColumn.add(createIconLabel("HRS", "Hours:", FONT_LABEL));
        rightColumn.add(createContactLabel("Mon-Fri: 8AM-6PM"));
        rightColumn.add(createContactLabel("Sat: 8AM-4PM"));
        rightColumn.add(createContactLabel("Sun: Emergency Only"));

        twoColumnPanel.add(leftColumn);
        twoColumnPanel.add(rightColumn);
        card.add(twoColumnPanel);

        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // Enhanced clinic information
        JPanel clinicPanel = new JPanel();
        clinicPanel.setLayout(new BoxLayout(clinicPanel, BoxLayout.Y_AXIS));
        clinicPanel.setOpaque(false);

        JLabel clinicLabel = new JLabel(clinic);
        clinicLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clinicLabel.setForeground(TEXT_COLOR);
        clinicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel addressLabel = new JLabel("LOC: " + address);
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addressLabel.setForeground(new Color(100, 100, 100));
        addressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        clinicPanel.add(clinicLabel);
        clinicPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        clinicPanel.add(addressLabel);
        card.add(clinicPanel);

        return card;
    }

    private JLabel createIconLabel(String icon, String text, Font font) {
        JLabel label = new JLabel("[" + icon + "] " + text);
        label.setFont(font);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createBulletPoint(String text) {
        JLabel label = new JLabel("• " + text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createContactLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(new Color(100, 100, 100));
        return label;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 2, 2, 2);
        return gbc;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, Component component) {
        int currentGridX = gbc.gridx;
        int currentGridY = gbc.gridy;
        int currentGridWidth = gbc.gridwidth;

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(FONT_LABEL);
        jLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.insets = new Insets(2, 4, 2, 4);
        panel.add(jLabel, gbc);

        gbc.gridy = currentGridY + 1;
        gbc.insets = new Insets(0, 4, 12, 4);
        panel.add(component, gbc);

        gbc.gridx = currentGridX;
        gbc.gridy = currentGridY;
        gbc.gridwidth = currentGridWidth;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                super.paintComponent(g);
                g2d.dispose();
            }
        };
        textField.setFont(FONT_INPUT);
        textField.setBackground(INPUT_BG_COLOR);
        textField.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        textField.setOpaque(false);
        return textField;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea textArea = new JTextArea(rows, 0);
        textArea.setFont(FONT_INPUT);
        textArea.setBackground(INPUT_BG_COLOR);
        textArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private JFormattedTextField createDateField() {
        try {
            MaskFormatter formatter = new MaskFormatter("##/##/####");
            formatter.setPlaceholderCharacter('_');
            formatter.setValidCharacters("0123456789");
            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setToolTipText("MM/DD/YYYY");
            styleTextField(field, false);
            return field;
        } catch (ParseException e) { return new JFormattedTextField("Date Error"); }
    }

    private JSpinner createTimeField() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinner, "hh:mm a");
        spinner.setEditor(timeEditor);
        spinner.setFont(FONT_INPUT);
        JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        tf.setBackground(INPUT_BG_COLOR);
        tf.setBorder(new EmptyBorder(8, 10, 8, 10));
        spinner.setBorder(null);
        return spinner;
    }

    private JPanel createFieldWithIcon(Component field, String icon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(INPUT_BG_COLOR);
        panel.setBorder(new LineBorder(BORDER_COLOR, 1));

        JLabel iconLabel = new JLabel("[" + icon + "]");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        iconLabel.setForeground(BLUE_ACCENT);
        iconLabel.setBorder(new EmptyBorder(0, 8, 0, 8));

        panel.add(field, BorderLayout.CENTER);
        panel.add(iconLabel, BorderLayout.EAST);
        return panel;
    }

    private JFormattedTextField createContactField() {
        try {
            MaskFormatter formatter = new MaskFormatter("+63 ###-###-####");
            formatter.setPlaceholderCharacter('_');
            JFormattedTextField field = new JFormattedTextField(formatter);
            styleTextField(field, true);
            return field;
        } catch (ParseException e) { return new JFormattedTextField("Phone Error"); }
    }

    private void styleTextField(JFormattedTextField field, boolean hasIcon) {
        field.setFont(FONT_INPUT);
        field.setBackground(INPUT_BG_COLOR);
        if (!hasIcon) {
            field.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(8, 8, 8, 8)));
        } else {
            field.setBorder(new EmptyBorder(8, 8, 8, 8));
        }
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(FONT_INPUT);
        comboBox.setBackground(INPUT_BG_COLOR);
        comboBox.setBorder(new EmptyBorder(6, 4, 6, 4));
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configureScrollPaneForSmoothScrolling(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setBackground(SECTION_BG_COLOR);
        scrollPane.getVerticalScrollBar().setForeground(BORDER_COLOR);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Vet Appointment");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new VetAppointment());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void updateVetCard() {
        Container parent = currentVetCard.getParent();
        if (parent != null) {
            parent.remove(currentVetCard);

            currentVetCard = createEnhancedVetCard(
                    vetData[currentVetIndex][0],
                    vetData[currentVetIndex][1],
                    vetData[currentVetIndex][2],
                    vetData[currentVetIndex][3],
                    vetData[currentVetIndex][4]
            );

            parent.add(currentVetCard, 0);
            vetSelector.setSelectedIndex(currentVetIndex);
            updateVetCounter(parent);

            parent.revalidate();
            parent.repaint();
        }
    }

    private void showPreviousVet() {
        currentVetIndex--;
        if (currentVetIndex < 0) {
            currentVetIndex = vetData.length - 1; // Wrap to last vet
        }
        updateVetCard();
    }

    private void showNextVet() {
        currentVetIndex++;
        if (currentVetIndex >= vetData.length) {
            currentVetIndex = 0; // Wrap to first vet
        }
        updateVetCard();
    }

    private void updateVetCounter(Container parent) {
        // Find and update the counter label
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component innerComp : panel.getComponents()) {
                    if (innerComp instanceof JLabel && "vetCounter".equals(innerComp.getName())) {
                        ((JLabel) innerComp).setText((currentVetIndex + 1) + "/" + vetData.length);
                        break;
                    }
                }
            }
        }
    }

    private void clearForm() {
        petNameField.setText("");
        petTypeField.setText("");
        ageBreedField.setText("");
        vetSelector.setSelectedIndex(0);
        dateField.setValue(null);
        timeSpinner.setValue(new Date());
        contactNumberField.setValue(null);
        ownerNameField.setText("");
        petWeightField.setText("");
        emergencyContactField.setText("");
        genderComboBox.setSelectedIndex(0);
        lastVaccinationField.setValue(null);
        medicalHistoryArea.setText("");
        allergiesArea.setText("");
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setBackground(new Color(240, 240, 240));
        scrollPane.getVerticalScrollBar().setForeground(BLUE_ACCENT);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BLUE_ACCENT;
                this.trackColor = new Color(240, 240, 240);
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
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }
}