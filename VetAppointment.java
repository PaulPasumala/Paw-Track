import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;

public class VetAppointment extends JPanel {

    // Define colors and fonts to match the HTML design
    private static final Color BG_COLOR = new Color(248, 250, 252); // bg-gray-50
    private static final Color SECTION_BG_COLOR = new Color(243, 244, 246); // bg-gray-100
    private static final Color TEXT_COLOR = new Color(17, 24, 39); // text-gray-900
    private static final Color BORDER_COLOR = new Color(229, 231, 235); // border-gray-200
    private static final Color INPUT_BG_COLOR = Color.WHITE;
    private static final Color BLUE_ACCENT = new Color(59, 130, 246); // bg-blue-600
    private static final Color GRAY_BUTTON_BG = new Color(209, 213, 219); // bg-gray-300

    private static final Font FONT_HEADER = new Font("Inter", Font.BOLD, 36);
    private static final Font FONT_TITLE = new Font("Inter", Font.BOLD, 20);
    private static final Font FONT_LABEL = new Font("Inter", Font.PLAIN, 14);
    private static final Font FONT_INPUT = new Font("Inter", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Inter", Font.BOLD, 14);

    // Form components to be accessed for clearing
    private JTextField petNameField, petTypeField, ageBreedField;
    private JTextField petIdField, ownerNameField, petWeightField, emergencyContactField;
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
        setLayout(new BorderLayout(32, 0));
        setBorder(new EmptyBorder(32, 32, 32, 32));

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main content area with two columns
        add(createMainContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(BG_COLOR);
        
        JLabel titleLabel = new JLabel("Vet Appointment");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(TEXT_COLOR);
        
        headerPanel.add(titleLabel);
        headerPanel.setBorder(new EmptyBorder(0, 0, 24, 0));
        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Force alignment to northwest (left-top)

        // Left column: Form panel (increased width, aligned left)
        gbc.gridx = 0;
        gbc.weightx = 0.75; // Increased width
        gbc.insets = new Insets(0, 0, 0, 16);
        contentPanel.add(createFormPanel(), gbc);

        // Right column: Vet info panel (decreased width)
        gbc.gridx = 1;
        gbc.weightx = 0.25; // Decreased width
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createVetInfoPanel(), gbc);
        
        return contentPanel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT); // Force left alignment

        // Create Pet Details section with scrolling
        JPanel petDetailsPanel = createPetDetailsFields();
        JPanel petDetailsSection = createScrollableSectionPanel("Pet Details", petDetailsPanel, 300);
        panel.add(petDetailsSection);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Reduced spacing
        panel.add(createSectionPanel("Appointment Details", createAppointmentDetailsFields()));
        
        panel.add(Box.createVerticalGlue()); 
        panel.add(createActionButtons());

