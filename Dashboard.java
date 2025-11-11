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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.net.URL;

// Import classes needed for functionality
// Assuming other source files (PawManagement, VetAppointment, etc.) are available in the classpath
// If these throw a ClassNotFoundException, the dashboard will fall back to a placeholder panel.

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

        this.pawManagementPanel = new PawManagement();

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

        animalCategories.add("Dogs");
        breedsByCategory.put("Dogs", List.of("Golden Retriever", "Labrador Retriever", "German Shepherd", "Bulldog", "Poodle", "Beagle", "Rottweiler", "Yorkshire Terrier", "Dachshund", "Siberian Husky", "Boxer", "Shih Tzu"));

        animalCategories.add("Cats");
        breedsByCategory.put("Cats", List.of("Persian", "Maine Coon", "Siamese", "Ragdoll", "British Shorthair", "Abyssinian", "Birman", "Oriental Shorthair", "Sphynx", "Devon Rex", "Scottish Fold", "Russian Blue"));

        animalCategories.add("Birds");
        breedsByCategory.put("Birds", List.of("Budgerigar", "Cockatiel", "Canary", "Lovebird", "Macaw", "Cockatoo", "Conure", "African Grey", "Finch", "Parakeet"));

        animalCategories.add("Rabbits");
        breedsByCategory.put("Rabbits", List.of("Holland Lop", "Netherland Dwarf", "Mini Rex", "Lionhead", "Angora", "Flemish Giant", "Dutch", "English Lop"));
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
            URL url = getClass().getResource("/image/Logo.png");
            if (url != null) logoImage = ImageIO.read(url);
        } catch (IOException e) {
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

        JSeparator bottomSeparator = new JSeparator();
        bottomSeparator.setForeground(new Color(88, 91, 112));
        bottomSeparator.setMaximumSize(new Dimension(200, 1));
        sidebar.add(bottomSeparator);

        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        sidebar.add(createModernSidebarButton("👤", "Profile", "PROFILE_PANEL", false));

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel watermarkLabel = new JLabel("© 2025 PawTrack", SwingConstants.CENTER);
        watermarkLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        watermarkLabel.setForeground(new Color(120, 130, 150, 180));
        watermarkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        watermarkLabel.setMaximumSize(new Dimension(200, 20));
        sidebar.add(watermarkLabel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private JLabel createCircularLogo(Image image, int diameter) {
        JLabel logoLabel = new JLabel() {
            private Image originalImage = image;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval(3, 3, diameter, diameter);

                g2d.setColor(Color.WHITE);
                g2d.fillOval(0, 0, diameter, diameter);

                g2d.setClip(new Ellipse2D.Float(5, 5, diameter - 10, diameter - 10));

                if (originalImage != null) {
                    g2d.drawImage(originalImage, 5, 5, diameter - 10, diameter - 10, this);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT_PRIMARY, diameter, diameter, ACCENT_SECONDARY);
                    g2d.setPaint(gp);
                    g2d.fillOval(5, 5, diameter - 10, diameter - 10);

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
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                int bgR = (int)(248 + (240 - 248) * hoverProgress);
                int bgG = (int)(250 + (245 - 250) * hoverProgress);
                int bgB = (int)(252 + (255 - 252) * hoverProgress);
                g2d.setColor(new Color(bgR, bgG, bgB));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));

                if (hover && hoverProgress > 0) {
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
                    @Override public void mouseEntered(MouseEvent e) { hover = true; startHoverAnimation(true); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; startHoverAnimation(false); }
                });
            }

            private void startHoverAnimation(boolean forward) {
                if (hoverTimer != null && hoverTimer.isRunning()) { hoverTimer.stop(); }
                hoverTimer = new Timer(15, null);
                hoverTimer.addActionListener(e -> {
                    if (forward) {
                        hoverProgress += 0.1f;
                        if (hoverProgress >= 1f) { hoverProgress = 1f; hoverTimer.stop(); }
                    } else {
                        hoverProgress -= 0.1f;
                        if (hoverProgress <= 0f) { hoverProgress = 0f; hoverTimer.stop(); }
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
                    @Override public void mouseEntered(MouseEvent e) { startBounce(); }
                });
            }

            private void startBounce() {
                if (bounceTimer != null && bounceTimer.isRunning()) { return; }
                bounceTimer = new Timer(20, null);
                final int[] step = {0};
                bounceTimer.addActionListener(e -> {
                    step[0]++;
                    bounceOffset = (float)(Math.sin(step[0] * 0.4) * 3 * Math.exp(-step[0] * 0.08));
                    if (step[0] > 25 || Math.abs(bounceOffset) < 0.1f) { bounceOffset = 0f; bounceTimer.stop(); }
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
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(placeholder)) { searchField.setText(""); searchField.setForeground(new Color(30, 41, 59)); }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) { searchField.setText(placeholder); searchField.setForeground(new Color(148, 163, 184)); hideSuggestions(); }
            }
        });

        searchField.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { hideSuggestions(); }
                else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = searchField.getText();
                    if (!text.trim().isEmpty() && !text.equals(placeholder)) { hideSuggestions(); performSearch(text); }
                }
            }
            @Override public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (!text.equals(placeholder)) { showSuggestions(text, searchField); } else { hideSuggestions(); }
            }
        });

        searchFieldPanel.add(searchIcon, BorderLayout.WEST);
        searchFieldPanel.add(searchField, BorderLayout.CENTER);

        return searchFieldPanel;
    }

    private void showSuggestions(String searchText, JTextField searchField) {
        if (suggestionPopup != null) { suggestionPopup.setVisible(false); }

        List<String> suggestions = getFilteredSuggestions(searchText);
        if (suggestions.isEmpty()) { return; }

        suggestionPopup = new JPopupMenu() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2d.dispose();
            }
        };
        suggestionPopup.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        suggestionPopup.setOpaque(false);

        List<String> categories = new ArrayList<>();
        List<String> breeds = new ArrayList<>();

        for (String suggestion : suggestions) {
            if (animalCategories.contains(suggestion)) { categories.add(suggestion); } else { breeds.add(suggestion); }
        }

        if (!categories.isEmpty()) {
            JLabel categoryHeader = new JLabel("  Categories");
            categoryHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
            categoryHeader.setForeground(new Color(107, 114, 128));
            categoryHeader.setBorder(BorderFactory.createEmptyBorder(5, 10, 2, 10));
            suggestionPopup.add(categoryHeader);
            for (String category : categories) { suggestionPopup.add(createSuggestionItem(category, searchText)); }
        }

        if (!breeds.isEmpty()) {
            JLabel breedHeader = new JLabel("  Breeds");
            breedHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
            breedHeader.setForeground(new Color(107, 114, 128));
            breedHeader.setBorder(BorderFactory.createEmptyBorder(5, 10, 2, 10));
            suggestionPopup.add(breedHeader);
            for (String breed : breeds) { suggestionPopup.add(createSuggestionItem(breed, searchText)); }
        }

        Point location = searchField.getLocationOnScreen();
        suggestionPopup.show(searchField, 0, searchField.getHeight() + 2);
        suggestionPopup.setLocation(location.x, location.y + searchField.getHeight() + 2);
    }

    private JMenuItem createSuggestionItem(String suggestion, String searchText) {
        JMenuItem item = new JMenuItem() {
            private boolean hover = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) {
                    g2d.setColor(new Color(243, 244, 246));
                    g2d.fill(new RoundRectangle2D.Float(4, 2, getWidth() - 8, getHeight() - 4, 8, 8));
                }
                super.paintComponent(g2d);
                g2d.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
        };

        String highlightedText = highlightSearchText(suggestion, searchText);
        String category = getCategoryForSuggestion(suggestion);
        String icon = getCategoryIcon(category);

        item.setText("<html><div style='padding: 5px 10px;'>" + icon + " " + highlightedText + "</div></html>");
        item.setFont(new Font("SansSerif", Font.PLAIN, 13));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        item.addActionListener(e -> {
            currentSearchField.setText(suggestion);
            hideSuggestions();
            performSearch(suggestion);
        });
        return item;
    }

    private List<String> getFilteredSuggestions(String searchText) {
        List<String> suggestions = new ArrayList<>();
        String lowerSearchText = searchText.toLowerCase();

        for (String category : animalCategories) {
            if (category.toLowerCase().contains(lowerSearchText)) { suggestions.add(category); }
        }

        for (List<String> breeds : breedsByCategory.values()) {
            for (String breed : breeds) {
                if (breed.toLowerCase().contains(lowerSearchText) && !suggestions.contains(breed)) { suggestions.add(breed); }
            }
        }

        List<String> finalSuggestions = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (animalCategories.contains(suggestion)) { finalSuggestions.add(suggestion); }
        }
        for (String suggestion : suggestions) {
            if (!animalCategories.contains(suggestion) && finalSuggestions.size() < 8) { finalSuggestions.add(suggestion); }
        }
        return finalSuggestions;
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

    private String getCategoryForSuggestion(String suggestion) {
        for (Map.Entry<String, List<String>> entry : breedsByCategory.entrySet()) {
            if (entry.getValue().contains(suggestion)) { return entry.getKey(); }
        }
        return animalCategories.contains(suggestion) ? suggestion : null;
    }

    private String getCategoryIcon(String category) {
        if (category == null) return "🐾";
        if ("Dogs".equals(category)) return "🐕";
        if ("Cats".equals(category)) return "🐱";
        if ("Birds".equals(category)) return "🐦";
        if ("Rabbits".equals(category)) return "🐰";
        return "🐾";
    }

    private void hideSuggestions() {
        if (suggestionPopup != null && suggestionPopup.isVisible()) {
            suggestionPopup.setVisible(false);
        }
    }

    private JTextField findSearchField(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) { return (JTextField) comp; }
            else if (comp instanceof Container container1) { JTextField found = findSearchField(container1); if (found != null) { return found; } }
        }
        return null;
    }

    private void performSearch(String searchText) {
        hideSuggestions();
        String lowerSearchText = searchText.toLowerCase();
        String message = "Search executed for: " + searchText + "\n\n";
        String category = getCategoryForSuggestion(searchText);

        if (category != null && animalCategories.contains(searchText)) {
            List<String> breeds = breedsByCategory.get(searchText);
            message += "Category Filter: Showing all " + searchText.toLowerCase() + " breeds (" + breeds.size() + " total):\n\n";
            message += String.join(", ", breeds) + ".";
            cardLayout.show(mainContent, "PAW_PANEL");
        } else if (category != null && breedsByCategory.get(category).stream().anyMatch(b -> b.equalsIgnoreCase(searchText))) {
            message += "Breed Found: **" + searchText + "** in **" + category + "**.\n";
            message += "Action: Navigating to Paw Management and highlighting relevant listings.";
            cardLayout.show(mainContent, "PAW_PANEL");
        } else if (lowerSearchText.contains("vet") || lowerSearchText.contains("appointment") || lowerSearchText.contains("dr.")) {
            message += "System Filter: Search keyword suggests **Vet Appointments**.\n";
            message += "Action: Navigating to Vet Appointments panel. (Search term: '" + searchText + "')";
            cardLayout.show(mainContent, "LICENSE_PANEL");
        } else if (lowerSearchText.contains("profile") || lowerSearchText.contains("user")) {
            message += "System Filter: Search keyword suggests **User Profile**.\n";
            message += "Action: Navigating to Profile panel.";
            cardLayout.show(mainContent, "PROFILE_PANEL");
        } else {
            message += "General Search: No direct match found in quick filters.\n";
            message += "Action: Executing deep search across all data tables for '" + searchText + "'.";
        }

        JOptionPane.showMessageDialog(this, message, "Unified Search Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createModernActionButton(String text, Color baseColor, int width) {
        JButton button = new JButton(text) {
            private boolean hover = false;
            private boolean pressed = false;
            private float hoverProgress = 0f;
            private Timer hoverTimer;

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int width = getWidth();
                int height = getHeight();

                if (hover && hoverProgress > 0) {
                    int shadowAlpha = (int)(30 * hoverProgress);
                    for (int i = 0; i < 4; i++) {
                        int alpha = shadowAlpha - (i * 7);
                        if (alpha > 0) {
                            Color shadowColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
                            g2d.setColor(shadowColor);
                            g2d.fill(new RoundRectangle2D.Float(-i, -i, width + i * 2, height + i * 2, 20 + i, 20 + i));
                        }
                    }
                }
                Color bgColor;
                if (pressed) { bgColor = baseColor.darker().darker(); }
                else if (hover) {
                    int r = (int)(baseColor.getRed() + (baseColor.darker().getRed() - baseColor.getRed()) * hoverProgress * 0.5f);
                    int gr = (int)(baseColor.getGreen() + (baseColor.darker().getGreen() - baseColor.getGreen()) * hoverProgress * 0.5f);
                    int b = (int)(baseColor.getBlue() + (baseColor.darker().getBlue() - baseColor.getBlue()) * hoverProgress * 0.5f);
                    bgColor = new Color(r, gr, b);
                } else { bgColor = baseColor; }
                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 20, 20));
                if (hover && hoverProgress > 0) {
                    int highlightAlpha = (int)(40 * hoverProgress);
                    g2d.setColor(new Color(255, 255, 255, highlightAlpha));
                    g2d.fill(new RoundRectangle2D.Float(0, 0, width, height / 2.5f, 20, 20));
                }
                if (hover && hoverProgress > 0.7f) {
                    int shimmerAlpha = (int)((hoverProgress - 0.7f) * 3.3f * 50);
                    GradientPaint shimmer = new GradientPaint(0, 0, new Color(255, 255, 255, 0), width, height / 2, new Color(255, 255, 255, shimmerAlpha), false);
                    g2d.setPaint(shimmer);
                    g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 20, 20));
                }
                g2d.dispose();
                super.paintComponent(g);
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; startHoverAnimation(true); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; pressed = false; startHoverAnimation(false); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                });
            }

            private void startHoverAnimation(boolean forward) {
                if (hoverTimer != null && hoverTimer.isRunning()) { hoverTimer.stop(); }
                hoverTimer = new Timer(15, null);
                hoverTimer.addActionListener(e -> {
                    if (forward) {
                        hoverProgress += 0.12f;
                        if (hoverProgress >= 1f) { hoverProgress = 1f; hoverTimer.stop(); }
                    } else {
                        hoverProgress -= 0.12f;
                        if (hoverProgress <= 0f) { hoverProgress = 0f; hoverTimer.stop(); }
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

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int width = getWidth();
                int height = getHeight();

                if ((hover || active) && hoverProgress > 0) {
                    int shadowAlpha = (int)(25 * hoverProgress);
                    for (int i = 0; i < 6; i++) {
                        int alpha = shadowAlpha - (i * 4);
                        if (alpha > 0) {
                            g2d.setColor(new Color(137, 180, 250, alpha));
                            g2d.fill(new RoundRectangle2D.Float(-i, -i, width + i * 2, height + i * 2, 12 + i, 12 + i));
                        }
                    }
                }

                if (active) { g2d.setColor(BUTTON_ACTIVE); }
                else if (hover) {
                    int r = (int)(BUTTON_HOVER.getRed() + (BUTTON_ACTIVE.getRed() - BUTTON_HOVER.getRed()) * hoverProgress * 0.3f);
                    int gr = (int)(BUTTON_HOVER.getGreen() + (BUTTON_ACTIVE.getGreen() - BUTTON_HOVER.getGreen()) * hoverProgress * 0.3f);
                    int b = (int)(BUTTON_HOVER.getBlue() + (BUTTON_ACTIVE.getBlue() - BUTTON_HOVER.getBlue()) * hoverProgress * 0.3f);
                    g2d.setColor(new Color(r, gr, b));
                } else { g2d.setColor(new Color(0, 0, 0, 0)); }
                g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 12, 12));

                if (active) {
                    g2d.setColor(ACCENT_PRIMARY);
                    int barHeight = (int)(height * 0.6f);
                    int barY = (height - barHeight) / 2;
                    g2d.fill(new RoundRectangle2D.Float(0, barY, 4, barHeight, 4, 4));
                } else if (hover && hoverProgress > 0) {
                    g2d.setColor(new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), (int)(255 * hoverProgress)));
                    int barHeight = (int)(height * 0.6f * hoverProgress);
                    int barY = (height - barHeight) / 2;
                    g2d.fill(new RoundRectangle2D.Float(0, barY, 4, barHeight, 4, 4));
                }

                if (hover && hoverProgress > 0.5f) {
                    int shimmerAlpha = (int)((hoverProgress - 0.5f) * 2 * 35);
                    GradientPaint shimmer = new GradientPaint(0, 0, new Color(255, 255, 255, 0), width, 0, new Color(255, 255, 255, shimmerAlpha));
                    g2d.setPaint(shimmer);
                    g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 12, 12));
                }
                g2d.dispose();
                super.paintComponent(g);
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; startHoverAnimation(true); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; pressed = false; startHoverAnimation(false); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                });
            }

            private void startHoverAnimation(boolean forward) {
                if (hoverTimer != null && hoverTimer.isRunning()) { hoverTimer.stop(); }
                hoverTimer = new Timer(15, null);
                hoverTimer.addActionListener(e -> {
                    if (forward) {
                        hoverProgress += 0.1f;
                        if (hoverProgress >= 1f) { hoverProgress = 1f; hoverTimer.stop(); }
                    } else {
                        hoverProgress -= 0.1f;
                        if (hoverProgress <= 0f) { hoverProgress = 0f; hoverTimer.stop(); }
                    }
                    repaint();
                });
                hoverTimer.start();
            }

            public void setActive(boolean active) {
                this.active = active;
                if (active) { hoverProgress = 1f; } else { hoverProgress = 0f; }
                repaint();
            }
        };

        button.setLayout(new BorderLayout(10, 0));
        button.setOpaque(false);

        JLabel iconLabel = new JLabel(icon) {
            private float bounceOffset = 0f;
            private Timer bounceTimer;

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.translate(0, (int)bounceOffset);
                super.paintComponent(g2d);
                g2d.dispose();
            }

            { button.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { startBounce(); } }); }
            private void startBounce() {
                if (bounceTimer != null && bounceTimer.isRunning()) { return; }
                bounceTimer = new Timer(20, null);
                final int[] step = {0};
                bounceTimer.addActionListener(e -> {
                    step[0]++;
                    bounceOffset = (float)(Math.sin(step[0] * 0.4) * 4 * Math.exp(-step[0] * 0.08));
                    if (step[0] > 25 || Math.abs(bounceOffset) < 0.1f) { bounceOffset = 0f; bounceTimer.stop(); }
                    repaint();
                });
                bounceTimer.start();
            }
        };
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        iconLabel.setBorder(new EmptyBorder(0, 15, 0, 0));

        JLabel textLabel = new JLabel(text);
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

        if (isFirst) { activeButton = button; }

        button.addActionListener((ActionEvent e) -> {
            if (panelName.equals("SHOP_PANEL")) { openPetShop(); return; }

            if (activeButton != null && activeButton instanceof JButton) { try { activeButton.getClass().getMethod("setActive", boolean.class).invoke(activeButton, false); } catch (Exception ex) { /* Ignore */ } }
            try { button.getClass().getMethod("setActive", boolean.class).invoke(button, true); } catch (Exception ex) { /* Ignore */ }
            activeButton = button;
            cardLayout.show(mainContent, panelName);
        });

        return button;
    }

    private void createMainContentPanel() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);

        mainContent.add(pawManagementPanel, "PAW_PANEL");

        try { mainContent.add(new VetAppointment(), "LICENSE_PANEL"); } catch (Exception e) { mainContent.add(createContentPanel("Vet Appointments - Error"), "LICENSE_PANEL"); }
        try { Donation donation = new Donation(); mainContent.add(donation.createDonationPanel(), "DONATION_PANEL"); } catch (Exception e) { mainContent.add(createContentPanel("Donations - Error"), "DONATION_PANEL"); }
        try { JPanel triviaPanel = TriviaApp.createTriviaPanel(); mainContent.add(triviaPanel, "TRIVIA_PANEL"); } catch (Exception e) { mainContent.add(createContentPanel("Trivia - Error"), "TRIVIA_PANEL"); }

        mainContent.add(createContentPanel("Shop"), "SHOP_PANEL");

        try { mainContent.add(AboutUs.createAboutUsPanel(), "ABOUT_PANEL"); } catch (Exception e) { mainContent.add(createContentPanel("About Us - Error"), "ABOUT_PANEL"); }

        try {
            UserProfile userProfile = new UserProfile();
            JPanel profileWrapper = createDashboardWrapper(userProfile.getMainContentPanel());
            mainContent.add(profileWrapper, "PROFILE_PANEL");
        } catch (Exception e) {
            mainContent.add(createContentPanel("Profile - Error"), "PROFILE_PANEL");
        }

        cardLayout.show(mainContent, "PAW_PANEL");
    }

    // --- MISSING METHOD IMPLEMENTATION ---
    /**
     * Creates a generic content panel for sections where a full custom panel hasn't been implemented.
     */
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
    // --- END OF MISSING METHOD IMPLEMENTATION ---

    private JPanel createDashboardWrapper(JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
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
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private void handleAddNew() {
        JButton adoptionButton = findAdoptionButton();
        if (adoptionButton != null) { adoptionButton.setEnabled(false); }
        try {
            AdoptionForm adoptionForm = new AdoptionForm(this);
            adoptionForm.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent windowEvent) { returnToDashboard(adoptionButton); }
                @Override public void windowClosed(java.awt.event.WindowEvent windowEvent) { returnToDashboard(adoptionButton); }
            });
            this.setVisible(false);
            adoptionForm.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening adoption form: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            if (adoptionButton != null) { adoptionButton.setEnabled(true); }
        }
    }

    private JButton findAdoptionButton() { return findButtonInPanel(headerPanel, "Adoption"); }
    private JButton findButtonInPanel(Container container, String buttonText) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton button && button.getText().contains(buttonText)) { return button; }
            else if (comp instanceof Container container1) { JButton found = findButtonInPanel(container1, buttonText); if (found != null) { return found; } }
        }
        return null;
    }
    private void returnToDashboard(JButton adoptionButton) {
        SwingUtilities.invokeLater(() -> { this.setVisible(true); if (adoptionButton != null) { adoptionButton.setEnabled(true); } this.toFront(); this.requestFocus(); });
    }

    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> { try { new PawTrackLogin().setVisible(true); } catch (Exception e) { System.exit(0); } });
        }
    }

    private void openPetShop() {
        try {
            JButton shopButton = findShopButton();
            if (shopButton != null) { shopButton.setEnabled(false); }
            PetShop petShop = new PetShop(this);
            petShop.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent windowEvent) { returnFromPetShop(shopButton); }
                @Override public void windowClosed(java.awt.event.WindowEvent windowEvent) { returnFromPetShop(shopButton); }
            });
            this.setVisible(false);
            petShop.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening Pet Shop: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton findShopButton() { return findButtonInSidebar(sidebar, "Shop"); }
    private JButton findButtonInSidebar(Container container, String buttonText) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton button) {
                for (Component innerComp : button.getComponents()) {
                    if (innerComp instanceof JLabel label && label.getText().equals(buttonText)) { return button; }
                }
            } else if (comp instanceof Container container1) { JButton found = findButtonInSidebar(container1, buttonText); if (found != null) { return found; } }
        }
        return null;
    }

    private void returnFromPetShop(JButton shopButton) {
        SwingUtilities.invokeLater(() -> { this.setVisible(true); if (shopButton != null) { shopButton.setEnabled(true); } this.toFront(); this.requestFocus(); });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }
}