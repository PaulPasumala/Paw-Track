import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.SwingWorker; // Added

public class AdoptionForm extends JFrame {

    private JLabel imagePreviewLabel;
    private int PREVIEW_WIDTH = 500;
    private int PREVIEW_HEIGHT = 400;

    private JTextField petNameField;
    private JTextField genderField;
    private JTextField ageField;
    private JTextField breedField;
    private JTextField healthField;
    private JTextField contactField;
    private JTextField traitsField;
    private JTextArea reasonArea;

    // Added this
    private JButton submitBtn;

    // Fonts
    private Font FONT_TITLE;
    private Font FONT_LABEL;
    private Font FONT_FIELD;
    private Font FONT_BUTTON;

    // Modern Color Palette
    private final Color COLOR_BG_START = new Color(240, 242, 245);
    private final Color COLOR_BG_END = new Color(220, 225, 235);
    private final Color COLOR_PRIMARY = new Color(99, 102, 241); // Indigo
    private final Color COLOR_SECONDARY = new Color(139, 92, 246); // Purple
    private final Color COLOR_SUCCESS = new Color(16, 185, 129); // Green
    private final Color COLOR_DANGER = new Color(239, 68, 68); // Red
    private final Color COLOR_CARD_BG = new Color(255, 255, 255);
    private final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private final Color COLOR_TEXT_LIGHT = new Color(100, 116, 139);
    private final Color COLOR_WHITE = Color.WHITE;

    private Dashboard parentDashboard;

    public AdoptionForm() {
        this(null);
    }

