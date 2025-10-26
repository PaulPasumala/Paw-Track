import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * A Java Swing application that provides a modern GUI for a pet adoption form.
 * This application is a direct conversion of the provided HTML/CSS/JS form.
 * It includes logic for conditional visibility, automatic age calculation, and enhanced styling.
 */
public class PetAdoptionForm extends JFrame {

    // --- Style Constants ---
    private static final Color COLOR_BACKGROUND = new Color(244, 247, 246);
    private static final Color COLOR_PANEL_BACKGROUND = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(52, 152, 219);
    private static final Color COLOR_PRIMARY_DARK = new Color(41, 128, 185);
    private static final Color COLOR_SECONDARY = new Color(128, 140, 153);
    private static final Color COLOR_SECONDARY_DARK = new Color(149, 165, 166);
    private static final Color COLOR_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_BORDER = new Color(204, 204, 204);
    private static final Color COLOR_BORDER_FOCUS = new Color(52, 152, 219);
    private static final Font FONT_TITLE = new Font("Inter", Font.BOLD, 28);
    private static final Font FONT_SUBTITLE = new Font("Inter", Font.PLAIN, 14);
    private static final Font FONT_LEGEND = new Font("Inter", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Inter", Font.PLAIN, 13);
    private static final Font FONT_INPUT = new Font("Inter", Font.PLAIN, 14);

    // --- Component Declarations ---
    private JTextField lastNameField, firstNameField, middleNameField;
    private JFormattedTextField birthdateField;
    private JTextField ageField, occupationField;
    private JRadioButton maleRadio, femaleRadio, nonBinaryRadio, preferNotToSayRadio;
    private JTextField emailField, phoneField, addressField;
    private JRadioButton homeRadio, duplexRadio, condoRadio, trailerRadio, apartmentRadio;
    private JRadioButton petsYesRadio, petsNoRadio;
    private JPanel currentPetSection;
    private JTextField petNameField;
    private JTextArea petInteractionArea;
    private JTextField signatureField;
    private JButton submitButton, backButton;
    private ButtonGroup sexGroup, residenceGroup, hasPetsGroup;
    private JFrame parentFrame;
    private String selectedPetName;


    public PetAdoptionForm() {
        this(null, null);
    }

    public PetAdoptionForm(JFrame parent, String petName) {
        super("Pet Adoption Application" + (petName != null ? " - " + petName : ""));
        this.parentFrame = parent;
        this.selectedPetName = petName;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
        }

        // Fix window closing behavior based on whether there's a parent
        if (parentFrame != null) {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    parentFrame.setVisible(true);
                    parentFrame.toFront();
                }
            });
        } else {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(COLOR_BACKGROUND);

        GridBagConstraints gbc = createGbc(0, 0);
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 10, 5);

        JLabel titleLabel = new JLabel("Pet Adoption Application", SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_TEXT);
        mainPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 25, 5);
        JLabel subtitleLabel = new JLabel("Thank you for your interest in giving a deserving animal a forever home.", SwingConstants.CENTER);
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(Color.DARK_GRAY);
        mainPanel.add(subtitleLabel, gbc);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridy++;
        JPanel aboutYouPanel = createTitledPanel("👤 Section 1: About You");
        setupAboutYouPanel(aboutYouPanel);
        mainPanel.add(aboutYouPanel, gbc);
        
        gbc.gridy++;
        JPanel contactPanel = createTitledPanel("📞 Section 2: Contact Information");
        setupContactPanel(contactPanel);
        mainPanel.add(contactPanel, gbc);

        gbc.gridy++;
        JPanel homePanel = createTitledPanel("🏡 Section 3: Your Home & Lifestyle");
        setupHomePanel(homePanel);
        mainPanel.add(homePanel, gbc);
        
        gbc.gridy++;
        JPanel declarationPanel = createTitledPanel("📝 Declaration & Signature");
        setupDeclarationPanel(declarationPanel);
        mainPanel.add(declarationPanel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_BACKGROUND);

        backButton = new JButton("Back");
        styleSecondaryButton(backButton);
        buttonPanel.add(backButton);

        submitButton = new JButton("Submit Application");
        styleButton(submitButton);
        buttonPanel.add(submitButton);

        mainPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);

        addListeners();

        // Fix window sizing and positioning
        pack();
        
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Set minimum size
        setMinimumSize(new Dimension(800, 600));
        
        // Set preferred size to fit screen with some margin
        int maxWidth = Math.min(1200, (int)(screenSize.width * 0.9));
        int maxHeight = Math.min(900, (int)(screenSize.height * 0.9));
        setSize(maxWidth, maxHeight);
        
        // Center the window
        setLocationRelativeTo(null);
        
        // Make window resizable
        setResizable(true);
        
        // Optionally start maximized if screen is large enough
        if (screenSize.width >= 1024 && screenSize.height >= 768) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PANEL_BACKGROUND);
        
        Border lineBorder = BorderFactory.createLineBorder(COLOR_BORDER.brighter());
        Border emptyBorder = new EmptyBorder(15, 15, 15, 15);
        
        Border titledBorder = BorderFactory.createTitledBorder(
            lineBorder, title, TitledBorder.LEFT, TitledBorder.TOP, FONT_LEGEND, COLOR_TEXT);

        panel.setBorder(new CompoundBorder(titledBorder, emptyBorder));
        return panel;
    }

    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void setupAboutYouPanel(JPanel panel) {
        GridBagConstraints c;
        // Row 0: Name Labels
        c = createGbc(0, 0); panel.add(createLabel("Last Name:"), c);
        c = createGbc(1, 0); panel.add(createLabel("First Name:"), c);
        c = createGbc(2, 0); panel.add(createLabel("Middle Name:"), c);

        // Row 1: Name Fields
        c = createGbc(0, 1); c.weightx = 0.33; lastNameField = createTextField(15); panel.add(lastNameField, c);
        c = createGbc(1, 1); c.weightx = 0.33; firstNameField = createTextField(15); panel.add(firstNameField, c);
        c = createGbc(2, 1); c.weightx = 0.33; middleNameField = createTextField(15); panel.add(middleNameField, c);
        
        // Row 2: Birthdate & Age Labels
        c = createGbc(0, 2); panel.add(createLabel("Date of Birth (YYYY-MM-DD):"), c);
        c = createGbc(1, 2); c.gridwidth = 2; panel.add(createLabel("Age:"), c);

        // Row 3: Birthdate & Age Fields
        c = createGbc(0, 3);
        try {
            MaskFormatter dateMask = new MaskFormatter("####-##-##");
            birthdateField = new JFormattedTextField(dateMask);
            styleTextField(birthdateField);
        } catch (ParseException e) {
            birthdateField = new JFormattedTextField();
        }
        panel.add(birthdateField, c);

        c = createGbc(1, 3); c.gridwidth = 2;
        ageField = createTextField(15);
        ageField.setEditable(false);
        ageField.setBackground(COLOR_BACKGROUND);
        panel.add(ageField, c);

        // Row 4: Sex & Occupation Labels
        c = createGbc(0, 4); c.gridwidth = 1; panel.add(createLabel("Sex:"), c);
        c = createGbc(1, 4); c.gridwidth = 2; panel.add(createLabel("Occupation:"), c);

        // Row 5: Sex & Occupation Fields
        c = createGbc(0, 5);
        JPanel sexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sexPanel.setBackground(COLOR_PANEL_BACKGROUND);
        maleRadio = createRadioButton("Male");
        femaleRadio = createRadioButton("Female");
        nonBinaryRadio = createRadioButton("Non-binary");
        preferNotToSayRadio = createRadioButton("Prefer not to say");
        sexGroup = new ButtonGroup();
        sexGroup.add(maleRadio); sexGroup.add(femaleRadio); sexGroup.add(nonBinaryRadio); sexGroup.add(preferNotToSayRadio);
        sexPanel.add(maleRadio); sexPanel.add(femaleRadio); sexPanel.add(nonBinaryRadio); sexPanel.add(preferNotToSayRadio);
        panel.add(sexPanel, c);

        c = createGbc(1, 5); c.gridwidth = 2;
        occupationField = createTextField(30);
        panel.add(occupationField, c);
    }
    
    private void setupContactPanel(JPanel panel) {
        GridBagConstraints c;
        c = createGbc(0, 0); c.weightx = 1.0; panel.add(createLabel("Email Address:"), c);
        c = createGbc(0, 1); emailField = createTextField(30); panel.add(emailField, c);
        
        c = createGbc(0, 2); panel.add(createLabel("Contact Number:"), c);
        c = createGbc(0, 3); phoneField = createTextField(30); panel.add(phoneField, c);
        
        c = createGbc(0, 4); panel.add(createLabel("Full Address:"), c);
        c = createGbc(0, 5); addressField = createTextField(30); panel.add(addressField, c);
    }

    private void setupHomePanel(JPanel panel) {
        GridBagConstraints c;
        
        JPanel residencePanel = new JPanel();
        residencePanel.setBackground(COLOR_PANEL_BACKGROUND);
        residencePanel.setLayout(new BoxLayout(residencePanel, BoxLayout.Y_AXIS));
        homeRadio = createRadioButton("A. Single Family Home");
        duplexRadio = createRadioButton("B. Duplex / Twin");
        condoRadio = createRadioButton("C. Condo / Townhouse");
        trailerRadio = createRadioButton("D. Trailer");
        apartmentRadio = createRadioButton("E. Apartment");
        residenceGroup = new ButtonGroup();
        residenceGroup.add(homeRadio); residenceGroup.add(duplexRadio); residenceGroup.add(condoRadio);
        residenceGroup.add(trailerRadio); residenceGroup.add(apartmentRadio);
        residencePanel.add(homeRadio); residencePanel.add(duplexRadio); residencePanel.add(condoRadio);
        residencePanel.add(trailerRadio); residencePanel.add(apartmentRadio);
        c = createGbc(0, 1); c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(residencePanel, c);

        c = createGbc(0, 0); c.weightx = 0.5; panel.add(createLabel("What type of residence do you live in?"), c);

        c = createGbc(1, 0); panel.add(createLabel("Do you currently have any other pets?"), c);
        JPanel hasPetsPanel = new JPanel();
        hasPetsPanel.setBackground(COLOR_PANEL_BACKGROUND);
        hasPetsPanel.setLayout(new BoxLayout(hasPetsPanel, BoxLayout.Y_AXIS));
        petsYesRadio = createRadioButton("A. YES");
        petsNoRadio = createRadioButton("B. NO");
        hasPetsGroup = new ButtonGroup();
        hasPetsGroup.add(petsYesRadio); hasPetsGroup.add(petsNoRadio);
        hasPetsPanel.add(petsYesRadio); hasPetsPanel.add(petsNoRadio);
        c = createGbc(1, 1); panel.add(hasPetsPanel, c);

        currentPetSection = createTitledPanel("Current Pet Details");
        currentPetSection.setVisible(false);
        setupCurrentPetSection(currentPetSection);
        c = createGbc(0, 2); c.gridwidth = 2; c.weighty = 1.0;
        panel.add(currentPetSection, c);
    }
    
    private void setupCurrentPetSection(JPanel panel) {
        GridBagConstraints c;
        c = createGbc(0, 0); c.weightx = 1.0; panel.add(createLabel("Name of Pet:"), c);
        c = createGbc(0, 1); petNameField = createTextField(20); panel.add(petNameField, c);
        
        c = createGbc(0, 2); panel.add(createLabel("Do you often play with your pet? (Describe temperament, etc.)"), c);
        c = createGbc(0, 3);
        petInteractionArea = new JTextArea(4, 30);
        styleTextArea(petInteractionArea);
        JScrollPane scrollPane = new JScrollPane(petInteractionArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, c);
    }
    
    private void setupDeclarationPanel(JPanel panel) {
        GridBagConstraints c;
        String declarationText = "<html><body style='width: 500px'>I certify that all the information provided is true and complete. I understand that any misrepresentation may result in the refusal of my adoption application.</body></html>";
        c = createGbc(0, 0); panel.add(createLabel(declarationText), c);

        c = createGbc(0, 1); c.insets = new Insets(10, 5, 5, 5); panel.add(createLabel("Signature (Type your full name):"), c);
        c = createGbc(0, 2); c.insets = new Insets(0, 5, 5, 5); signatureField = createTextField(30); panel.add(signatureField, c);
    }

    // --- Component Styling & Creation Helpers ---
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_TEXT);
        return label;
    }

    private JRadioButton createRadioButton(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(FONT_INPUT);
        rb.setBackground(COLOR_PANEL_BACKGROUND);
        return rb;
    }

    private JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        styleTextField(textField);
        return textField;
    }

    private void styleTextField(JComponent textField) {
        textField.setFont(FONT_INPUT);
        textField.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(8, 8, 8, 8)
        ));
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER_FOCUS, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
                ));
            }
        });
    }
    
    private void styleTextArea(JTextArea textArea) {
        textArea.setFont(FONT_INPUT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(8, 8, 8, 8)
        ));
        textArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                textArea.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER_FOCUS, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                textArea.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
                ));
            }
        });
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 30, 12, 30));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(COLOR_PRIMARY_DARK);
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(COLOR_PRIMARY);
            }
        });
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setBackground(COLOR_SECONDARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 30, 12, 30));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(COLOR_SECONDARY_DARK);
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(COLOR_SECONDARY);
            }
        });
    }

    // --- Logic & Listeners ---
    private void addListeners() {
        ActionListener petRadioListener = e -> {
            currentPetSection.setVisible(petsYesRadio.isSelected());
            revalidate();
            repaint();
        };
        petsYesRadio.addActionListener(petRadioListener);
        petsNoRadio.addActionListener(petRadioListener);
        
        birthdateField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { calculateAge(); }
            @Override
            public void removeUpdate(DocumentEvent e) { calculateAge(); }
            @Override
            public void changedUpdate(DocumentEvent e) { calculateAge(); }
        });
        
        submitButton.addActionListener(c -> handleSubmit());
        backButton.addActionListener(c -> handleBack());
    }

    private void handleBack() {
        if (parentFrame != null) {
            parentFrame.setVisible(true);
            this.dispose();
        } else {
            // If no parent frame, just close the application
            System.exit(0);
        }
    }

    private void calculateAge() {
        String text = birthdateField.getText().replaceAll("[^0-9]", "");
        if (text.length() == 8) {
             try {
                LocalDate birthDate = LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDate today = LocalDate.now();
                int age = Period.between(birthDate, today).getYears();
                ageField.setText(String.valueOf(age));
            } catch (DateTimeParseException e) {
                ageField.setText("");
            }
        } else {
            ageField.setText("");
        }
    }
    
    private void handleSubmit() {
        if (Objects.equals(firstNameField.getText(), "") || Objects.equals(lastNameField.getText(), "") || Objects.equals(signatureField.getText(), "")) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (First Name, Last Name, Signature).", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
return;
        }
        
        String petInfo = selectedPetName != null ? " for " + selectedPetName : "";
        String message = "Thank you, " + firstNameField.getText() + "! Your application" + petInfo + " has been submitted successfully.";
        JOptionPane.showMessageDialog(this, message, "Submission Successful", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PetAdoptionForm().setVisible(true));
    }
}

