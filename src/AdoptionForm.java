import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AdoptionForm extends JFrame {

    // UI Components
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private Theme.ModernButton toggleModeBtn;
    private Theme.ModernButton submitBtn;

    // Data Fields
    private JLabel imagePreviewLabel;
    private File selectedImageFile;
    private JTextField nameField;
    private JComboBox<String> genderBox;
    private JTextField ageField;
    private JTextField breedField;
    private JComboBox<String> healthBox;
    private JTextField contactField;
    private JTextField traitsField;
    private JTextArea descriptionTextArea;

    // State
    private Dashboard parentDashboard;
    private String currentUser = "Guest";
    private boolean isOwnerMode = false;

    public AdoptionForm() {
        setTitle("Register Pet");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Theme.MAIN_BG);
        setContentPane(contentPane);

        contentPane.add(createHeader(), BorderLayout.NORTH);
        contentPane.add(createFormContent(), BorderLayout.CENTER);
        contentPane.add(createFooter(), BorderLayout.SOUTH);
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(15, 40, 15, 40)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        titleLabel = new JLabel(" Register New Pet");
        titleLabel.setFont(Theme.HEADER);
        titleLabel.setForeground(Theme.TEXT_DARK);

        subtitleLabel = new JLabel("Add a new pet to the database for adoption.");
        subtitleLabel.setFont(Theme.BODY);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);

        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        header.add(textPanel, BorderLayout.WEST);

        toggleModeBtn = new Theme.ModernButton("My Pets", new Color(139, 92, 246));
        toggleModeBtn.setPreferredSize(new Dimension(140, 40));
        toggleModeBtn.addActionListener(e -> toggleMode());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(toggleModeBtn);
        header.add(btnWrapper, BorderLayout.EAST);

        return header;
    }

    private void toggleMode() {
        isOwnerMode = !isOwnerMode;
        if (isOwnerMode) {
            titleLabel.setText(" Add My Pet");
            subtitleLabel.setText("Register your own pet for services.");
            toggleModeBtn.setText("Back");
            toggleModeBtn.setBackground(new Color(100, 116, 139));
            submitBtn.setText("Save My Pet");
            submitBtn.setBackground(new Color(34, 197, 94));
        } else {
            titleLabel.setText(" Register New Pet");
            subtitleLabel.setText("Add a new pet to the database for adoption.");
            toggleModeBtn.setText("My Pets");
            toggleModeBtn.setBackground(new Color(139, 92, 246));
            submitBtn.setText("Register for Adoption");
            submitBtn.setBackground(Theme.SUCCESS);
        }
        toggleModeBtn.repaint();
        submitBtn.repaint();
    }

    private JScrollPane createFormContent() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Theme.MAIN_BG);
        container.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 15, 0, 15);
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.6;
        container.add(createInputSection(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        container.add(createImageSection(), gbc);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createInputSection() {
        Theme.RoundedPanel panel = new Theme.RoundedPanel(20, Color.WHITE);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridy = 0; gbc.gridx = 0;
        addLabeledField(panel, gbc, "Pet Name", nameField = createField());
        gbc.gridx = 1;
        addLabeledField(panel, gbc, "Age (e.g. 2 yrs)", ageField = createField());

        gbc.gridy++; gbc.gridx = 0;
        addLabeledField(panel, gbc, "Breed", breedField = createField());
        gbc.gridx = 1;
        genderBox = new JComboBox<>(new String[]{"Male", "Female"});
        styleComboBox(genderBox);
        addLabeledField(panel, gbc, "Gender", genderBox);

        gbc.gridy++; gbc.gridx = 0;
        healthBox = new JComboBox<>(new String[]{"Healthy", "Vaccinated", "Needs Attention", "Under Treatment"});
        styleComboBox(healthBox);
        addLabeledField(panel, gbc, "Health Status", healthBox);
        gbc.gridx = 1;
        addLabeledField(panel, gbc, "Contact Number", contactField = createField());

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        addLabeledField(panel, gbc, "Personal Traits", traitsField = createField());

        gbc.gridy++;
        descriptionTextArea = new JTextArea(4, 20);
        styleTextArea(descriptionTextArea);
        addLabeledField(panel, gbc, "Description", new JScrollPane(descriptionTextArea));

        return panel;
    }

    private JPanel createImageSection() {
        Theme.RoundedPanel panel = new Theme.RoundedPanel(20, Color.WHITE);
        panel.setLayout(new BorderLayout(0, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lbl = new JLabel("Pet Photo");
        lbl.setFont(Theme.SUBHEADER);
        panel.add(lbl, BorderLayout.NORTH);

        imagePreviewLabel = new JLabel("<html><center>No Image Selected<br><font size='2'>Click Upload</font></center></html>", SwingConstants.CENTER);
        imagePreviewLabel.setFont(Theme.BODY);
        imagePreviewLabel.setForeground(Theme.TEXT_MUTED);
        imagePreviewLabel.setBackground(new Color(243, 244, 246));
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setPreferredSize(new Dimension(300, 300));

        panel.add(imagePreviewLabel, BorderLayout.CENTER);

        Theme.ModernButton uploadBtn = new Theme.ModernButton("📸 Upload Image", Theme.ACCENT);
        uploadBtn.addActionListener(e -> openImageChooser());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(uploadBtn);
        panel.add(btnWrapper, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(Theme.BODY_BOLD);
        cancelBtn.setForeground(Theme.TEXT_MUTED);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        submitBtn = new Theme.ModernButton("Register for Adoption", Theme.SUCCESS);
        submitBtn.setPreferredSize(new Dimension(200, 45));
        submitBtn.addActionListener(e -> handleSubmit());

        footer.add(cancelBtn);
        footer.add(submitBtn);
        return footer;
    }

    // --- MODIFIED SUBMIT LOGIC: DUAL WRITE ---
    private void handleSubmit() {
        if (nameField.getText().trim().isEmpty() || contactField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Name and Contact Info.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prepare image bytes beforehand to be final/effectively final for lambda
        byte[] imgData = null;
        if (selectedImageFile != null) {
            try {
                imgData = Files.readAllBytes(selectedImageFile.toPath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error reading image file: " + e.getMessage());
                return;
            }
        }
        final byte[] imageBytes = imgData;

        // SQL
        String sql = "INSERT INTO pets_accounts (name, age, breed, gender, health_status, contact_number, personal_traits, reason_for_adoption, image, status, owner_username) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        // Use DBDual to write to both databases
        DBDual.executeUpdateBoth(sql, stmt -> {
            stmt.setString(1, nameField.getText());
            stmt.setString(2, ageField.getText());
            stmt.setString(3, breedField.getText());
            stmt.setString(4, (String) genderBox.getSelectedItem());
            stmt.setString(5, (String) healthBox.getSelectedItem());
            stmt.setString(6, contactField.getText());
            stmt.setString(7, traitsField.getText());
            stmt.setString(8, descriptionTextArea.getText());

            if (imageBytes != null) stmt.setBytes(9, imageBytes);
            else stmt.setBytes(9, null);

            stmt.setString(10, isOwnerMode ? "Owned" : "Available");
            stmt.setString(11, currentUser);
        });

        String successMsg = isOwnerMode ? "Pet Saved to Your Profile (Synced)!" : "Pet Registered for Adoption (Synced)!";
        JOptionPane.showMessageDialog(this, successMsg);

        if (parentDashboard != null && !isOwnerMode) {
            parentDashboard.getPawManagementPanel().reloadPets();
        }
        dispose();
    }

    // --- UI Helpers ---
    private void addLabeledField(JPanel p, GridBagConstraints gbc, String text, Component c) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY_BOLD);
        label.setForeground(Theme.TEXT_DARK);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(c, BorderLayout.CENTER);
        p.add(wrapper, gbc);
    }

    private JTextField createField() {
        JTextField tf = new JTextField();
        tf.setFont(Theme.BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox box) {
        box.setFont(Theme.BODY);
        box.setBackground(Color.WHITE);
        ((JComponent)box.getRenderer()).setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    private void styleTextArea(JTextArea ta) {
        ta.setFont(Theme.BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void openImageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg", "gif"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            try {
                byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                // WebP Check
                if (imageBytes.length > 12 &&
                        imageBytes[0] == (byte)0x52 && imageBytes[1] == (byte)0x49 &&
                        imageBytes[8] == (byte)0x57 && imageBytes[9] == (byte)0x45) {
                    imagePreviewLabel.setText("<html><center><b>WebP Format Detected</b><br>Java cannot read WebP.<br>Please open in Paint<br>and Save As .PNG</center></html>");
                    imagePreviewLabel.setIcon(null);
                    return;
                }
                ImageIcon icon = new ImageIcon(imageBytes);
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0) {
                    Image scaled = icon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                    imagePreviewLabel.setText("");
                    imagePreviewLabel.setIcon(new ImageIcon(scaled));
                } else {
                    imagePreviewLabel.setText("<html><center>File Corrupted<br>or Unsupported</center></html>");
                    imagePreviewLabel.setIcon(null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                imagePreviewLabel.setText("Read Error");
            }
        }
    }
    public void setParentDashboard(Dashboard d) { this.parentDashboard = d; }
}