    public AdoptionForm(Dashboard parentDashboard) {
        this.parentDashboard = parentDashboard;

        setTitle("Pet Adoption Form - Paw Track Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initializeFonts();
        initializeUI();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private void initializeFonts() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int titleSize = Math.max(32, (int)(screenSize.height * 0.040));
        int labelSize = Math.max(14, (int)(screenSize.height * 0.018));
        int fieldSize = Math.max(16, (int)(screenSize.height * 0.020));
        int buttonSize = Math.max(14, (int)(screenSize.height * 0.018));

        FONT_TITLE = new Font("Segoe UI", Font.BOLD, titleSize);
        FONT_LABEL = new Font("Segoe UI", Font.BOLD, labelSize);
        FONT_FIELD = new Font("Segoe UI", Font.PLAIN, fieldSize);
        FONT_BUTTON = new Font("Segoe UI", Font.BOLD, buttonSize);

        PREVIEW_WIDTH = (int)(screenSize.width * 0.28);
        PREVIEW_HEIGHT = (int)(screenSize.height * 0.42);
    }

    private void initializeUI() {
        GradientPanel contentPane = new GradientPanel(COLOR_BG_START, COLOR_BG_END);
        setContentPane(contentPane);
        contentPane.setLayout(new GridBagLayout());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int cardWidth = (int)(screenSize.width * 0.88);
        int cardHeight = (int)(screenSize.height * 0.88);

        RoundedShadowPanel mainCard = new RoundedShadowPanel(25);
        mainCard.setLayout(new GridLayout(1, 2, 3, 0));
        mainCard.setPreferredSize(new Dimension(cardWidth, cardHeight));
        mainCard.setBackground(COLOR_CARD_BG);

        mainCard.add(createFormPanel());
        mainCard.add(createPhotoPanel());

        contentPane.add(mainCard, new GridBagConstraints());
    }

    private class GradientPanel extends JPanel {
        private Color startColor;
        private Color endColor;

        public GradientPanel(Color start, Color end) {
            this.startColor = start;
            this.endColor = end;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private class RoundedShadowPanel extends JPanel {
        private int cornerRadius;

        public RoundedShadowPanel(int radius) {
            super();
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < 20; i++) {
                g2.setColor(new Color(0, 0, 0, 2));
                g2.fillRoundRect(i, i, getWidth() - (i * 2), getHeight() - (i * 2),
                        cornerRadius + i, cornerRadius + i);
            }

            g2.setColor(getBackground());
            g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, cornerRadius, cornerRadius);
        }
    }

    private JPanel createFormPanel() {
        GradientPanel formPanel = new GradientPanel(COLOR_PRIMARY, COLOR_SECONDARY);
        formPanel.setLayout(new BorderLayout(0, 20));
        formPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);

        JLabel titleIcon = new JLabel("🐾");
        titleIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, FONT_TITLE.getSize()));

        JLabel title = new JLabel("PET ADOPTION");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_WHITE);

        titlePanel.add(titleIcon);
        titlePanel.add(title);
        formPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0;
        formGrid.add(createLabel("Name of Pet"), gbc);
        gbc.gridy = row++;
        petNameField = createTextField();
        formGrid.add(petNameField, gbc);

        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        formGrid.add(createLabel("Gender"), gbc);
        gbc.gridx = 1;
        formGrid.add(createLabel("Age"), gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        genderField = createTextField();
        formGrid.add(genderField, gbc);
        gbc.gridx = 1;
        ageField = createTextField();
        formGrid.add(ageField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        formGrid.add(createLabel("Breed"), gbc);
        gbc.gridy = row++;
        breedField = createTextField();
        formGrid.add(breedField, gbc);

        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        formGrid.add(createLabel("Health Status"), gbc);
        gbc.gridx = 1;
        formGrid.add(createLabel("Contact Number"), gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        healthField = createTextField();
        formGrid.add(healthField, gbc);
        gbc.gridx = 1;
        contactField = createTextField();
        formGrid.add(contactField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        formGrid.add(createLabel("Personal Traits"), gbc);
        gbc.gridy = row++;
        traitsField = createTextField();
        formGrid.add(traitsField, gbc);

        gbc.gridy = row++; gbc.weighty = 0;
        formGrid.add(createLabel("Reason for Adoption"), gbc);
        gbc.gridy = row++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 0;
        reasonArea = new JTextArea();
        reasonArea.setFont(FONT_FIELD);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(BorderFactory.createEmptyBorder());
        formGrid.add(reasonScroll, gbc);

        formPanel.add(formGrid, BorderLayout.CENTER);
        return formPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_WHITE);
        label.setBorder(new EmptyBorder(6, 5, 2, 5));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        field.setFont(FONT_FIELD);
        field.setBackground(new Color(255, 255, 255, 230));
        field.setForeground(COLOR_TEXT_DARK);
        field.setCaretColor(COLOR_PRIMARY);
        field.setBorder(new EmptyBorder(10, 16, 10, 16));
        field.setOpaque(false);

        field.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                field.setBackground(new Color(255, 255, 255, 255));
            }
            public void mouseExited(MouseEvent e) {
                if (!field.hasFocus()) field.setBackground(new Color(255, 255, 255, 230));
            }
        });

        return field;
    }

    private JScrollPane createTextArea() {
        JTextArea area = new JTextArea(4, 20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        area.setFont(FONT_FIELD);
        area.setBackground(new Color(255, 255, 255, 230));
        area.setForeground(COLOR_TEXT_DARK);
        area.setCaretColor(COLOR_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 16, 10, 16));
        area.setOpaque(false);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel createPhotoPanel() {
        JPanel photoPanel = new JPanel(new BorderLayout(0, 20));
        photoPanel.setBackground(COLOR_CARD_BG);
        photoPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);

        JLabel titleIcon = new JLabel("📷");
        titleIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, FONT_TITLE.getSize()));

        JLabel title = new JLabel("PET PHOTO");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_TEXT_DARK);

        titlePanel.add(titleIcon);
        titlePanel.add(title);
        photoPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        imagePreviewLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(241, 245, 249),
                        getWidth(), getHeight(), new Color(226, 232, 240));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0, new float[]{8, 8}, 0));
                g2.drawRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        imagePreviewLabel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setOpaque(false);
        setPlaceholderText("Click Upload to Add Image");

        centerPanel.add(imagePreviewLabel, BorderLayout.CENTER);

        JButton uploadBtn = createButton("📤 UPLOAD IMAGE", COLOR_PRIMARY, COLOR_WHITE);
        uploadBtn.addActionListener(e -> openImageChooser());
        centerPanel.add(uploadBtn, BorderLayout.SOUTH);

        photoPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        actionPanel.setOpaque(false);

        JButton backBtn = createButton("← BACK", COLOR_DANGER, COLOR_WHITE);
        backBtn.addActionListener(e -> handleBackButton());

        // Assign to field
        submitBtn = createButton("SUBMIT ✓", COLOR_SUCCESS, COLOR_WHITE);
        submitBtn.addActionListener(e -> handleSubmit());

        actionPanel.add(backBtn);
        actionPanel.add(submitBtn);

        photoPanel.add(actionPanel, BorderLayout.SOUTH);

        return photoPanel;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else {
                    g2.setColor(bg);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int height = (int)(Toolkit.getDefaultToolkit().getScreenSize().height * 0.065);
        btn.setPreferredSize(new Dimension(150, height));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            public void mouseExited(MouseEvent e) { btn.repaint(); }
        });

        return btn;
    }

    private void setPlaceholderText(String text) {
        String html = String.format(
                "<html><div style='text-align:center;color:#94A3B8;font-family:Segoe UI;font-size:%dpx'>%s</div></html>",
                FONT_FIELD.getSize(), text
        );
        imagePreviewLabel.setText(html);
        imagePreviewLabel.setIcon(null);
    }

    private void openImageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg") ||
                        name.endsWith(".jpeg") || name.endsWith(".gif");
            }
            public String getDescription() {
                return "Image Files (*.png, *.jpg, *.jpeg, *.gif)";
            }
        });

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(
                        PREVIEW_WIDTH - 30, PREVIEW_HEIGHT - 30, Image.SCALE_SMOOTH
                );
                imagePreviewLabel.setIcon(new ImageIcon(img));
                imagePreviewLabel.setText(null);
            } catch (Exception ex) {
                setPlaceholderText("Error loading image");
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleBackButton() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to go back?\nAny unsaved changes will be lost.",
                "Confirm Back",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            this.dispose();
        }
    }

    // CHANGED: Implemented SwingWorker, DBConnector, and correct table name
    private void handleSubmit() {
        submitBtn.setEnabled(false);
        submitBtn.setText("SUBMITTING...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

            private String errorMessage = "";
            private byte[] imageBytes = null;

            @Override
            protected Boolean doInBackground() throws Exception {
                // This runs on a background thread

                // 1. Prepare image data
                if (imagePreviewLabel.getIcon() != null) {
                    ImageIcon icon = (ImageIcon) imagePreviewLabel.getIcon();
                    Image img = icon.getImage();
                    BufferedImage buffered = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = buffered.createGraphics();
                    g2.drawImage(img, 0, 0, null);
                    g2.dispose();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(buffered, "png", baos);
                    imageBytes = baos.toByteArray();
                }

                // 2. Run database query
                String sql = "INSERT INTO pets_accounts (name, gender, age, breed, health_status, contact_number, personal_traits, reason_for_adoption, image, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, petNameField.getText());
                    stmt.setString(2, genderField.getText());
                    stmt.setString(3, ageField.getText());
                    stmt.setString(4, breedField.getText());
                    stmt.setString(5, healthField.getText());
                    stmt.setString(6, contactField.getText());
                    stmt.setString(7, traitsField.getText());
                    stmt.setString(8, reasonArea.getText());

                    if (imageBytes != null) {
                        stmt.setBytes(9, imageBytes);
                    } else {
                        stmt.setNull(9, java.sql.Types.BLOB);
                    }
                    stmt.setString(10, "available");

                    int rows = stmt.executeUpdate();
                    return rows > 0;

                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMessage = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                // This runs back on the UI thread
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AdoptionForm.this, "Pet saved successfully!");

                        if (parentDashboard != null) {
                            parentDashboard.getPawManagementPanel().reloadPets();
                        }
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(AdoptionForm.this, "Error: " + errorMessage,
                                "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdoptionForm.this, "An unexpected error occurred: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                                 }

                // Re-enable button
                submitBtn.setEnabled(true);
                submitBtn.setText("SUBMIT ✓");
            }
        };

        worker.execute();
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new AdoptionForm());
    }
}