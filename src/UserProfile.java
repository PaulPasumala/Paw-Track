import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserProfile {

    private static JLabel nameLabel, emailLabel, numLabel, addressLabel;

    private JLabel petsCountLabel;
    private JLabel adoptionsCountLabel;

    private static BufferedImage profileImage = null;
    private static JPanel iconPanel;
    private String username;
    private JPanel mainContentPanel;

    // Components to refresh
    private JPanel ownedPetsContainer;
    private JPanel systemLogsContainer;
    private JPanel adoptedPetsContainer;

    private static final String[] PROFILE_IMAGE_URLS = {
            "resources/avatar/Avatar.png",
            "resources/avatar/Avatar 1.png",
            "resources/avatar/Avatar 2.png",
            "resources/avatar/Avatar 3.png",
            "resources/avatar/Avatar 4.png",
            "resources/avatar/Avatar 6.png"
    };

    private int currentImageIndex = 0;
    
    // Edit mode components
    private boolean isEditMode = false;
    private JTextField emailField, contactField, addressField;
    private JButton editButton;
    private JPanel leftPanelContainer;

    public UserProfile(String username) {
        this.username = username;
        initializeUI();
        loadUserData();
        loadStats();

        int randomIndex = 0;
        if (username != null && !username.isEmpty()) {
            randomIndex = Math.abs(username.hashCode()) % PROFILE_IMAGE_URLS.length;
        }
        loadProfileImage(randomIndex);
    }

    public UserProfile() {
        this("Guest");
    }

    public JPanel getMainContentPanel() {
        return mainContentPanel;
    }

    public void refresh() {
        loadUserData();
        loadStats();
        if (adoptedPetsContainer != null) loadAdoptedPets();
        if (ownedPetsContainer != null) loadOwnedPets();
        if (systemLogsContainer != null) loadSystemLogs();
    }

    private void loadStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int ownedCount = 0;
                int registeredCount = 0;

                try (Connection conn = DBConnector.getConnection()) {
                    String sqlOwned = "SELECT COUNT(*) FROM pets_accounts WHERE owner_username = ? AND status IN ('Owned', 'Pairing', 'Paired')";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlOwned)) {
                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) ownedCount = rs.getInt(1);
                    }

                    String sqlReg = "SELECT COUNT(*) FROM pets_accounts WHERE owner_username = ? AND status = 'Available'";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlReg)) {
                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) registeredCount = rs.getInt(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new int[]{ownedCount, registeredCount};
            }

            @Override
            protected void done() {
                try {
                    int[] results = get();
                    if (petsCountLabel != null) petsCountLabel.setText("Pets: " + results[0]);
                    if (adoptionsCountLabel != null) adoptionsCountLabel.setText("Adoptions: " + results[1]);
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void initializeUI() {
        mainContentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(173, 216, 230),
                        0, getHeight(), new Color(255, 182, 193)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainContentPanel.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        JPanel leftPanel = createLeftPanel();
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        mainContentPanel.add(leftPanel, gbc);

        JPanel rightPanel = createRightPanel();
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        mainContentPanel.add(rightPanel, gbc);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(210, 200, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                if (profileImage != null) {
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(x, y, size, size));
                    g2.drawImage(profileImage, x, y, size, size, null);
                } else {
                    g2.setColor(new Color(230, 230, 240));
                    g2.fillOval(x, y, size, size);
                }
            }
        };
        iconPanel.setPreferredSize(new Dimension(130, 130));
        iconPanel.setMaximumSize(new Dimension(130, 130));
        iconPanel.setMinimumSize(new Dimension(130, 130));
        iconPanel.setOpaque(false);
        iconPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        iconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showImageSelectionDialog();
            }
        });

        nameLabel = new JLabel(username, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 22));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setMaximumSize(new Dimension(240, 30));

        emailLabel = new JLabel("Loading email...", SwingConstants.CENTER);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailLabel.setMaximumSize(new Dimension(240, 20));

        numLabel = new JLabel("Loading contact...", SwingConstants.CENTER);
        numLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        numLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        numLabel.setMaximumSize(new Dimension(240, 20));

        addressLabel = new JLabel("<html><center>No address on file.<br>Adopt a pet to update!</center></html>", SwingConstants.CENTER);
        addressLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        addressLabel.setForeground(new Color(80, 80, 80));
        addressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addressLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        addressLabel.setMaximumSize(new Dimension(240, 80));
        addressLabel.setMinimumSize(new Dimension(240, 40));
        addressLabel.setPreferredSize(new Dimension(240, 60));

        editButton = new JButton("Edit Profile");
        editButton.setFont(new Font("Arial", Font.BOLD, 15));
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.setBackground(new Color(100, 90, 220));
        editButton.setForeground(Color.WHITE);
        editButton.setBorder(BorderFactory.createEmptyBorder(12, 50, 12, 50));
        editButton.setFocusPainted(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.setMaximumSize(new Dimension(250, 45));
        editButton.setMinimumSize(new Dimension(250, 45));
        editButton.setPreferredSize(new Dimension(250, 45));
        editButton.addActionListener(e -> toggleEditMode());

        JPanel quickStatsPanel = createQuickStatsPanel();

        leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(iconPanel);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(nameLabel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(emailLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(numLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(addressLabel);
        leftPanel.add(Box.createVerticalStrut(25));
        leftPanel.add(editButton);
        leftPanel.add(Box.createVerticalStrut(35));
        leftPanel.add(quickStatsPanel);
        leftPanel.add(Box.createVerticalGlue());
        
        leftPanelContainer = leftPanel;

        return leftPanel;
    }

    private void loadUserData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String dbEmail = "";
            String dbContact = "";
            String dbAddress = "";
            String dbName = username;

            @Override
            protected Void doInBackground() {
                try (Connection conn = DBConnector.getConnection()) {
                    String adoptionSql = "SELECT email, contact_number, address FROM adoption_applications " +
                            "WHERE applicant_name = ? ORDER BY application_date DESC LIMIT 1";

                    try (PreparedStatement stmt = conn.prepareStatement(adoptionSql)) {
                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            dbEmail = rs.getString("email");
                            dbContact = rs.getString("contact_number");
                            dbAddress = rs.getString("address");
                        }
                    }

                    if (dbEmail == null || dbEmail.isEmpty()) {
                        String userSql = "SELECT email_address, contact_number, first_name, last_name FROM user_accounts WHERE username = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                            stmt.setString(1, username);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()) {
                                dbEmail = rs.getString("email_address");
                                dbContact = rs.getString("contact_number");
                                dbName = rs.getString("first_name") + " " + rs.getString("last_name");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (dbName != null && !dbName.isEmpty()) nameLabel.setText(dbName);
                emailLabel.setText((dbEmail != null && !dbEmail.isEmpty()) ? dbEmail : "No email set");
                numLabel.setText((dbContact != null && !dbContact.isEmpty()) ? dbContact : "No contact #");
                
                // Format address with word wrapping - constrained height
                if (dbAddress != null && !dbAddress.isEmpty()) {
                    String wrapped = wrapText(dbAddress, 30);
                    addressLabel.setText(wrapped);
                    // Ensure size remains constrained
                    addressLabel.setMaximumSize(new Dimension(240, 80));
                    addressLabel.revalidate();
                } else {
                    addressLabel.setText("<html><center>No address info.<br>Adopt a pet to update.</center></html>");
                }
            }
        };
        worker.execute();
    }

    private JPanel createQuickStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(260, 180));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statsTitle = new JLabel("Quick Stats");
        statsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel statsContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);
        statsContent.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        statsContent.setMaximumSize(new Dimension(240, 110));
        statsContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        petsCountLabel = new JLabel("Pets: Loading...");
        petsCountLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        petsCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        adoptionsCountLabel = new JLabel("Adoptions: Loading...");
        adoptionsCountLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        adoptionsCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsContent.add(petsCountLabel);
        statsContent.add(Box.createVerticalStrut(12));
        statsContent.add(adoptionsCountLabel);

        statsPanel.add(statsTitle);
        statsPanel.add(statsContent);

        return statsPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);

        JPanel topSection = createTopPetCardsSection();

        JPanel middleSection = new JPanel(new GridLayout(1, 2, 15, 0));
        middleSection.setOpaque(false);

        JPanel systemLogsPanel = createSystemLogsPanel();
        JPanel adoptedPetsPanel = createAdoptedPetsPanel();

        middleSection.add(systemLogsPanel);
        middleSection.add(adoptedPetsPanel);

        rightPanel.add(topSection, BorderLayout.NORTH);
        rightPanel.add(middleSection, BorderLayout.CENTER);

        return rightPanel;
    }

    private JPanel createTopPetCardsSection() {
        JPanel section = createWhitePanel();
        section.setLayout(new BorderLayout());
        section.setPreferredSize(new Dimension(0, 220));

        JLabel title = new JLabel("Owned Pets");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        section.add(title, BorderLayout.NORTH);

        ownedPetsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        ownedPetsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(ownedPetsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getHorizontalScrollBar().setUI(new Theme.ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));

        section.add(scrollPane, BorderLayout.CENTER);
        return section;
    }

    private JPanel createSystemLogsPanel() {
        JPanel panel = createWhitePanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 180));

        JLabel title = new JLabel("Registered for Adoption");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.add(title, BorderLayout.NORTH);

        systemLogsContainer = new JPanel();
        systemLogsContainer.setLayout(new BoxLayout(systemLogsContainer, BoxLayout.Y_AXIS));
        systemLogsContainer.setOpaque(false);
        systemLogsContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(systemLogsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUI(new Theme.ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdoptedPetsPanel() {
        JPanel panel = createWhitePanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 180));

        JLabel title = new JLabel("Adopted Pets History");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.add(title, BorderLayout.NORTH);

        adoptedPetsContainer = new JPanel();
        adoptedPetsContainer.setLayout(new BoxLayout(adoptedPetsContainer, BoxLayout.Y_AXIS));
        adoptedPetsContainer.setOpaque(false);
        adoptedPetsContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(adoptedPetsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUI(new Theme.ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // --- [FIXED] Load Adopted Pets with FALLBACK for Missing Column ---
    private void loadAdoptedPets() {
        if (adoptedPetsContainer != null) {
            adoptedPetsContainer.removeAll();
            adoptedPetsContainer.revalidate();
            adoptedPetsContainer.repaint();
        }

        SwingWorker<List<JPanel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JPanel> doInBackground() {
                List<JPanel> list = new ArrayList<>();
                Set<Integer> processedPetIds = new HashSet<>();

                // Query 1: Try new schema with pet_id
                String sql = "SELECT a.pet_id, a.pet_name, a.status, a.application_date, " +
                        "p.breed, p.gender, p.age, p.image, p.reason_for_adoption " +
                        "FROM adoption_applications a " +
                        "LEFT JOIN pets_accounts p ON (a.pet_id = p.pet_id OR (a.pet_id IS NULL AND a.pet_name = p.name)) " +
                        "WHERE a.applicant_name = ? " +
                        "ORDER BY a.application_date DESC";

                try (Connection conn = DBConnector.getConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            int pId = rs.getInt("pet_id");
                            if (pId != 0) {
                                if (processedPetIds.contains(pId)) continue;
                                processedPetIds.add(pId);
                            }
                            String pName = rs.getString("pet_name");
                            String pStatus = rs.getString("status");
                            String pDate = rs.getString("application_date");
                            String pBreed = rs.getString("breed");
                            String pGender = rs.getString("gender");
                            String pAge = rs.getString("age");
                            byte[] pImg = rs.getBytes("image");
                            String pDesc = rs.getString("reason_for_adoption");
                            list.add(createAdoptedPetCard(pName, pBreed, pGender, pAge, pStatus, pDate, pImg, pDesc));
                        }
                    } catch (SQLException ex) {
                        // FALLBACK: If column 'pet_id' is unknown, run legacy query
                        if (ex.getMessage() != null && ex.getMessage().contains("Unknown column")) {
                            System.out.println("⚠️ Column 'pet_id' missing. Switching to legacy query.");
                            return loadLegacyAdoptedPets(conn);
                        } else {
                            ex.printStackTrace();
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return list;
            }

            // Helper for fallback query
            private List<JPanel> loadLegacyAdoptedPets(Connection conn) {
                List<JPanel> list = new ArrayList<>();
                String legacySql = "SELECT a.pet_name, a.status, a.application_date, " +
                        "p.breed, p.gender, p.age, p.image, p.reason_for_adoption " +
                        "FROM adoption_applications a " +
                        "LEFT JOIN pets_accounts p ON a.pet_name = p.name " +
                        "WHERE a.applicant_name = ? " +
                        "ORDER BY a.application_date DESC";
                try (PreparedStatement stmt = conn.prepareStatement(legacySql)) {
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        String pName = rs.getString("pet_name");
                        String pStatus = rs.getString("status");
                        String pDate = rs.getString("application_date");
                        String pBreed = rs.getString("breed");
                        String pGender = rs.getString("gender");
                        String pAge = rs.getString("age");
                        byte[] pImg = rs.getBytes("image");
                        String pDesc = rs.getString("reason_for_adoption");
                        list.add(createAdoptedPetCard(pName, pBreed, pGender, pAge, pStatus, pDate, pImg, pDesc));
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return list;
            }

            @Override
            protected void done() {
                try {
                    List<JPanel> cards = get();
                    if (cards.isEmpty()) {
                        JLabel empty = new JLabel("No adoption history found.");
                        empty.setForeground(Color.GRAY);
                        empty.setFont(new Font("Arial", Font.ITALIC, 13));
                        empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                        adoptedPetsContainer.add(Box.createVerticalStrut(20));
                        adoptedPetsContainer.add(empty);
                    } else {
                        for (JPanel c : cards) {
                            adoptedPetsContainer.add(c);
                            adoptedPetsContainer.add(Box.createVerticalStrut(10));
                        }
                    }
                    adoptedPetsContainer.revalidate();
                    adoptedPetsContainer.repaint();
                } catch(Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private JPanel createAdoptedPetCard(String name, String breed, String gender, String age,
                                        String status, String date, byte[] imageBytes, String desc) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.setMaximumSize(new Dimension(32000, 75));
        card.setPreferredSize(new Dimension(0, 75));

        JLabel imageLabel = new JLabel() {
            Image img = null;
            {
                if (imageBytes != null && imageBytes.length > 0) {
                    try {
                        InputStream in = new ByteArrayInputStream(imageBytes);
                        img = ImageIO.read(in);
                    } catch(Exception e){}
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0,0,50,50));
                    g2.drawImage(img, 0, 0, 50, 50, null);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillOval(0,0,50,50);
                }
            }
        };
        imageLabel.setPreferredSize(new Dimension(50, 50));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 14));

        String shortDate = (date != null && date.length() >= 10) ? date.substring(0, 10) : date;
        String subText = String.format("%s • %s", (status != null ? status : "Adoption"), (shortDate != null ? shortDate : "-"));
        JLabel detailLbl = new JLabel(subText);
        detailLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        detailLbl.setForeground(new Color(34, 197, 94));

        infoPanel.add(nameLbl);
        infoPanel.add(detailLbl);

        JButton viewBtn = new JButton("View");
        styleMiniButton(viewBtn, new Color(99, 102, 241));
        viewBtn.addActionListener(e -> {
            String safeBreed = breed != null ? breed : "Unknown";
            String safeGender = gender != null ? gender : "Unknown";
            String safeAge = age != null ? age : "?";
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(mainContentPanel);
            ViewContent details = new ViewContent(parentFrame, name, safeBreed, safeGender, safeAge, imageBytes, desc);
            details.setVisible(true);
        });

        card.add(imageLabel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(viewBtn, BorderLayout.EAST);

        return card;
    }

    private void loadSystemLogs() {
        if (systemLogsContainer != null) {
            systemLogsContainer.removeAll();
            systemLogsContainer.revalidate();
            systemLogsContainer.repaint();
        }

        SwingWorker<List<JPanel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JPanel> doInBackground() {
                List<JPanel> logs = new ArrayList<>();
                String sql = "SELECT name, breed, gender, age, image, reason_for_adoption, status " +
                        "FROM pets_accounts " +
                        "WHERE owner_username = ? AND status = 'Available'";

                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        String pName = rs.getString("name");
                        String pBreed = rs.getString("breed");
                        String pGender = rs.getString("gender");
                        String pAge = rs.getString("age");
                        byte[] pImg = rs.getBytes("image");
                        String pDesc = rs.getString("reason_for_adoption");

                        logs.add(createSystemLogCard(pName, pBreed, pGender, pAge, pImg, pDesc));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return logs;
            }

            @Override
            protected void done() {
                try {
                    List<JPanel> logs = get();
                    if (logs.isEmpty()) {
                        JLabel empty = new JLabel("You haven't registered any pets for adoption.");
                        empty.setForeground(Color.GRAY);
                        empty.setFont(new Font("Arial", Font.ITALIC, 13));
                        empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                        systemLogsContainer.add(Box.createVerticalStrut(20));
                        systemLogsContainer.add(empty);
                    } else {
                        for (JPanel log : logs) {
                            systemLogsContainer.add(log);
                            systemLogsContainer.add(Box.createVerticalStrut(10));
                        }
                    }
                    systemLogsContainer.revalidate();
                    systemLogsContainer.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private JPanel createSystemLogCard(String name, String breed, String gender, String age, byte[] imageBytes, String desc) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.setMaximumSize(new Dimension(32000, 70));
        card.setPreferredSize(new Dimension(0, 70));

        JLabel imageLabel = new JLabel() {
            Image img = null;
            {
                if (imageBytes != null && imageBytes.length > 0) {
                    try {
                        InputStream in = new ByteArrayInputStream(imageBytes);
                        img = ImageIO.read(in);
                    } catch(Exception e){}
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0,0,45,45));
                    g2.drawImage(img, 0, 0, 45, 45, null);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillOval(0,0,45,45);
                }
            }
        };
        imageLabel.setPreferredSize(new Dimension(45, 45));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        JLabel nameLbl = new JLabel(name + " (Registered)");
        nameLbl.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel detailLbl = new JLabel(breed + " • " + gender);
        detailLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        detailLbl.setForeground(Color.GRAY);
        infoPanel.add(nameLbl);
        infoPanel.add(detailLbl);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);

        JButton viewBtn = new JButton("View");
        styleMiniButton(viewBtn, new Color(120, 100, 200));
        viewBtn.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(mainContentPanel);
            ViewContent details = new ViewContent(parentFrame, name, breed, gender, age, imageBytes, desc);
            details.setVisible(true);
        });

        JButton deleteBtn = new JButton("Delete");
        styleMiniButton(deleteBtn, new Color(220, 80, 80));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainContentPanel,
                    "Delete " + name + " from adoption registry?\nIt will be removed from the public gallery.",
                    "Cancel Registration", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                deleteSystemLogPet(name);
            }
        });

        btnPanel.add(viewBtn);
        btnPanel.add(deleteBtn);

        card.add(imageLabel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    private void deleteSystemLogPet(String petName) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                String sql = "DELETE FROM pets_accounts WHERE name = ? AND owner_username = ?";
                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, petName);
                    stmt.setString(2, username);
                    int rows = stmt.executeUpdate();
                    return rows > 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(mainContentPanel, "Adoption registration cancelled.");
                        loadSystemLogs();
                        loadStats();
                    } else {
                        JOptionPane.showMessageDialog(mainContentPanel, "Failed to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void loadOwnedPets() {
        if (ownedPetsContainer != null) {
            ownedPetsContainer.removeAll();
            ownedPetsContainer.revalidate();
            ownedPetsContainer.repaint();
        }

        SwingWorker<List<JPanel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JPanel> doInBackground() {
                List<JPanel> cards = new ArrayList<>();
                String sql = "SELECT name, breed, gender, age, image, reason_for_adoption, status " +
                        "FROM pets_accounts " +
                        "WHERE owner_username = ? AND status IN ('Owned', 'Pairing', 'Paired')";

                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        String pName = rs.getString("name");
                        String pBreed = rs.getString("breed");
                        String pGender = rs.getString("gender");
                        String pAge = rs.getString("age");
                        byte[] pImg = rs.getBytes("image");
                        String pDesc = rs.getString("reason_for_adoption");
                        String pStatus = rs.getString("status");

                        cards.add(createDynamicPetCard(pName, "Pet", pBreed, pStatus, pGender, pAge, pImg, pDesc));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return cards;
            }

            @Override
            protected void done() {
                try {
                    List<JPanel> cards = get();
                    if (cards.isEmpty()) {
                        JLabel empty = new JLabel("No owned pets yet. Use 'Add My Pet' to register.");
                        empty.setForeground(Color.GRAY);
                        empty.setFont(new Font("Arial", Font.ITALIC, 14));
                        ownedPetsContainer.add(empty);
                    } else {
                        for (JPanel card : cards) {
                            ownedPetsContainer.add(card);
                        }
                    }
                    ownedPetsContainer.revalidate();
                    ownedPetsContainer.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private JPanel createDynamicPetCard(String name, String type, String breed, String status,
                                        String gender, String age, byte[] imageBytes, String desc) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setLayout(new BorderLayout(10, 10));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(280, 140));

        JPanel imagePanel = new JPanel() {
            Image img = null;
            {
                if (imageBytes != null && imageBytes.length > 0) {
                    try {
                        InputStream in = new ByteArrayInputStream(imageBytes);
                        img = ImageIO.read(in);
                    } catch(Exception e){}
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 200, 210));
                g2.fillOval(0, 0, 55, 55);
                if (img != null) {
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0,0,55,55));
                    g2.drawImage(img, 0, 0, 55, 55, null);
                }
            }
        };
        imagePanel.setPreferredSize(new Dimension(55, 55));
        imagePanel.setOpaque(false);

        JPanel petInfoPanel = new JPanel();
        petInfoPanel.setLayout(new BoxLayout(petInfoPanel, BoxLayout.Y_AXIS));
        petInfoPanel.setOpaque(false);

        JLabel petNameLabel = new JLabel(name);
        petNameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel breedLabel = new JLabel("Breed: " + breed);
        breedLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        String statusDisplay = status;
        if (status.equalsIgnoreCase("Pairing")) statusDisplay = "Ready to Pair";
        if (status.equalsIgnoreCase("Owned")) statusDisplay = "My Pet";

        JLabel statusLabel = new JLabel(statusDisplay);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(status.equals("Pairing") ? new Color(255, 140, 0) : Color.GRAY);

        petInfoPanel.add(petNameLabel);
        petInfoPanel.add(Box.createVerticalStrut(3));
        petInfoPanel.add(breedLabel);
        petInfoPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);

        // VIEW BUTTON
        JButton viewBtn = new JButton("View");
        styleMiniButton(viewBtn, new Color(120, 100, 200));
        viewBtn.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(mainContentPanel);
            ViewContent details = new ViewContent(parentFrame, name, breed, gender, age, imageBytes, desc);
            details.setVisible(true);
        });

        // DELETE BUTTON
        JButton deleteBtn = new JButton("Delete");
        styleMiniButton(deleteBtn, new Color(220, 80, 80));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainContentPanel,
                    "Are you sure you want to delete " + name + " from your owned pets?\nThis cannot be undone.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                deleteOwnedPet(name);
            }
        });

        // PAIR BUTTON (NEW)
        JButton pairBtn = new JButton();
        boolean isPairing = "Pairing".equalsIgnoreCase(status);
        boolean isPaired = "Paired".equalsIgnoreCase(status);

        if (isPaired) {
            pairBtn.setText("Paired");
            pairBtn.setEnabled(false);
            styleMiniButton(pairBtn, Color.GRAY);
        } else if (isPairing) {
            pairBtn.setText("Unpair");
            styleMiniButton(pairBtn, new Color(255, 165, 0)); // Orange for cancel
        } else {
            pairBtn.setText("Pair");
            styleMiniButton(pairBtn, new Color(34, 197, 94)); // Green for go
        }

        if (!isPaired) {
            pairBtn.addActionListener(e -> togglePairingStatus(name, status));
        }

        buttonPanel.add(viewBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(pairBtn);

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.add(imagePanel, BorderLayout.WEST);
        topPanel.add(petInfoPanel, BorderLayout.CENTER);

        card.add(topPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private void styleMiniButton(JButton btn, Color bg) {
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void togglePairingStatus(String petName, String currentStatus) {
        String newStatus = currentStatus.equalsIgnoreCase("Pairing") ? "Owned" : "Pairing";
        String message = currentStatus.equalsIgnoreCase("Pairing") ?
                "Pet removed from breeding pool." :
                "Pet is now available for breeding selection!";

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE pets_accounts SET status = ? WHERE name = ? AND owner_username = ?")) {
                    stmt.setString(1, newStatus);
                    stmt.setString(2, petName);
                    stmt.setString(3, username);
                    return stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(mainContentPanel, message);
                        loadOwnedPets();
                        loadStats();
                    } else {
                        JOptionPane.showMessageDialog(mainContentPanel, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void deleteOwnedPet(String petName) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                String sql = "DELETE FROM pets_accounts WHERE name = ? AND owner_username = ?";
                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, petName);
                    stmt.setString(2, username);
                    int rows = stmt.executeUpdate();
                    return rows > 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(mainContentPanel, "Pet deleted successfully.");
                        loadOwnedPets();
                        loadStats();
                    } else {
                        JOptionPane.showMessageDialog(mainContentPanel, "Failed to delete pet.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private JPanel createWhitePanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private void openEditPopup() {
        JOptionPane.showMessageDialog(mainContentPanel, "Edit Profile Feature Coming Soon!");
    }

    private void toggleEditMode() {
        if (isEditMode) {
            // Save mode - update database
            saveProfileChanges();
        } else {
            // Edit mode - show text fields
            enterEditMode();
        }
    }

    private void enterEditMode() {
        isEditMode = true;
        
        // Create modern styled text fields
        emailField = createModernTextField(emailLabel.getText().equals("No email set") ? "" : emailLabel.getText(), "Email Address");
        contactField = createModernTextField(numLabel.getText().equals("No contact #") ? "" : numLabel.getText(), "Contact Number");
        
        String currentAddress = addressLabel.getText();
        currentAddress = currentAddress.replaceAll("<html><center>", "").replaceAll("</center></html>", "").replaceAll("<br>", " ");
        if (currentAddress.contains("No address")) currentAddress = "";
        addressField = createModernTextField(currentAddress, "Home Address");
        
        // Hide labels and show fields
        emailLabel.setVisible(false);
        numLabel.setVisible(false);
        addressLabel.setVisible(false);
        
        // Add fields to panel (find position and insert)
        Component[] components = leftPanelContainer.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == emailLabel) {
                leftPanelContainer.add(emailField, i + 1);
            } else if (components[i] == numLabel) {
                leftPanelContainer.add(contactField, i + 1);
            } else if (components[i] == addressLabel) {
                leftPanelContainer.add(addressField, i + 1);
            }
        }
        
        // Update button with animation
        editButton.setText("Save Changes");
        editButton.setBackground(new Color(34, 197, 94));
        
        leftPanelContainer.revalidate();
        leftPanelContainer.repaint();
    }

    private JTextField createModernTextField(String text, String placeholder) {
        JTextField field = new JTextField(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background with rounded corners
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                // Draw border
                if (isFocusOwner()) {
                    g2.setColor(new Color(99, 102, 241));
                    g2.setStroke(new BasicStroke(2));
                } else {
                    g2.setColor(new Color(203, 213, 225));
                    g2.setStroke(new BasicStroke(1));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                super.paintComponent(g);
                
                // Draw placeholder if empty
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = getInsets().left;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(placeholder, x, y);
                }
            }
        };
        
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(240, 40));
        field.setMinimumSize(new Dimension(240, 40));
        field.setPreferredSize(new Dimension(240, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        field.setOpaque(false);
        field.setForeground(new Color(30, 41, 59));
        field.setCaretColor(new Color(99, 102, 241));
        
        // Add focus listener for visual feedback
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.repaint();
            }
        });
        
        return field;
    }

    private void exitEditMode() {
        isEditMode = false;
        
        // Remove text fields
        if (emailField != null) leftPanelContainer.remove(emailField);
        if (contactField != null) leftPanelContainer.remove(contactField);
        if (addressField != null) leftPanelContainer.remove(addressField);
        
        // Show labels
        emailLabel.setVisible(true);
        numLabel.setVisible(true);
        addressLabel.setVisible(true);
        
        // Update button
        editButton.setText("Edit Profile");
        editButton.setBackground(new Color(100, 90, 220));
        
        leftPanelContainer.revalidate();
        leftPanelContainer.repaint();
    }

    private void saveProfileChanges() {
        String newEmail = emailField.getText().trim();
        String newContact = contactField.getText().trim();
        String newAddress = addressField.getText().trim();
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection conn = DBConnector.getConnection()) {
                    // Update user_accounts table
                    String updateUserSql = "UPDATE user_accounts SET email_address = ?, contact_number = ? WHERE username = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateUserSql)) {
                        stmt.setString(1, newEmail.isEmpty() ? null : newEmail);
                        stmt.setString(2, newContact.isEmpty() ? null : newContact);
                        stmt.setString(3, username);
                        stmt.executeUpdate();
                    }
                    
                    // If address is provided, we could update the most recent adoption application
                    if (!newAddress.isEmpty()) {
                        String updateAdoptionSql = "UPDATE adoption_applications SET address = ?, email = ?, contact_number = ? " +
                                "WHERE applicant_name = ? ORDER BY application_date DESC LIMIT 1";
                        try (PreparedStatement stmt = conn.prepareStatement(updateAdoptionSql)) {
                            stmt.setString(1, newAddress);
                            stmt.setString(2, newEmail);
                            stmt.setString(3, newContact);
                            stmt.setString(4, username);
                            stmt.executeUpdate();
                        } catch (SQLException e) {
                            // Adoption applications might not exist, ignore
                        }
                    }
                    
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        // Update labels with new values
                        emailLabel.setText(newEmail.isEmpty() ? "No email set" : newEmail);
                        numLabel.setText(newContact.isEmpty() ? "No contact #" : newContact);
                        
                        // Format address with word wrapping
                        String displayAddress;
                        if (newAddress.isEmpty()) {
                            displayAddress = "<html><center>No address info.<br>Adopt a pet to update.</center></html>";
                        } else {
                            displayAddress = wrapText(newAddress, 30);
                        }
                        addressLabel.setText(displayAddress);
                        
                        exitEditMode();
                        JOptionPane.showMessageDialog(mainContentPanel, "Profile updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(mainContentPanel, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
                        exitEditMode();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    exitEditMode();
                }
            }
        };
        worker.execute();
    }

    // Helper method to wrap text for display with HTML
    private static String wrapText(String text, int maxCharsPerLine) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder html = new StringBuilder("<html><center>");
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxCharsPerLine) {
                if (currentLine.length() > 0) {
                    html.append(currentLine).append("<br>");
                    currentLine = new StringBuilder();
                }
                // Handle very long words
                if (word.length() > maxCharsPerLine) {
                    html.append(word.substring(0, maxCharsPerLine)).append("...<br>");
                    continue;
                }
            }
            
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }
        
        if (currentLine.length() > 0) {
            html.append(currentLine);
        }
        
        html.append("</center></html>");
        return html.toString();
    }

    // Helper method to truncate long text
    private static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private void loadProfileImage(int index) {
        if (index < 0 || index >= PROFILE_IMAGE_URLS.length) index = 0;
        currentImageIndex = index;
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                try {
                    File imageFile = new File(PROFILE_IMAGE_URLS[currentImageIndex]);
                    if (imageFile.exists()) return ImageIO.read(imageFile);
                } catch (IOException e) {}
                return null;
            }
            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        profileImage = img;
                        if (iconPanel != null) iconPanel.repaint();
                    }
                } catch (Exception e) {}
            }
        };
        worker.execute();
    }

    private void showImageSelectionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainContentPanel), "Select Profile Picture", true);
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(mainContentPanel);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Choose Your Profile Picture", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        dialog.add(titleLabel, BorderLayout.NORTH);

        JPanel imagesPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        imagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        imagesPanel.setBackground(Color.WHITE);

        for (int i = 0; i < PROFILE_IMAGE_URLS.length; i++) {
            final int index = i;
            JButton imgBtn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    try {
                        File imageFile = new File(PROFILE_IMAGE_URLS[index]);
                        if (imageFile.exists()) {
                            BufferedImage img = ImageIO.read(imageFile);
                            if (img != null) {
                                Graphics2D g2 = (Graphics2D) g;
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                int size = Math.min(getWidth(), getHeight()) - 20;
                                int x = (getWidth() - size) / 2;
                                int y = (getHeight() - size) / 2;
                                g2.setClip(new java.awt.geom.Ellipse2D.Float(x, y, size, size));
                                g2.drawImage(img, x, y, size, size, null);
                            }
                        }
                    } catch (Exception e) {}
                }
            };
            imgBtn.setPreferredSize(new Dimension(140, 140));
            imgBtn.setBackground(new Color(245, 245, 250));
            imgBtn.addActionListener(e -> { loadProfileImage(index); dialog.dispose(); });
            imagesPanel.add(imgBtn);
        }
        dialog.add(imagesPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("User Profile Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            
            UserProfile profile = new UserProfile("testuser");
            frame.add(profile.getMainContentPanel());
            
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}