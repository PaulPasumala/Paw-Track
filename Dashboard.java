import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.net.URL;

public class Dashboard extends JFrame {

    private JPanel sidebar;
    private JPanel mainContent;
    private JPanel headerPanel;
    private CardLayout cardLayout;
    private JPopupMenu suggestionPopup;
    private JTextField currentSearchField;
    private List<String> animalCategories;
    private Map<String, List<String>> breedsByCategory;
    private PawManagement pawManagementPanel;

    private final Color SIDEBAR_START = new Color(25, 42, 86);
    private final Color SIDEBAR_END = new Color(13, 27, 62);
    private final Color BUTTON_HOVER = new Color(45, 73, 145);
    private final Color BUTTON_ACTIVE = new Color(93, 135, 255);
    private final Color TEXT_COLOR = new Color(205, 214, 244);
    private final Color ACCENT_PRIMARY = new Color(137, 180, 250);
    private final Color ACCENT_SECONDARY = new Color(245, 194, 231);
    private final Color HEADER_BACKGROUND = new Color(255, 255, 255);
    private JButton activeButton = null;


    public Dashboard() {
        setTitle("Paw Track Management Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        pawManagementPanel = new PawManagement();


        initializeSearchData();

        createModernSidebar();
        createModernHeaderPanel();
        createMainContentPanel();

        add(sidebar, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }
    public PawManagement getPawManagementPanel() {
        return pawManagementPanel;
    }

    private void initializeSearchData() {
        animalCategories = new ArrayList<>();
        breedsByCategory = new HashMap<>();

        // Dog breeds
        animalCategories.add("Dogs");
        List<String> dogBreeds = new ArrayList<>();
        dogBreeds.add("Golden Retriever");
        dogBreeds.add("Labrador Retriever");
        dogBreeds.add("German Shepherd");
        dogBreeds.add("Bulldog");
        dogBreeds.add("Poodle");
        dogBreeds.add("Beagle");
        dogBreeds.add("Rottweiler");
        dogBreeds.add("Yorkshire Terrier");
        dogBreeds.add("Dachshund");
        dogBreeds.add("Siberian Husky");
        dogBreeds.add("Boxer");
        dogBreeds.add("Shih Tzu");
        breedsByCategory.put("Dogs", dogBreeds);

        // Cat breeds
        animalCategories.add("Cats");
        List<String> catBreeds = new ArrayList<>();
        catBreeds.add("Persian");
        catBreeds.add("Maine Coon");
        catBreeds.add("Siamese");
        catBreeds.add("Ragdoll");
        catBreeds.add("British Shorthair");
        catBreeds.add("Abyssinian");
        catBreeds.add("Birman");
        catBreeds.add("Oriental Shorthair");
        catBreeds.add("Sphynx");
        catBreeds.add("Devon Rex");
        catBreeds.add("Scottish Fold");
        catBreeds.add("Russian Blue");
        breedsByCategory.put("Cats", catBreeds);

        // Bird breeds
        animalCategories.add("Birds");
        List<String> birdBreeds = new ArrayList<>();
        birdBreeds.add("Budgerigar");
        birdBreeds.add("Cockatiel");
        birdBreeds.add("Canary");
        birdBreeds.add("Lovebird");
        birdBreeds.add("Macaw");
        birdBreeds.add("Cockatoo");
        birdBreeds.add("Conure");
        birdBreeds.add("African Grey");
        birdBreeds.add("Finch");
        birdBreeds.add("Parakeet");
        breedsByCategory.put("Birds", birdBreeds);

        // Rabbit breeds
        animalCategories.add("Rabbits");
        List<String> rabbitBreeds = new ArrayList<>();
        rabbitBreeds.add("Holland Lop");
        rabbitBreeds.add("Netherland Dwarf");
        rabbitBreeds.add("Mini Rex");
        rabbitBreeds.add("Lionhead");
        rabbitBreeds.add("Angora");
        rabbitBreeds.add("Flemish Giant");
        rabbitBreeds.add("Dutch");
        rabbitBreeds.add("English Lop");
        breedsByCategory.put("Rabbits", rabbitBreeds);
    }

    private void createModernSidebar() {
        sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, SIDEBAR_START, 0, getHeight(), SIDEBAR_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        Image logoImage = null;
        try {
            java.net.URL imageUrl = getClass().getResource("/image/Logo.png");
            if (imageUrl != null) {
                logoImage = ImageIO.read(imageUrl);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error loading dashboard logo: " + e.getMessage());
        }
        JLabel logoLabel = createCircularLogo(logoImage, 120);

        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(new EmptyBorder(10, 0, 20, 0));
        sidebar.add(logoLabel);

        JLabel brandLabel = new JLabel("PawTrack");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        brandLabel.setForeground(TEXT_COLOR);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(brandLabel);

        JLabel taglineLabel = new JLabel("Management System");
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        taglineLabel.setForeground(new Color(180, 190, 210));
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(taglineLabel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(88, 91, 112));
        separator.setMaximumSize(new Dimension(200, 1));
        sidebar.add(separator);

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        sidebar.add(createModernSidebarButton("🐾", "Paw Management", "PAW_PANEL", true));
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(createModernSidebarButton("📜", "Vet Appointments", "LICENSE_PANEL", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(createModernSidebarButton("💝", "Donations", "DONATION_PANEL", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(createModernSidebarButton("🎯", "Trivia", "TRIVIA_PANEL", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(createModernSidebarButton("💰", "Shop", "SHOP_PANEL", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(createModernSidebarButton("ℹ️", "About Us", "ABOUT_PANEL", false));

        sidebar.add(Box.createVerticalGlue());
    }

    private JLabel createCircularLogo(Image image, int diameter) {
        JLabel logoLabel = new JLabel() {
            private Image originalImage;
            {
                if (image != null) {
                    this.originalImage = image;
                } else {
                    System.err.println("Could not find logo resource.");
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circular shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval(3, 3, diameter, diameter);

                // Draw white circular border
                g2d.setColor(Color.WHITE);
                g2d.fillOval(0, 0, diameter, diameter);

                // Clip to circle for image
                g2d.setClip(new Ellipse2D.Float(5, 5, diameter - 10, diameter - 10));

                if (originalImage != null) {
                    g2d.drawImage(originalImage, 5, 5, diameter - 10, diameter - 10, this);
                } else {
                    // Fallback gradient background
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT_PRIMARY, diameter, diameter, ACCENT_SECONDARY);
                    g2d.setPaint(gp);
                    g2d.fillOval(5, 5, diameter - 10, diameter - 10);

                    // Draw paw emoji
                    g2d.setClip(null);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 50));
                    g2d.setColor(Color.WHITE);
                    String emoji = "🐾";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (diameter - fm.stringWidth(emoji)) / 2;
                    int y = ((diameter - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(emoji, x, y);
                }

                g2d.dispose();
            }
        };

        logoLabel.setPreferredSize(new Dimension(diameter, diameter));
        logoLabel.setMinimumSize(new Dimension(diameter, diameter));
        logoLabel.setMaximumSize(new Dimension(diameter, diameter));

        return logoLabel;
    }

    private void createModernHeaderPanel() {
        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(HEADER_BACKGROUND);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Subtle bottom shadow
                GradientPaint shadow = new GradientPaint(0, getHeight() - 3, new Color(0, 0, 0, 15),
                        0, getHeight(), new Color(0, 0, 0, 0));
                g2d.setPaint(shadow);
                g2d.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchPanel.setOpaque(false);

        JPanel searchFieldPanel = createModernSearchField();
        searchPanel.add(searchFieldPanel);

        JButton searchButton = createModernActionButton("🔍 Search", new Color(99, 102, 241), 100);
        searchButton.addActionListener((ActionEvent e) -> {
            JTextField searchField = findSearchField(searchFieldPanel);
            if (searchField != null) {
                String searchText = searchField.getText();
                if (!searchText.trim().isEmpty() && !searchText.equals("Search paws, licenses, or contacts...")) {
                    performSearch(searchText);
                } else {
                    JOptionPane.showMessageDialog(Dashboard.this,
                            "Please enter something to search for.", "Search", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        searchPanel.add(searchButton);

        JButton adoptionButton = createModernActionButton("🏠 Adoption", new Color(236, 72, 153), 110);
        adoptionButton.addActionListener((ActionEvent e) -> handleAddNew());
        searchPanel.add(adoptionButton);

        JButton logoutButton = createModernActionButton("🚪 Logout", new Color(239, 68, 68), 90);
        logoutButton.addActionListener((ActionEvent e) -> handleLogout());

        headerPanel.add(searchPanel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
    }

    private JPanel createModernSearchField() {
        JPanel searchFieldPanel = new JPanel(new BorderLayout()) {
            private boolean hover = false;
            private float hoverProgress = 0f;
            private Timer hoverTimer;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Animated background color
                int bgR = (int)(248 + (240 - 248) * hoverProgress);
                int bgG = (int)(250 + (245 - 250) * hoverProgress);
                int bgB = (int)(252 + (255 - 252) * hoverProgress);
                g2d.setColor(new Color(bgR, bgG, bgB));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));

                // Animated border with glow effect
                if (hover && hoverProgress > 0) {
                    // Outer glow
                    int glowAlpha = (int)(20 * hoverProgress);
                    for (int i = 0; i < 3; i++) {
                        int alpha = glowAlpha - (i * 7);
                        if (alpha > 0) {
                            g2d.setColor(new Color(99, 102, 241, alpha));
                            g2d.setStroke(new BasicStroke(2.5f + i));
                            g2d.draw(new RoundRectangle2D.Float(-i, -i,
                                    getWidth() + i * 2, getHeight() + i * 2, 25 + i, 25 + i));
                        }
                    }
                }

                // Main border
                int borderR = (int)(226 + (99 - 226) * hoverProgress * 0.5f);
                int borderG = (int)(232 + (102 - 232) * hoverProgress * 0.5f);
                int borderB = (int)(240 + (241 - 240) * hoverProgress * 0.5f);
                g2d.setColor(new Color(borderR, borderG, borderB));
                g2d.setStroke(new BasicStroke(1.5f + hoverProgress * 0.5f));
                g2d.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 25, 25));

                g2d.dispose();
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        startHoverAnimation(true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        startHoverAnimation(false);
                    }
                });
            }

            private void startHoverAnimation(boolean forward) {
                if (hoverTimer != null && hoverTimer.isRunning()) {
                    hoverTimer.stop();
                }

                hoverTimer = new Timer(15, null);
                hoverTimer.addActionListener(e -> {
                    if (forward) {
                        hoverProgress += 0.1f;
                        if (hoverProgress >= 1f) {
                            hoverProgress = 1f;
                            hoverTimer.stop();
                        }
                    } else {
                        hoverProgress -= 0.1f;
                        if (hoverProgress <= 0f) {
                            hoverProgress = 0f;
                            hoverTimer.stop();
                        }
                    }
                    repaint();
                });
                hoverTimer.start();
            }
        };
        searchFieldPanel.setOpaque(false);
        searchFieldPanel.setPreferredSize(new Dimension(320, 40));

        JLabel searchIcon = new JLabel("🔍") {
            private float bounceOffset = 0f;
            private Timer bounceTimer;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.translate(0, (int)bounceOffset);
                super.paintComponent(g2d);
                g2d.dispose();
            }

            {
                searchFieldPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        startBounce();
                    }
                });
            }

            private void startBounce() {
                if (bounceTimer != null && bounceTimer.isRunning()) {
                    return;
                }

                bounceTimer = new Timer(20, null);
                final int[] step = {0};
                bounceTimer.addActionListener(e -> {
                    step[0]++;
                    bounceOffset = (float)(Math.sin(step[0] * 0.4) * 3 * Math.exp(-step[0] * 0.08));

                    if (step[0] > 25 || Math.abs(bounceOffset) < 0.1f) {
                        bounceOffset = 0f;
                        bounceTimer.stop();
                    }
                    repaint();
                });
                bounceTimer.start();
            }
        };
        searchIcon.setFont(new Font("Arial", Font.PLAIN, 16));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));

        final JTextField searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 15));
        searchField.setOpaque(false);

        final String placeholder = "Search paws, licenses, or contacts...";
        searchField.setText(placeholder);
        searchField.setForeground(new Color(148, 163, 184));

        currentSearchField = searchField;

        searchField.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(new Color(30, 41, 59));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(new Color(148, 163, 184));
                    hideSuggestions();
                }
            }
        });

        searchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideSuggestions();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = searchField.getText();
                    if (!text.trim().isEmpty() && !text.equals(placeholder)) {
                        hideSuggestions();
                        performSearch(text);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                // Always show suggestions when typing, even for categories
                if (!text.equals(placeholder)) {
                    showSuggestions(text, searchField);
                } else {
                    hideSuggestions();
                }
            }
        });

        searchFieldPanel.add(searchIcon, BorderLayout.WEST);
        searchFieldPanel.add(searchField, BorderLayout.CENTER);

        return searchFieldPanel;
    }

    private void showSuggestions(String searchText, JTextField searchField) {
        if (suggestionPopup != null) {
            suggestionPopup.setVisible(false);
        }

        List<String> suggestions = getFilteredSuggestions(searchText);

        // Always show suggestions if there's any text (even if empty suggestions)
        if (searchText.trim().isEmpty()) {
            // Show all categories when search is empty
            suggestions = new ArrayList<>(animalCategories);
        }

        if (suggestions.isEmpty()) {
            return;
        }

        suggestionPopup = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background with rounded corners
                g2d.setColor(Color.WHITE);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Subtle border
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));

                // Drop shadow
                g2d.setColor(new Color(0, 0, 0, 10));
                for (int i = 1; i <= 4; i++) {
                    g2d.draw(new RoundRectangle2D.Float(-i, -i, getWidth() + i * 2, getHeight() + i * 2, 12 + i, 12 + i));
                }

                g2d.dispose();
            }
        };
        suggestionPopup.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        suggestionPopup.setOpaque(false);

        // Group suggestions by type
        List<String> categories = new ArrayList<>();
        List<String> breeds = new ArrayList<>();

        for (String suggestion : suggestions) {
            if (animalCategories.contains(suggestion)) {
                categories.add(suggestion);
            } else {
                breeds.add(suggestion);
            }
        }

        // Add category header and items
        if (!categories.isEmpty()) {
            JLabel categoryHeader = new JLabel("  Categories");
            categoryHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
            categoryHeader.setForeground(new Color(107, 114, 128));
            categoryHeader.setOpaque(false);
            categoryHeader.setPreferredSize(new Dimension(300, 25));
            categoryHeader.setBorder(BorderFactory.createEmptyBorder(5, 10, 2, 10));
            suggestionPopup.add(categoryHeader);

            for (String category : categories) {
                JMenuItem item = createSuggestionItem(category, searchText);
                suggestionPopup.add(item);
            }
        }

        // Add breeds header and items
        if (!breeds.isEmpty()) {
            JLabel breedHeader = new JLabel("  Breeds");
            breedHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
            breedHeader.setForeground(new Color(107, 114, 128));
            breedHeader.setOpaque(false);
            breedHeader.setPreferredSize(new Dimension(300, 25));
            breedHeader.setBorder(BorderFactory.createEmptyBorder(5, 10, 2, 10));
            suggestionPopup.add(breedHeader);

            for (String breed : breeds) {
                JMenuItem item = createSuggestionItem(breed, searchText);
                suggestionPopup.add(item);
            }
        }

        // Show popup below the search field
        Point location = searchField.getLocationOnScreen();
        suggestionPopup.show(searchField, 0, searchField.getHeight() + 2);
        suggestionPopup.setLocation(location.x, location.y + searchField.getHeight() + 2);
    }

    private JMenuItem createSuggestionItem(String suggestion, String searchText) {
        JMenuItem item = new JMenuItem() {
            private boolean hover = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hover) {
                    g2d.setColor(new Color(243, 244, 246));
                    g2d.fill(new RoundRectangle2D.Float(4, 2, getWidth() - 8, getHeight() - 4, 8, 8));
                }

                super.paintComponent(g2d);
                g2d.dispose();
            }

            @Override
            public void addNotify() {
                super.addNotify();
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
        };

        // Create highlighted text
        String highlightedText = highlightSearchText(suggestion, searchText);
        item.setText("<html><div style='padding: 5px 10px;'>" + highlightedText + "</div></html>");
        item.setFont(new Font("SansSerif", Font.PLAIN, 13));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        // Add category icon
        String category = getCategoryForSuggestion(suggestion);
        String icon = getCategoryIcon(category);
        if (icon != null) {
            item.setText("<html><div style='padding: 5px 10px;'>" + icon + " " + highlightedText + "</div></html>");
        }

        item.addActionListener(e -> {
            currentSearchField.setText(suggestion);
            hideSuggestions();
            performSearch(suggestion);
        });

        return item;
    }

    private String highlightSearchText(String text, String searchText) {
        if (searchText.trim().isEmpty()) return text;

        String lowerText = text.toLowerCase();
        String lowerSearch = searchText.toLowerCase();

        int index = lowerText.indexOf(lowerSearch);
        if (index >= 0) {
            String before = text.substring(0, index);
            String match = text.substring(index, index + searchText.length());
            String after = text.substring(index + searchText.length());
            return before + "<span style='background-color: #fef3c7; color: #92400e; font-weight: bold;'>" + match + "</span>" + after;
        }
        return text;
    }

    private String getCategoryIcon(String category) {
        if (category == null) return null;
        if ("Dogs".equals(category)) return "🐕";
        if ("Cats".equals(category)) return "🐱";
        if ("Birds".equals(category)) return "🐦";
        if ("Rabbits".equals(category)) return "🐰";
        return "🐾";
    }

    private String getCategoryForSuggestion(String suggestion) {
        for (Map.Entry<String, List<String>> entry : breedsByCategory.entrySet()) {
            if (entry.getValue().contains(suggestion)) {
                return entry.getKey();
            }
        }
        if (animalCategories.contains(suggestion)) {
            return "Categories";
        }
        return null;
    }

    // This is the method that was causing the error
    private List<String> getFilteredSuggestions(String searchText) {
        List<String> suggestions = new ArrayList<>();
        String lowerSearchText = searchText.toLowerCase();

        // Always add matching categories first (prioritize them)
        for (String category : animalCategories) {
            if (category.toLowerCase().contains(lowerSearchText)) {
                suggestions.add(category);
            }
        }

        // Add matching breeds (but don't stop categories from showing)
        for (Map.Entry<String, List<String>> entry : breedsByCategory.entrySet()) {
            for (String breed : entry.getValue()) {
                if (breed.toLowerCase().contains(lowerSearchText) && !suggestions.contains(breed)) {
                    suggestions.add(breed);
                }
            }
        }

        // If no exact matches, show categories that start with the search text
        if (suggestions.isEmpty()) {
            for (String category : animalCategories) {
                if (category.toLowerCase().startsWith(lowerSearchText)) {
                    suggestions.add(category);
                }
            }
        }

        // Always try to include at least one category if search is short
        if (lowerSearchText.length() <= 3) {
            for (String category : animalCategories) {
                if (!suggestions.contains(category)) {
                    suggestions.add(0, category); // Add to beginning
                }
            }
        }

        // Limit to 8 suggestions but prioritize categories
        List<String> finalSuggestions = new ArrayList<>();

        // First add categories
        for (String suggestion : suggestions) {
            if (animalCategories.contains(suggestion)) {
                finalSuggestions.add(suggestion);
            }
        }

        // Then add breeds up to limit
        for (String suggestion : suggestions) {
            if (!animalCategories.contains(suggestion) && finalSuggestions.size() < 8) {
                finalSuggestions.add(suggestion);
            }
        }

        return finalSuggestions;
    }

    private void hideSuggestions() {
        if (suggestionPopup != null && suggestionPopup.isVisible()) {
            suggestionPopup.setVisible(false);
        }
    }

    private JTextField findSearchField(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                return (JTextField) comp;
            } else if (comp instanceof Container) {
                JTextField found = findSearchField((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JButton createModernActionButton(String text, Color baseColor, int width) {
        JButton button = new JButton(text) {
            private boolean hover = false;
            private boolean pressed = false;
            private float hoverProgress = 0f;
            private Timer hoverTimer;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int width = getWidth();
                int height = getHeight();

                // Draw glowing shadow on hover
                if (hover && hoverProgress > 0) {
                    int shadowAlpha = (int)(30 * hoverProgress);
                    for (int i = 0; i < 4; i++) {
                        int alpha = shadowAlpha - (i * 7);
                        if (alpha > 0) {
                            Color shadowColor = new Color(baseColor.getRed(), baseColor.getGreen(),
                                    baseColor.getBlue(), alpha);
                            g2d.setColor(shadowColor);
                            g2d.fill(new RoundRectangle2D.Float(-i, -i,
                                    width + i * 2, height + i * 2, 20 + i, 20 + i));
                        }
                    }
                }

                // Animated background color
                Color bgColor;
                if (pressed) {
                    bgColor = baseColor.darker().darker();
                } else if (hover) {
                    int r = (int)(baseColor.getRed() + (baseColor.darker().getRed() - baseColor.getRed()) * hoverProgress * 0.5f);
                    int gr = (int)(baseColor.getGreen() + (baseColor.darker().getGreen() - baseColor.getGreen()) * hoverProgress * 0.5f);
                    int b = (int)(baseColor.getBlue() + (baseColor.darker().getBlue() - baseColor.getBlue()) * hoverProgress * 0.5f);
                    bgColor = new Color(r, gr, b);
                } else {
                    bgColor = baseColor;
                }

                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 20, 20));

                // Animated highlight
                if (hover && hoverProgress > 0) {
                    int highlightAlpha = (int)(40 * hoverProgress);
                    g2d.setColor(new Color(255, 255, 255, highlightAlpha));
                    g2d.fill(new RoundRectangle2D.Float(0, 0, width, height / 2.5f, 20, 20));
                }

                // Shimmer effect
                if (hover && hoverProgress > 0.7f) {
                    int shimmerAlpha = (int)((hoverProgress - 0.7f) * 3.3f * 50);
                    GradientPaint shimmer = new GradientPaint(
                            0, 0, new Color(255, 255, 255, 0),
                            width, height / 2, new Color(255, 255, 255, shimmerAlpha),
                            false
                    );
                    g2d.setPaint(shimmer);
                    g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 20, 20));
                }

                g2d.dispose();
                super.paintComponent(g);
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        startHoverAnimation(true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        pressed = false;
                        startHoverAnimation(false);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            private void startHoverAnimation(boolean forward) {
                if (hoverTimer != null && hoverTimer.isRunning()) {
                    hoverTimer.stop();
                }

                hoverTimer = new Timer(15, null);
                hoverTimer.addActionListener(e -> {
                    if (forward) {
                        hoverProgress += 0.12f;
                        if (hoverProgress >= 1f) {
                            hoverProgress = 1f;
                            hoverTimer.stop();
                        }
                    } else {
                        hoverProgress -= 0.12f;
                        if (hoverProgress <= 0f) {
                            hoverProgress = 0f;
                            hoverTimer.stop();
                        }
                    }
                    repaint();
                });
                hoverTimer.start();
            }
        };

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JButton createModernSidebarButton(String icon, String text, final String panelName, boolean isFirst) {
        JButton button = new JButton() {
            private boolean hover = false;
            private boolean active = isFirst;
            private boolean pressed = false;
            private float hoverProgress = 0f;
            private Timer hoverTimer;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int width = getWidth();
                int height = getHeight();

                // Draw glowing shadow on hover
                if ((hover || active) && hoverProgress > 0) {
                    int shadowAlpha = (int)(25 * hoverProgress);
                    for (int i = 0; i < 6; i++) {
                        int alpha = shadowAlpha - (i * 4);
                        if (alpha > 0) {
                            g2d.setColor(new Color(137, 180, 250, alpha));
                            g2d.fill(new RoundRectangle2D.Float(-i, -i,
                                    width + i * 2, height + i * 2, 12 + i, 12 + i));
                        }
                    }
                }

                // Main button background
                if (active) {
                    g2d.setColor(BUTTON_ACTIVE);
                } else if (hover) {
                    int r = (int)(BUTTON_HOVER.getRed() + (BUTTON_ACTIVE.getRed() - BUTTON_HOVER.getRed()) * hoverProgress * 0.3f);
                    int gr = (int)(BUTTON_HOVER.getGreen() + (BUTTON_ACTIVE.getGreen() - BUTTON_HOVER.getGreen()) * hoverProgress * 0.3f);
                    int b = (int)(BUTTON_HOVER.getBlue() + (BUTTON_ACTIVE.getBlue() - BUTTON_HOVER.getBlue()) * hoverProgress * 0.3f);
                    g2d.setColor(new Color(r, gr, b));
                } else {
                    g2d.setColor(new Color(0, 0, 0, 0));
                }

                g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 12, 12));

                // Sliding left border indicator
                if (active) {
                    g2d.setColor(ACCENT_PRIMARY);
                    int barHeight = (int)(height * 0.6f);
                    int barY = (height - barHeight) / 2;
                    g2d.fill(new RoundRectangle2D.Float(0, barY, 4, barHeight, 4, 4));
                } else if (hover && hoverProgress > 0) {
                    g2d.setColor(new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(),
                            ACCENT_PRIMARY.getBlue(), (int)(255 * hoverProgress)));
                    int barHeight = (int)(height * 0.6f * hoverProgress);
                    int barY = (height - barHeight) / 2;
                    g2d.fill(new RoundRectangle2D.Float(0, barY, 4, barHeight, 4, 4));
                }

                // Shimmer effect on hover
                if (hover && hoverProgress > 0.5f) {
                    int shimmerAlpha = (int)((hoverProgress - 0.5f) * 2 * 35);
                    GradientPaint shimmer = new GradientPaint(
                            0, 0, new Color(255, 255, 255, 0),
                            width, 0, new Color(255, 255, 255, shimmerAlpha)
                    );
                    g2d.setPaint(shimmer);
                    g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 12, 12));
                }

                g2d.dispose();
                super.paintComponent(g);
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        startHoverAnimation(true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        pressed = false;
                        startHoverAnimation(false);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            private void startHoverAnimation(boolean forward) {
                if (hoverTimer != null && hoverTimer.isRunning()) {
                    hoverTimer.stop();
                }

                hoverTimer = new Timer(15, null);
                hoverTimer.addActionListener(e -> {
                    if (forward) {
                        hoverProgress += 0.1f;
                        if (hoverProgress >= 1f) {
                            hoverProgress = 1f;
                            hoverTimer.stop();
                        }
                    } else {
                        hoverProgress -= 0.1f;
                        if (hoverProgress <= 0f) {
                            hoverProgress = 0f;
                            hoverTimer.stop();
                        }
                    }
                    repaint();
                });
                hoverTimer.start();
            }

            public void setActive(boolean active) {
                this.active = active;
                if (active) {
                    hoverProgress = 1f;
                } else {
                    hoverProgress = 0f;
                }
                repaint();
            }
        };

        button.setLayout(new BorderLayout(10, 0));
        button.setOpaque(false);

        JLabel iconLabel = new JLabel(icon) {
            private float bounceOffset = 0f;
            private Timer bounceTimer;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.translate(0, (int)bounceOffset);
                super.paintComponent(g2d);
                g2d.dispose();
            }

            {
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        startBounce();
                    }
                });
            }

            private void startBounce() {
                if (bounceTimer != null && bounceTimer.isRunning()) {
                    return;
                }

                bounceTimer = new Timer(20, null);
                final int[] step = {0};
                bounceTimer.addActionListener(e -> {
                    step[0]++;
                    bounceOffset = (float)(Math.sin(step[0] * 0.4) * 4 * Math.exp(-step[0] * 0.08));

                    if (step[0] > 25 || Math.abs(bounceOffset) < 0.1f) {
                        bounceOffset = 0f;
                        bounceTimer.stop();
                    }
                    repaint();
                });
                bounceTimer.start();
            }
        };
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        iconLabel.setBorder(new EmptyBorder(0, 15, 0, 0));

        JLabel textLabel = new JLabel(text) {
            private Timer colorTimer;
            private float colorProgress = 0f;

            @Override
            protected void paintComponent(Graphics g) {
                // Smooth color transition on hover
                if (button.getModel().isRollover() && !isButtonActive()) {
                    if (colorProgress < 1f) {
                        startColorTransition(true);
                    }
                    int r = (int)(TEXT_COLOR.getRed() + (ACCENT_PRIMARY.getRed() - TEXT_COLOR.getRed()) * colorProgress);
                    int gr = (int)(TEXT_COLOR.getGreen() + (ACCENT_PRIMARY.getGreen() - TEXT_COLOR.getGreen()) * colorProgress);
                    int b = (int)(TEXT_COLOR.getBlue() + (ACCENT_PRIMARY.getBlue() - TEXT_COLOR.getBlue()) * colorProgress);
                    setForeground(new Color(r, gr, b));
                } else {
                    if (colorProgress > 0f && !isButtonActive()) {
                        startColorTransition(false);
                    }
                    if (!isButtonActive()) {
                        int r = (int)(TEXT_COLOR.getRed() + (ACCENT_PRIMARY.getRed() - TEXT_COLOR.getRed()) * colorProgress);
                        int gr = (int)(TEXT_COLOR.getGreen() + (ACCENT_PRIMARY.getGreen() - TEXT_COLOR.getGreen()) * colorProgress);
                        int b = (int)(TEXT_COLOR.getBlue() + (ACCENT_PRIMARY.getBlue() - TEXT_COLOR.getBlue()) * colorProgress);
                        setForeground(new Color(r, gr, b));
                    } else {
                        setForeground(Color.WHITE);
                    }
                }
                super.paintComponent(g);
            }

            private boolean isButtonActive() {
                try {
                    java.lang.reflect.Field activeField = button.getClass().getDeclaredField("active");
                    activeField.setAccessible(true);
                    return (boolean) activeField.get(button);
                } catch (Exception ex) {
                    return false;
                }
            }

            private void startColorTransition(boolean forward) {
                if (colorTimer != null && colorTimer.isRunning()) {
                    return;
                }

                colorTimer = new Timer(15, null);
                colorTimer.addActionListener(e -> {
                    if (forward) {
                        colorProgress += 0.15f;
                        if (colorProgress >= 1f) {
                            colorProgress = 1f;
                            colorTimer.stop();
                        }
                    } else {
                        colorProgress -= 0.15f;
                        if (colorProgress <= 0f) {
                            colorProgress = 0f;
                            colorTimer.stop();
                        }
                    }
                    repaint();
                });
                colorTimer.start();
            }
        };
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        textLabel.setForeground(isFirst ? Color.WHITE : TEXT_COLOR);

        button.add(iconLabel, BorderLayout.WEST);
        button.add(textLabel, BorderLayout.CENTER);

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(210, 45));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isFirst) {
            activeButton = button;
        }

        button.addActionListener((ActionEvent e) -> {
            // Special handling for Shop button
            if ("SHOP_PANEL".equals(panelName)) {
                // Open PetShop as a separate window without affecting Dashboard
                SwingUtilities.invokeLater(() -> {
                    try {
                        PetShop petShop = new PetShop(Dashboard.this); // Pass reference to this Dashboard

                        // Ensure PetShop doesn't terminate the main application
                        petShop.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                        // Add window listener to handle proper cleanup
                        petShop.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                                petShop.setVisible(false);
                                Dashboard.this.setVisible(true);
                                Dashboard.this.toFront();
                                Dashboard.this.requestFocus();
                            }
                        });

                        // Hide Dashboard while PetShop is open
                        Dashboard.this.setVisible(false);
                        petShop.setVisible(true);
                        petShop.toFront();

                    } catch (Exception ex) {
                        System.err.println("Error opening PetShop: " + ex.getMessage());
                        JOptionPane.showMessageDialog(Dashboard.this,
                                "Error opening Pet Shop: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
                return; // Don't change the active panel
            }

            if (activeButton != null && activeButton instanceof JButton) {
                try {
                    activeButton.getClass().getMethod("setActive", boolean.class).invoke(activeButton, false);
                } catch (Exception ex) {
                    // Ignore
                }
            }

            try {
                button.getClass().getMethod("setActive", boolean.class).invoke(button, true);
            } catch (Exception ex) {
                // Ignore
            }

            activeButton = button;
            cardLayout.show(mainContent, panelName);
        });

        return button;
    }

    private void createMainContentPanel() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);

        try {
            pawManagementPanel = new PawManagement();
            mainContent.add(pawManagementPanel, "PAW_PANEL");

        } catch (Exception e) {
            System.err.println("PawManagement class not found, using placeholder");
            mainContent.add(createContentPanel("Paw Management"), "PAW_PANEL");
        }

        try {
            mainContent.add(new VetAppointment(), "LICENSE_PANEL");
        } catch (Exception e) {
            System.err.println("VetAppointment class not found, using placeholder");
            mainContent.add(createContentPanel("Vet Appointments"), "LICENSE_PANEL");
        }

        try {
            Donation donation = new Donation();
            mainContent.add(donation.createDonationPanel(), "DONATION_PANEL");
        } catch (Exception e) {
            System.err.println("Donation class not found, using placeholder");
            mainContent.add(createContentPanel("Donation"), "DONATION_PANEL");
        }



        mainContent.add(createContentPanel("Contact Us"), "CONTACT_PANEL");

        try {
            mainContent.add(AboutUs.createAboutUsPanel(), "ABOUT_PANEL");
        } catch (Exception e) {
            System.err.println("AboutUs class not found, using placeholder");
            mainContent.add(createContentPanel("About Us"), "ABOUT_PANEL");
        }

        mainContent.add(createContentPanel("Buy & Sell"), "BUYSELL_PANEL");
        mainContent.add(createContentPanel("Breeding"), "BREEDING_PANEL");

        try {
            mainContent.add(TriviaApp.createTriviaPanel(), "TRIVIA_PANEL");
        } catch (Exception e) {
            System.err.println("TriviaApp class not found, using placeholder");
            mainContent.add(createContentPanel("Trivia"), "TRIVIA_PANEL");
        }
    }

    private JPanel createContentPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(243, 244, 246), 0, getHeight(), new Color(229, 231, 235));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 32));
        label.setForeground(new Color(71, 85, 105));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void performSearch(String searchText) {
        hideSuggestions();

        String category = getCategoryForSuggestion(searchText);
        String message;

        if (animalCategories.contains(searchText)) {
            List<String> breeds = breedsByCategory.get(searchText);
            message = "Showing all " + searchText.toLowerCase() + ":\n\n";
            for (String breed : breeds) {
                message += "• " + breed + "\n";
            }
        } else if (category != null) {
            message = "Found breed: " + searchText + "\nCategory: " + category + "\n\nThis breed is available for adoption!";
        } else {
            message = "Searching for: " + searchText + "\n\nThis is where you would implement the actual search functionality.";
        }

        JOptionPane.showMessageDialog(this, message, "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }



    private JButton findAdoptionButton() {
        return findButtonInPanel(headerPanel, "Adoption");
    }

    private JButton findButtonInPanel(Container container, String buttonText) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText().contains(buttonText)) {
                    return button;
                }
            } else if (comp instanceof Container) {
                Container container1 = (Container) comp;
                JButton found = findButtonInPanel(container1, buttonText);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void handleAddNew() {
        try {
            SwingUtilities.invokeLater(() -> {
                // ✅ Pass this Dashboard instance into the constructor
                AdoptionForm adoptionForm = new AdoptionForm(this);
                adoptionForm.setVisible(true);

                // Optional: refresh pets again when the form window closes
                adoptionForm.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        pawManagementPanel.reloadPets();
                    }
                });
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening Adoption Form: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void returnToDashboard(JButton adoptionButton) {
        SwingUtilities.invokeLater(() -> {

            // ✅ ADD THIS LINE:
            // This tells the PawManagement panel to reload all pets from the database.
            if (pawManagementPanel != null) {
                pawManagementPanel.reloadPets();
            }

            this.setVisible(true);
            if (adoptionButton != null) {
                adoptionButton.setEnabled(true);
            }
            this.toFront();
            this.requestFocus();
        });
    }

    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            this.dispose();

            SwingUtilities.invokeLater(() -> {
                try {
                    new PawTrackLogin().setVisible(true);
                } catch (Exception e) {
                    System.err.println("PawTrackLogin class not found: " + e.getMessage());
                    System.exit(0);
                }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}