        return panel;
    }

    private JPanel createSectionPanel(String title, JPanel fieldsPanel) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(SECTION_BG_COLOR);
        section.setAlignmentX(Component.LEFT_ALIGNMENT); // Force left alignment
        
        Border lineBorder = new LineBorder(BORDER_COLOR, 1);
        Border emptyBorder = new EmptyBorder(16, 16, 16, 16); // Reduced padding

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            new CompoundBorder(lineBorder, emptyBorder),
            title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            FONT_TITLE,
            TEXT_COLOR
        );
        section.setBorder(titledBorder);
        section.add(fieldsPanel, BorderLayout.CENTER);
        return section;
    }

    private JPanel createScrollableSectionPanel(String title, JPanel fieldsPanel, int maxHeight) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(SECTION_BG_COLOR);
        section.setAlignmentX(Component.LEFT_ALIGNMENT); // Force left alignment
        
        Border lineBorder = new LineBorder(BORDER_COLOR, 1);
        Border emptyBorder = new EmptyBorder(16, 16, 16, 16); // Reduced padding

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            new CompoundBorder(lineBorder, emptyBorder),
            title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            FONT_TITLE,
            TEXT_COLOR
        );
        section.setBorder(titledBorder);
        
        // Create scroll pane for the fields
        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null); // Remove border as it's handled by the section
        scrollPane.setBackground(SECTION_BG_COLOR);
        scrollPane.getViewport().setBackground(SECTION_BG_COLOR);
        
        // Configure smooth scrolling
        configureScrollPaneForSmoothScrolling(scrollPane);
        
        // Set preferred size to control when scrolling kicks in
        scrollPane.setPreferredSize(new Dimension(0, maxHeight));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        
        section.add(scrollPane, BorderLayout.CENTER);
        return section;
    }

    private JPanel createPetDetailsFields() {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = createGbc();

        // Pet ID & Owner Name (first row)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        petIdField = createTextField();
        addField(fields, gbc, "Pet ID", petIdField);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
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
        addField(fields, gbc, "Age/Breed", ageBreedField);

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
        addField(fields, gbc, "Last Vaccination", createFieldWithIcon(lastVaccinationField, "💉"));
        
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
        // Configure smooth scrolling for medical history
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
        // Configure smooth scrolling for allergies
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
        gbc.gridy = 0; // Same row as select vet
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        dateField = createDateField();
        addField(fields, gbc, "Date", createFieldWithIcon(dateField, "📅"));

        // Time & Contact Number (second row, properly aligned)
        gbc.gridx = 0;
        gbc.gridy = 2; // Skip past the first row
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        timeSpinner = createTimeField();
        addField(fields, gbc, "Time", createFieldWithIcon(timeSpinner, "🕒"));
        
        gbc.gridx = 1;
        gbc.gridy = 2; // Same row as time
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        contactNumberField = createContactField();
        addField(fields, gbc, "Contact Number", contactNumberField);
        
        return fields;
    }
    
    private JPanel createActionButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); // Reduced gap
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(16, 0, 0, 0)); // Reduced top margin
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Force left alignment

        JButton bookButton = new JButton("Book Appointment");
        styleButton(bookButton, BLUE_ACCENT, Color.WHITE);
        bookButton.addActionListener(e -> {
            // Simulate booking success
            JOptionPane.showMessageDialog(this, 
                "Your appointment has been successfully scheduled.",
                "Appointment Booked!",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton clearButton = new JButton("Clear Form");
        styleButton(clearButton, GRAY_BUTTON_BG, TEXT_COLOR);
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(bookButton);
        buttonPanel.add(clearButton);
        return buttonPanel;
    }

    private JPanel createVetInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SECTION_BG_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // Create initial vet card
        currentVetCard = createVetCard(
            vetData[currentVetIndex][0], vetData[currentVetIndex][1], vetData[currentVetIndex][2],
            vetData[currentVetIndex][3], vetData[currentVetIndex][4]
        );
        panel.add(currentVetCard);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // Navigation buttons panel
        JPanel navigationPanel = createNavigationButtons();
        panel.add(navigationPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // Google Maps container
        JPanel mapsContainer = createGoogleMapsContainer();
        panel.add(mapsContainer);
        
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createGoogleMapsContainer() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(INPUT_BG_COLOR);
        container.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header with map icon and title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        
        JLabel mapIconLabel = new JLabel("🗺️");
        mapIconLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        
        JLabel titleLabel = new JLabel(" Clinic Location");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_COLOR);
        
        headerPanel.add(mapIconLabel);
        headerPanel.add(titleLabel);
        container.add(headerPanel);
        container.add(Box.createRigidArea(new Dimension(0, 12)));

        // Map display area (rectangular shape)
        JPanel mapDisplayArea = new JPanel();
        mapDisplayArea.setBackground(new Color(245, 245, 245));
        mapDisplayArea.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        mapDisplayArea.setPreferredSize(new Dimension(0, 350)); // Increased height for rectangle
        mapDisplayArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        mapDisplayArea.setLayout(new BorderLayout());
        
        // Placeholder content
        JLabel placeholderLabel = new JLabel("<html><center>🗺️<br><br>Interactive Map<br>Clinic Location Display</center></html>");
        placeholderLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        placeholderLabel.setForeground(new Color(100, 100, 100));
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        mapDisplayArea.add(placeholderLabel, BorderLayout.CENTER);
        container.add(mapDisplayArea);

        return container;
    }


    private JPanel createNavigationButtons() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navPanel.setBackground(SECTION_BG_COLOR);
        navPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton previousButton = new JButton("◀ Previous");
        styleNavigationButton(previousButton);
        previousButton.addActionListener(e -> showPreviousVet());

        JButton nextButton = new JButton("Next ▶");
        styleNavigationButton(nextButton);
        nextButton.addActionListener(e -> showNextVet());

        // Vet counter label
        JLabel vetCounterLabel = new JLabel((currentVetIndex + 1) + "/" + vetData.length);
        vetCounterLabel.setFont(new Font("Inter", Font.BOLD, 12));
        vetCounterLabel.setForeground(TEXT_COLOR);
        vetCounterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        vetCounterLabel.setPreferredSize(new Dimension(40, 30));

        navPanel.add(previousButton);
        navPanel.add(vetCounterLabel);
        navPanel.add(nextButton);

        return navPanel;
    }

    private void styleNavigationButton(JButton button) {
        button.setBackground(BLUE_ACCENT);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Inter", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 30));
    }

    private void showNextVet() {
        currentVetIndex = (currentVetIndex + 1) % vetData.length;
        updateVetCard();
        // Update the vet selector to match
        vetSelector.setSelectedIndex(currentVetIndex);
    }

    private void showPreviousVet() {
        currentVetIndex = (currentVetIndex - 1 + vetData.length) % vetData.length;
        updateVetCard();
        // Update the vet selector to match
        vetSelector.setSelectedIndex(currentVetIndex);
    }

    private void updateVetCard() {
        // Get the parent container of the current vet card
        Container parent = currentVetCard.getParent();
        
        // Remove the old card
        parent.remove(currentVetCard);
        
        // Create new card with current vet data
        currentVetCard = createVetCard(
            vetData[currentVetIndex][0], vetData[currentVetIndex][1], vetData[currentVetIndex][2],
            vetData[currentVetIndex][3], vetData[currentVetIndex][4]
        );
        
        // Add the new card at the same position (index 0)
        parent.add(currentVetCard, 0);
        
        // Update the counter label
        updateCounterLabel();
        
        // Refresh the display
        parent.revalidate();
        parent.repaint();
    }

    private void updateCounterLabel() {
        // Find and update the counter label in navigation panel
        Container vetInfoPanel = currentVetCard.getParent();
        for (Component comp : vetInfoPanel.getComponents()) {
            if (comp instanceof JPanel panel) {
                for (Component navComp : panel.getComponents()) {
                    if (navComp instanceof JLabel label) {
                        if (label.getText().contains("/")) {
                            label.setText((currentVetIndex + 1) + "/" + vetData.length);
                            break;
                        }
                    }
                }
            }
        }
    }

    private JPanel createVetCard(String name, String phone, String email, String clinic, String address) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(INPUT_BG_COLOR);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(16, 16, 16, 16)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        // Vet name centered with rating on the right
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel nameLabel = createCardLabel(name, FONT_TITLE);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel ratingLabel = createCardLabel("⭐ 4.9/5.0", new Font("Inter", Font.BOLD, 12));
        ratingLabel.setForeground(new Color(34, 197, 94));
        
        headerPanel.add(nameLabel, BorderLayout.CENTER);
        headerPanel.add(ratingLabel, BorderLayout.EAST);
        card.add(headerPanel);
        
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Professional credentials (centered)
        JLabel dvmLabel = createCardLabel("DVM, Veterinary Medicine", new Font("Inter", Font.ITALIC, 12));
        dvmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dvmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(dvmLabel);
        
        JLabel licenseLabel = createCardLabel("Licensed Veterinarian - PRC #12345", new Font("Inter", Font.PLAIN, 11));
        licenseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        licenseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(licenseLabel);
        
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Two-column layout for information
        JPanel twoColumnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        twoColumnPanel.setOpaque(false);
        
        // Left column
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);
        
        // Specializations
        leftColumn.add(createCardLabel("🎯 Specializations:", new Font("Inter", Font.BOLD, 12)));
        leftColumn.add(createCardLabel("• Small Animal Medicine", new Font("Inter", Font.PLAIN, 10)));
        leftColumn.add(createCardLabel("• Emergency Care", new Font("Inter", Font.PLAIN, 10)));
        leftColumn.add(createCardLabel("• Preventive Medicine", new Font("Inter", Font.PLAIN, 10)));
        leftColumn.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Experience
        leftColumn.add(createCardLabel("👨‍⚕️ Experience:", new Font("Inter", Font.BOLD, 12)));
        leftColumn.add(createCardLabel("8+ Years Practice", new Font("Inter", Font.PLAIN, 11)));
        leftColumn.add(createCardLabel("UP Graduate", new Font("Inter", Font.PLAIN, 11)));
        
        // Right column
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);
        
        // Contact information
        rightColumn.add(createCardLabel("📞 Contact:", new Font("Inter", Font.BOLD, 12)));
        rightColumn.add(createCardLabel(phone, new Font("Inter", Font.PLAIN, 11)));
        rightColumn.add(createCardLabel(email, new Font("Inter", Font.PLAIN, 10)));
        rightColumn.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Operating hours
        rightColumn.add(createCardLabel("🕒 Hours:", new Font("Inter", Font.BOLD, 12)));
        rightColumn.add(createCardLabel("Mon-Fri: 8AM-6PM", new Font("Inter", Font.PLAIN, 11)));
        rightColumn.add(createCardLabel("Sat: 8AM-4PM", new Font("Inter", Font.PLAIN, 11)));
        rightColumn.add(createCardLabel("Sun: Emergency Only", new Font("Inter", Font.PLAIN, 11)));
        
        twoColumnPanel.add(leftColumn);
        twoColumnPanel.add(rightColumn);
        card.add(twoColumnPanel);
        
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Clinic information (centered, full width)
        JPanel clinicPanel = new JPanel();
        clinicPanel.setLayout(new BoxLayout(clinicPanel, BoxLayout.Y_AXIS));
        clinicPanel.setOpaque(false);
        
        JLabel clinicLabel = createCardLabel(clinic, new Font("Inter", Font.BOLD, 14));
        clinicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel addressLabel = createCardLabel("📍 " + address, FONT_LABEL);
        addressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        clinicPanel.add(clinicLabel);
        clinicPanel.add(addressLabel);
        card.add(clinicPanel);
        
        return card;
    }

    private void clearForm() {
        // Clear existing fields
        petNameField.setText("");
        petTypeField.setText("");
        ageBreedField.setText("");
        vetSelector.setSelectedIndex(0);
        dateField.setValue(null); // Clears the formatted text field
        timeSpinner.setValue(new Date()); // Reset time to current
        contactNumberField.setValue(null);
        
        // Clear new pet detail fields
        petIdField.setText("");
        ownerNameField.setText("");
        petWeightField.setText("");
        emergencyContactField.setText("");
        genderComboBox.setSelectedIndex(0);
        lastVaccinationField.setValue(null);
        medicalHistoryArea.setText("");
        allergiesArea.setText("");
    }


    
    private JLabel createCardLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Force northwest alignment (left-top)
        gbc.insets = new Insets(2, 2, 2, 2); // Reduced insets for tighter layout
        return gbc;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, Component component) {
        // Use the provided grid position (don't auto-calculate)
        int currentGridX = gbc.gridx;
        int currentGridY = gbc.gridy;
        int currentGridWidth = gbc.gridwidth;

        // Add label
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(FONT_LABEL);
        jLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.insets = new Insets(2, 4, 2, 4);
        panel.add(jLabel, gbc);
        
        // Add component below the label
        gbc.gridy = currentGridY + 1;
        gbc.insets = new Insets(0, 4, 12, 4);
        panel.add(component, gbc);

        // Restore original grid settings for next use
        gbc.gridx = currentGridX;
        gbc.gridy = currentGridY;
        gbc.gridwidth = currentGridWidth;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(FONT_INPUT);
        textField.setBackground(INPUT_BG_COLOR);
        textField.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(10, 10, 10, 10)));
        return textField;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea textArea = new JTextArea(rows, 0);
        textArea.setFont(FONT_INPUT);
        textArea.setBackground(INPUT_BG_COLOR);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private JFormattedTextField createDateField() {
        try {
            MaskFormatter formatter = new MaskFormatter("####-##-##");
            formatter.setPlaceholderCharacter(' ');
            JFormattedTextField field = new JFormattedTextField(formatter);
            styleTextField(field, false);
            return field;
        } catch (ParseException e) { return new JFormattedTextField("Date Error"); }
    }

    private JSpinner createTimeField() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinner, "hh:mm a");
        spinner.setEditor(timeEditor);
        spinner.setFont(FONT_INPUT);
        // Style the inner text field
        JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        tf.setBackground(INPUT_BG_COLOR);
        tf.setBorder(new EmptyBorder(8, 10, 8, 10));
        // Remove spinner's own border to fit inside the icon panel
        spinner.setBorder(null);
        return spinner;
    }
    
    private JPanel createFieldWithIcon(Component field, String icon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(INPUT_BG_COLOR);
        panel.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(FONT_LABEL);
        iconLabel.setBorder(new EmptyBorder(0, 8, 0, 8));
        
        panel.add(field, BorderLayout.CENTER);
        panel.add(iconLabel, BorderLayout.EAST);
        return panel;
    }
    
    private JFormattedTextField createContactField() {
        try {
            MaskFormatter formatter = new MaskFormatter("+#-###-###-####");
            formatter.setPlaceholderCharacter(' ');
            JFormattedTextField field = new JFormattedTextField(formatter);
            styleTextField(field, true);
            return field;
        } catch (ParseException e) { return new JFormattedTextField("Phone Error"); }
    }

    private void styleTextField(JFormattedTextField field, boolean hasIcon) {
        field.setFont(FONT_INPUT);
        field.setBackground(INPUT_BG_COLOR);
        // The border will be on the parent panel if there's an icon
        if (!hasIcon) {
            field.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(10, 10, 10, 10)));
        } else {
             field.setBorder(new EmptyBorder(10, 10, 10, 10));
        }
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(FONT_INPUT);
        comboBox.setBackground(INPUT_BG_COLOR);
        comboBox.setBorder(new EmptyBorder(8, 5, 8, 5));
    }
    
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configureScrollPaneForSmoothScrolling(JScrollPane scrollPane) {
        // Configure smooth scrolling behavior
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scroll increment
        scrollPane.getVerticalScrollBar().setBlockIncrement(64); // Page scroll increment
        scrollPane.setWheelScrollingEnabled(true); // Enable mouse wheel scrolling
        
        // Optional: Style the scroll bar for better appearance
        scrollPane.getVerticalScrollBar().setBackground(SECTION_BG_COLOR);
        scrollPane.getVerticalScrollBar().setForeground(BORDER_COLOR);
    }

    // Main method to allow running this panel as a standalone application
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
}
