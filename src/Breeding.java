import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class Breeding extends JPanel {

    private final Color BG_GRADIENT_TOP = new Color(147, 197, 253);
    private final Color BG_GRADIENT_BOTTOM = new Color(249, 168, 212);

    private String petIdCol = "pet_id";

    // Core UI
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel gridPanel;
    private JButton mainActionButton;

    // Match Maker Components
    private JPanel leftPetPanel;
    private JPanel rightPetPanel;
    private JPanel centerCardPanel;
    private JPanel cardContainer;
    private JLabel matchStepLabel;

    // Control buttons
    private JButton btnPrevious, btnSelect, btnNext;

    // Logic state
    private List<PetData> candidates = new ArrayList<>();
    private PetData selectedFemale = null;
    private PetData selectedMale = null;
    private int currentIndex = 0;

    // [FIX] Defined only once here
    private List<BreedingCardData> allPairs = new ArrayList<>();

    private enum MatchStep { SELECT_FEMALE, SELECT_MALE, CONFIRMATION }
    private MatchStep currentStep = MatchStep.SELECT_FEMALE;

    public Breeding() {
        setLayout(new BorderLayout());
        setOpaque(true);

        resolveSchema();
        ensureDatabaseSchema();

        add(createHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(createListView(), "LIST");
        contentPanel.add(createMatchMakerView(), "MATCH");

        add(contentPanel, BorderLayout.CENTER);

        loadBreedingPairs();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bg = new GradientPaint(
                0, 0, BG_GRADIENT_TOP,
                0, getHeight(), BG_GRADIENT_BOTTOM
        );

        g2.setPaint(bg);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color glass = new Color(255, 255, 255, 110);
                g2.setColor(glass);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                GradientPaint gloss = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 190),
                        0, getHeight() / 2, new Color(255, 255, 255, 40)
                );
                g2.setPaint(gloss);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(255, 255, 255, 150));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };

        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        header.setPreferredSize(new Dimension(0, 100));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Breeding Program");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(40, 40, 40));

        JLabel subtitle = new JLabel("Manage active pairings and track litters");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(60, 60, 60, 180));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        mainActionButton = createHeaderButton("+ New Pair");
        mainActionButton.addActionListener(e -> toggleView());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(mainActionButton, BorderLayout.EAST);

        return header;
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(34, 197, 94);
                Color hover = new Color(22, 163, 74);
                Color press = new Color(21, 128, 61);
                Color fill = base;
                if (getModel().isPressed()) fill = press;
                else if (getModel().isRollover()) fill = hover;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setMinimumSize(new Dimension(130, 40));
        btn.setMaximumSize(new Dimension(130, 40));
        return btn;
    }

    private JPanel createMatchMakerView() {
        JPanel container = new JPanel(new GridLayout(1, 3, 30, 0));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 40, 30, 40));

        leftPetPanel = createGlassSidePanel("Your Pet (Female)");
        centerCardPanel = createGlassCenterCard();
        rightPetPanel = createGlassSidePanel("Chosen Partner (Male)");

        container.add(leftPetPanel);
        container.add(centerCardPanel);
        container.add(rightPetPanel);

        return container;
    }

    private JPanel createGlassSidePanel(String labelText) {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(new Color(255, 255, 255, 170));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(70, 70, 70));
        p.add(lbl);
        return p;
    }

    private JPanel createGlassCenterCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                GradientPaint gloss = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 180),
                        0, getHeight() / 2, new Color(255, 255, 255, 40)
                );
                g2.setPaint(gloss);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Find a Match", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(60, 60, 60));

        matchStepLabel = new JLabel("Step 1: Select Female", SwingConstants.CENTER);
        matchStepLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        matchStepLabel.setForeground(new Color(80, 80, 80, 200));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(matchStepLabel);

        JPanel buttonContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                g2.dispose();
            }
        };
        buttonContainer.setOpaque(false);
        buttonContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonContainer.setBorder(new EmptyBorder(8, 25, 50, 25));

        btnPrevious = createGlassButton("Previous", new Color(236, 72, 153));
        btnPrevious.addActionListener(e -> navigateCandidate(-1));

        btnSelect = createGlassButton("Select", new Color(34, 197, 94));
        btnSelect.addActionListener(e -> selectCurrentCandidate());

        btnNext = createGlassButton("Next", new Color(99, 102, 241));
        btnNext.addActionListener(e -> navigateCandidate(1));

        buttonContainer.add(btnPrevious);
        buttonContainer.add(btnSelect);
        buttonContainer.add(btnNext);

        JPanel headerWrap = new JPanel();
        headerWrap.setLayout(new BoxLayout(headerWrap, BoxLayout.Y_AXIS));
        headerWrap.setOpaque(false);
        headerWrap.setBorder(new EmptyBorder(0, 0, 16, 0));
        headerWrap.add(titleBox);
        headerWrap.add(Box.createVerticalStrut(10));
        headerWrap.add(buttonContainer);

        card.add(headerWrap, BorderLayout.NORTH);

        cardContainer = new JPanel(new BorderLayout());
        cardContainer.setOpaque(false);
        cardContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.add(cardContainer, BorderLayout.CENTER);

        return card;
    }

    private JButton createGlassButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color hover = baseColor.darker();
                Color press = baseColor.darker().darker();
                Color fill = baseColor;
                if (getModel().isPressed()) fill = press;
                else if (getModel().isRollover()) fill = hover;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(95, 38));
        btn.setMinimumSize(new Dimension(95, 38));
        btn.setMaximumSize(new Dimension(95, 38));
        return btn;
    }

    private void displayCurrentCandidate() {
        cardContainer.removeAll();

        if (candidates.isEmpty()) {
            JLabel empty = new JLabel("<html><center>No " +
                    (currentStep == MatchStep.SELECT_FEMALE ? "Females" : "Males") +
                    "<br>Available for Pairing.</center></html>",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.BOLD, 18));
            empty.setForeground(new Color(120, 120, 120));
            cardContainer.add(empty, BorderLayout.CENTER);
            btnSelect.setEnabled(false);
            cardContainer.revalidate();
            cardContainer.repaint();
            return;
        }

        btnSelect.setEnabled(true);
        PetData p = candidates.get(currentIndex);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(170, 170));

        ImageIcon circular = createCircularImage(p.image, 170);
        if (circular != null) {
            imageLabel.setIcon(circular);
        } else {
            imageLabel.setText("🐾");
            imageLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        }

        JLabel name = new JLabel(p.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 26));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel info = new JLabel(p.breed + " • " + p.age + " yrs");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        info.setForeground(new Color(100, 100, 100));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.CENTER));
        badges.setOpaque(false);
        if (p.health != null && !p.health.isEmpty()) {
            badges.add(createMaterialBadge(p.health));
        } else {
            badges.add(createMaterialBadge("Healthy"));
        }

        card.add(imageLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(name);
        card.add(Box.createVerticalStrut(5));
        card.add(info);
        card.add(Box.createVerticalStrut(15));
        card.add(badges);

        cardContainer.add(card, BorderLayout.CENTER);
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    private JPanel createListView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);

        JPanel searchWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        searchWrapper.setOpaque(false);
        searchWrapper.setBorder(new EmptyBorder(8, 12, 8, 12));
        searchWrapper.setPreferredSize(new Dimension(300, 45));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        searchIcon.setBorder(new EmptyBorder(0, 8, 0, 8));

        JTextField searchField = new JTextField();
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.putClientProperty("JTextField.placeholderText", "Search pets…");

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterGrid(searchField.getText());
            }
        });

        searchWrapper.add(searchIcon, BorderLayout.WEST);
        searchWrapper.add(searchField, BorderLayout.CENTER);
        topBar.add(searchWrapper);

        panel.add(topBar, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 3, 25, 25));
        gridPanel.setOpaque(false);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.setBorder(new EmptyBorder(20, 0, 0, 0));
        gridWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void filterGrid(String query) {
        gridPanel.removeAll();
        String q = query.toLowerCase().trim();

        for (BreedingCardData data : allPairs) {
            boolean matches = false;
            if (data.female != null && data.female.toLowerCase().contains(q)) matches = true;
            if (data.male != null && data.male.toLowerCase().contains(q)) matches = true;
            if (data.status != null && data.status.toLowerCase().contains(q)) matches = true;
            if (data.date != null && data.date.contains(q)) matches = true;
            if (data.due != null && data.due.contains(q)) matches = true;

            if (matches) {
                gridPanel.add(new BreedingListCard(data));
            }
        }

        if (gridPanel.getComponentCount() == 0) {
            JLabel empty = new JLabel("No breeding pairs found.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(new Color(120, 120, 120));
            gridPanel.add(empty);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private class BreedingListCard extends JPanel {
        public BreedingListCard(BreedingCardData data) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(300, 260));
            setBorder(new EmptyBorder(8, 8, 8, 8));

            JPanel card = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                    g2.dispose();
                }
            };

            card.setOpaque(false);
            card.setBorder(new EmptyBorder(20, 20, 20, 20));

            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);

            JButton btnRemove = new JButton("x") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = new Color(239, 68, 68);
                    Color hover = new Color(220, 38, 38);
                    Color press = new Color(185, 28, 28);
                    Color fill = base;
                    if (getModel().isPressed()) fill = press;
                    else if (getModel().isRollover()) fill = hover;
                    g2.setColor(fill);
                    g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnRemove.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnRemove.setForeground(Color.WHITE);
            btnRemove.setContentAreaFilled(false);
            btnRemove.setBorderPainted(false);
            btnRemove.setFocusPainted(false);
            btnRemove.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRemove.setPreferredSize(new Dimension(28, 28));
            btnRemove.addActionListener(e -> deletePair(data.id));

            topBar.add(btnRemove, BorderLayout.EAST);
            card.add(topBar, BorderLayout.NORTH);

            JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            imgRow.setOpaque(false);

            JLabel imgF = createMaterialThumbnail(data.imgFemale);
            JLabel heart = new JLabel("❤️");
            heart.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            JLabel imgM = createMaterialThumbnail(data.imgMale);

            imgRow.add(imgF);
            imgRow.add(heart);
            imgRow.add(imgM);

            card.add(imgRow, BorderLayout.CENTER);

            JPanel info = new JPanel(new GridLayout(3, 1, 0, 3));
            info.setOpaque(false);
            info.setBorder(new EmptyBorder(10, 0, 0, 0));

            JLabel names = new JLabel(data.female + " & " + data.male, SwingConstants.CENTER);
            names.setFont(new Font("Segoe UI", Font.BOLD, 15));
            names.setForeground(new Color(50, 50, 50));

            JLabel status = new JLabel(data.status, SwingConstants.CENTER);
            status.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            status.setForeground(new Color(34, 197, 94));

            JLabel date = new JLabel("Due: " + data.due, SwingConstants.CENTER);
            date.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            date.setForeground(new Color(120, 120, 120));

            info.add(names);
            info.add(status);
            info.add(date);

            card.add(info, BorderLayout.SOUTH);
            add(card, BorderLayout.CENTER);
        }
    }

    private JLabel createMaterialThumbnail(byte[] imgBytes) {
        JLabel lbl = new JLabel();
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setPreferredSize(new Dimension(90, 90));
        ImageIcon icon = createCircularImage(imgBytes, 90);
        if (icon != null) {
            lbl.setIcon(icon);
        } else {
            lbl.setText("🐾");
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        }
        return lbl;
    }

    private JPanel createMaterialBadge(String text) {
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 240, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setBorder(new EmptyBorder(4, 10, 4, 10));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        bg.add(lbl);
        return bg;
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(203, 213, 225);
            this.trackColor = new Color(248, 250, 252);
        }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton j = new JButton();
            j.setPreferredSize(new Dimension(0, 0));
            j.setOpaque(false);
            j.setBorder(null);
            return j;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.dispose();
        }
    }

    private void resolveSchema() {
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pets_accounts LIMIT 1")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if ("id".equalsIgnoreCase(meta.getColumnName(i))) {
                    petIdCol = "id";
                    return;
                }
            }
        } catch (Exception e) {}
    }

    private void ensureDatabaseSchema() {
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement()) {
            try {
                stmt.executeQuery("SELECT female_id FROM active_breeding_pairs LIMIT 1");
            } catch (SQLException e) {
                stmt.executeUpdate("ALTER TABLE active_breeding_pairs ADD COLUMN female_id INT");
                stmt.executeUpdate("ALTER TABLE active_breeding_pairs ADD COLUMN male_id INT");
            }
        } catch (Exception ignored) {}
    }

    private void toggleView() {
        if (mainActionButton.getText().equals("+ New Pair")) {
            mainActionButton.setText("Cancel");
            mainActionButton.setBackground(new Color(255, 255, 255, 50));
            cardLayout.show(contentPanel, "MATCH");
            startMatchingProcess();
        } else {
            mainActionButton.setText("+ New Pair");
            styleHeaderButton(mainActionButton);
            cardLayout.show(contentPanel, "LIST");
            loadBreedingPairs();
        }
    }

    private void styleHeaderButton(JButton btn) {}

    private void startMatchingProcess() {
        selectedFemale = null;
        selectedMale = null;
        currentStep = MatchStep.SELECT_FEMALE;
        matchStepLabel.setText("Step 1: Select Female");
        resetSidePanel(leftPetPanel, "Your Pet (Female)");
        resetSidePanel(rightPetPanel, "Chosen Partner (Male)");
        btnSelect.setText("Select");
        btnNext.setEnabled(true);
        loadCandidates("Female");
    }

    private void resetSidePanel(JPanel panel, String label) {
        panel.removeAll();
        panel.setLayout(new GridBagLayout());
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(160, 160, 160));
        panel.add(lbl);
        panel.revalidate();
        panel.repaint();
    }

    private void updateSidePanel(JPanel panel, PetData pet) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        BreedingDetailCard card = new BreedingDetailCard(pet);
        panel.add(card, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void loadCandidates(String gender) {
        candidates.clear();
        currentIndex = 0;
        cardContainer.removeAll();

        SwingWorker<List<PetData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PetData> doInBackground() throws Exception {
                List<PetData> list = new ArrayList<>();
                // [UPDATED] Query for pets explicitly marked as 'Pairing'
                String sql =
                        "SELECT " + petIdCol + ", name, breed, age, gender, health_status, contact_number, " +
                                "personal_traits, reason_for_adoption, image " +
                                "FROM pets_accounts WHERE gender = ? AND status = 'Pairing'";

                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, gender);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        list.add(new PetData(
                                rs.getInt(1),
                                rs.getString("name"),
                                rs.getString("breed"),
                                rs.getString("age"),
                                gender,
                                rs.getString("health_status"),
                                rs.getString("contact_number"),
                                rs.getString("personal_traits"),
                                rs.getString("reason_for_adoption"),
                                rs.getBytes("image")
                        ));
                    }
                }
                return list;
            }

            @Override
            protected void done() {
                try {
                    candidates = get();
                    displayCurrentCandidate();
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void navigateCandidate(int direction) {
        if (candidates.isEmpty() || currentStep == MatchStep.CONFIRMATION) return;
        currentIndex += direction;
        if (currentIndex < 0) currentIndex = candidates.size() - 1;
        if (currentIndex >= candidates.size()) currentIndex = 0;
        displayCurrentCandidate();
    }

    private void selectCurrentCandidate() {
        if (candidates.isEmpty()) return;
        PetData selected = candidates.get(currentIndex);

        if (currentStep == MatchStep.SELECT_FEMALE) {
            selectedFemale = selected;
            updateSidePanel(leftPetPanel, selectedFemale);
            currentStep = MatchStep.SELECT_MALE;
            matchStepLabel.setText("Step 2: Select Partner for " + selectedFemale.name);
            loadCandidates("Male");
        } else if (currentStep == MatchStep.SELECT_MALE) {
            selectedMale = selected;
            updateSidePanel(rightPetPanel, selectedMale);
            currentStep = MatchStep.CONFIRMATION;
            matchStepLabel.setText("Step 3: Confirm Pairing");
            showConfirmationView();
        } else if (currentStep == MatchStep.CONFIRMATION) {
            createPairInDB(selectedFemale, selectedMale);
        }
    }

    private void showConfirmationView() {
        cardContainer.removeAll();
        JLabel heart = new JLabel("❤️", SwingConstants.CENTER);
        heart.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
        JLabel msg = new JLabel("Ready to Pair?", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel sub = new JLabel(selectedFemale.name + " + " + selectedMale.name, SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(Color.GRAY);
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.setOpaque(false);
        panel.add(heart);
        panel.add(msg);
        panel.add(sub);
        cardContainer.add(panel, BorderLayout.CENTER);
        btnSelect.setText("Pair ❤️");
        btnNext.setEnabled(false);
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    // [UPDATED] Direct Pairing Logic (No Approval Required)
    private void createPairInDB(PetData female, PetData male) {
        String sql = """
                INSERT INTO active_breeding_pairs
                (female_pet_name, male_pet_name, female_id, male_id, pairing_date, expected_due_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String today = LocalDate.now().toString();
        String due = LocalDate.now().plusDays(63).toString();

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, female.name);
            pstmt.setString(2, male.name);
            pstmt.setInt(3, female.id);
            pstmt.setInt(4, male.id);
            pstmt.setString(5, today);
            pstmt.setString(6, due);

            // [CHANGED] Status is now 'Monitoring' instantly
            pstmt.setString(7, "Monitoring");

            pstmt.executeUpdate();

            // [CHANGED] Pets are now 'Paired'
            updatePetStatus(female.name, "Paired");
            updatePetStatus(male.name, "Paired");

            JOptionPane.showMessageDialog(this,
                    "Success! Pairing created.\nPets are now marked as 'Paired'.",
                    "Paired Successfully",
                    JOptionPane.INFORMATION_MESSAGE);

            toggleView();

        } catch (Exception e) {
            tryFallbackInsert(female, male, today, due);
        }
    }

    private void updatePetStatus(String petName, String newStatus) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE pets_accounts SET status = ? WHERE name = ?")) {
            stmt.setString(1, newStatus);
            stmt.setString(2, petName);
            stmt.executeUpdate();
        } catch(SQLException ignored) {}
    }

    private void tryFallbackInsert(PetData female, PetData male, String today, String due) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("""
                     INSERT INTO active_breeding_pairs
                     (female_pet_name, male_pet_name, pairing_date, expected_due_date, status)
                     VALUES (?, ?, ?, ?, ?)
                     """)) {
            pstmt.setString(1, female.name);
            pstmt.setString(2, male.name);
            pstmt.setString(3, today);
            pstmt.setString(4, due);

            // [CHANGED] Fallback Status is also Monitoring
            pstmt.setString(5, "Monitoring");

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Success! (Legacy Mode)");
            toggleView();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deletePair(int pairId) {
        String sql = "DELETE FROM active_breeding_pairs WHERE pair_id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pairId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) loadBreedingPairs();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadBreedingPairs() {
        gridPanel.removeAll();
        allPairs.clear();
        SwingWorker<List<BreedingCardData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BreedingCardData> doInBackground() throws Exception {
                List<BreedingCardData> list = new ArrayList<>();
                String sql =
                        "SELECT abp.pair_id, abp.female_pet_name, abp.male_pet_name, abp.pairing_date, " +
                                "abp.expected_due_date, abp.status, MAX(f.image) as imgF, MAX(m.image) as imgM " +
                                "FROM active_breeding_pairs abp " +
                                "LEFT JOIN pets_accounts f ON (abp.female_id = f." + petIdCol + " OR (abp.female_id IS NULL AND abp.female_pet_name = f.name)) " +
                                "LEFT JOIN pets_accounts m ON (abp.male_id = m." + petIdCol + " OR (abp.male_id IS NULL AND abp.male_pet_name = m.name)) " +
                                "GROUP BY abp.pair_id ORDER BY abp.pairing_date DESC";
                try (Connection conn = DBConnector.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        list.add(new BreedingCardData(
                                rs.getInt("pair_id"),
                                rs.getString("female_pet_name"),
                                rs.getString("male_pet_name"),
                                rs.getString("pairing_date"),
                                rs.getString("expected_due_date"),
                                rs.getString("status"),
                                rs.getBytes("imgF"),
                                rs.getBytes("imgM")
                        ));
                    }
                }
                return list;
            }
            @Override
            protected void done() {
                try {
                    allPairs = get();
                    filterGrid("");
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private ImageIcon createCircularImage(byte[] bytes, int diameter) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
            if (src == null) return null;
            Image scaled = src.getScaledInstance(diameter, diameter, Image.SCALE_SMOOTH);
            BufferedImage mask = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = mask.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillOval(0, 0, diameter, diameter);
            g2.dispose();
            BufferedImage output = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            g2 = output.createGraphics();
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, diameter, diameter));
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            return new ImageIcon(output);
        } catch (Exception e) { return null; }
    }

    private class BreedingDetailCard extends JPanel {
        public BreedingDetailCard(PetData pet) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(15, 15, 15, 15));
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            JLabel imgLabel = new JLabel();
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            ImageIcon circular = createCircularImage(pet.image, 120);
            if (circular != null) imgLabel.setIcon(circular);
            else {
                imgLabel.setText("🐾");
                imgLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
            }
            JLabel name = new JLabel(pet.name);
            name.setFont(new Font("Segoe UI", Font.BOLD, 20));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            name.setForeground(new Color(50, 50, 50));
            JLabel info = new JLabel(pet.breed + " • " + pet.age + " yrs");
            info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.setForeground(new Color(100, 100, 100));
            content.add(imgLabel);
            content.add(Box.createVerticalStrut(15));
            content.add(name);
            content.add(Box.createVerticalStrut(5));
            content.add(info);
            add(content, BorderLayout.CENTER);
        }
    }

    record PetData(int id, String name, String breed, String age, String gender,
                   String health, String contact, String traits, String desc, byte[] image) {}

    record BreedingCardData(int id, String female, String male,
                            String date, String due, String status,
                            byte[] imgFemale, byte[] imgMale) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DemoRunner::launch);
    }

    private static class DemoRunner extends JFrame {
        private DemoRunner() {
            setTitle("Breeding Program Demo");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setContentPane(new Breeding());
            setSize(1200, 800);
            setLocationRelativeTo(null);
        }
        static void launch() {
            DemoRunner frame = new DemoRunner();
            frame.setVisible(true);
        }
    }
}