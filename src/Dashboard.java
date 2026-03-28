import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Dashboard extends JFrame {

    private JPanel sidebar, mainContent, headerPanel;
    private CardLayout cardLayout;

    private final Color SIDEBAR_START = new Color(25, 42, 86);
    private final Color SIDEBAR_END = new Color(13, 27, 62);
    private final Color TEXT_COLOR = new Color(205, 214, 244);
    private final Color ACCENT_PRIMARY = new Color(137, 180, 250);
    private final Color HEADER_BACKGROUND = new Color(255, 255, 255);

    private AbstractButton activeButton = null;
    private Map<String, NavButton> navButtonsMap = new HashMap<>();
    private Map<String, Boolean> loadedPanels = new HashMap<>();

    private PawManagement pawManagementPanel;
    private UserProfile userProfile;
    private AppointmentView appointmentView;
    private VetAppointmentSystem vetForm;

    private String initialPanel = null;
    private final int NAV_HEIGHT = 44;
    private ChatOverlay chatOverlay;
    private String currentUser;

    public void createVetAppointment() {
        if (vetForm == null) loadPanelLazy("VET_FORM");
        vetForm.resetForm();
        cardLayout.show(mainContent, "VET_FORM");
    }

    public void editVetAppointment(int id, String pet, String owner, String vet, String date, String time) {
        if (vetForm == null) loadPanelLazy("VET_FORM");
        vetForm.setEditData(id, pet, owner, vet, date, time);
        cardLayout.show(mainContent, "VET_FORM");
    }

    public void showVetList() {
        if (appointmentView != null) appointmentView.loadAppointments();
        cardLayout.show(mainContent, "VET_PANEL");
    }

    public void showVetForm() { createVetAppointment(); }

    private class NavButton extends JToggleButton {
        public NavButton(String text) {
            super(text);
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setOpaque(false); setForeground(TEXT_COLOR);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(10, 20, 10, 10));
            setPreferredSize(new Dimension(240, NAV_HEIGHT));
            setMaximumSize(new Dimension(240, NAV_HEIGHT));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (getModel().isRollover() && !getModel().isSelected()) {
                g2.setColor(new Color(255,255,255,10));
                g2.fillRoundRect(6, 6, w-12, h-12, 10, 10);
            }
            if (getModel().isSelected()) {
                g2.setColor(ACCENT_PRIMARY);
                g2.fillRoundRect(6, 6, 4, h-12, 4, 4);
                g2.setColor(new Color(255,255,255,20));
                g2.fillRoundRect(16, 6, w-32, h-12, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public Dashboard(String username) {
        this.currentUser = username;
        setTitle("Paw Track Management Dashboard - " + username);
        try { URL iconUrl = getClass().getResource("/image/Logo.png"); if (iconUrl != null) setIconImage(ImageIO.read(iconUrl)); } catch (Exception e) {}

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        pawManagementPanel = new PawManagement();

        createSidebar();
        createHeader();
        createMainContent();

        if (initialPanel != null) {
            loadPanelLazy(initialPanel);
            cardLayout.show(mainContent, initialPanel);
        }

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.add(sidebar, BorderLayout.WEST);
        basePanel.add(headerPanel, BorderLayout.NORTH);
        basePanel.add(mainContent, BorderLayout.CENTER);

        chatOverlay = new ChatOverlay(this.currentUser) {
            @Override public boolean contains(int x, int y) {
                for (Component c : getComponents()) {
                    if (c.isVisible()) {
                        Point p = SwingUtilities.convertPoint(this, x, y, c);
                        if (c.contains(p)) return true;
                    }
                }
                return false;
            }
        };
        chatOverlay.setOpaque(false);

        layeredPane.add(basePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(chatOverlay, JLayeredPane.PALETTE_LAYER);

        int initialWidth = 1200;
        int initialHeight = 800;
        basePanel.setBounds(0, 0, initialWidth, initialHeight);
        chatOverlay.setBounds(0, 0, initialWidth, initialHeight);
        chatOverlay.updateComponentPositions();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = getContentPane().getWidth();
                int h = getContentPane().getHeight();
                basePanel.setBounds(0, 0, w, h);
                chatOverlay.setBounds(0, 0, w, h);
                chatOverlay.revalidate();
                chatOverlay.updateComponentPositions();
            }
        });

        setContentPane(layeredPane);
    }

    public Dashboard() { this("Guest"); }

    public PawManagement getPawManagementPanel() { return pawManagementPanel; }

    public void activateSidebarByPanelName(String targetPanelName) {
        boolean alreadyLoaded = loadedPanels.containsKey(targetPanelName);
        loadPanelLazy(targetPanelName);

        if (cardLayout != null && mainContent != null) {
            cardLayout.show(mainContent, targetPanelName);
        }

        if (alreadyLoaded) {
            if ("PAW_PANEL".equals(targetPanelName) && pawManagementPanel != null) {
                pawManagementPanel.reloadPets();
            }
            if ("VET_PANEL".equals(targetPanelName) && appointmentView != null) {
                appointmentView.loadAppointments();
            }
        }

        if (navButtonsMap.containsKey(targetPanelName)) {
            NavButton targetBtn = navButtonsMap.get(targetPanelName);
            if (activeButton != null) {
                activeButton.setSelected(false);
                activeButton.repaint();
            }
            activeButton = targetBtn;
            targetBtn.setSelected(true);
            targetBtn.repaint();
        }
    }

    // [UPDATED] Method signature now accepts petId
    public void showAdoptionForm(int petId, String petName) {
        if (cardLayout != null && mainContent != null) {
            PetAdoptionForm form = new PetAdoptionForm(petId, petName);
            form.setParentDashboard(this);
            form.setCurrentUser(this.currentUser);
            String cardName = "ADOPT_FORM_" + System.currentTimeMillis();
            mainContent.add(form, cardName);
            loadedPanels.put(cardName, true);
            cardLayout.show(mainContent, cardName);
        }
    }

    // [ADDED] Compatibility method for ViewContent.java (allows call by name only)
    public void showAdoptionForm(String petName) {
        int foundId = 0;
        try (java.sql.Connection conn = DBConnector.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT pet_id FROM pets_accounts WHERE name = ?")) {
            stmt.setString(1, petName);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                foundId = rs.getInt("pet_id");
            }
        } catch (Exception e) {
            System.err.println("Error looking up pet ID for adoption: " + e.getMessage());
        }
        // Delegate to the main method with the found ID
        showAdoptionForm(foundId, petName);
    }

    public void handleAddNew() {
        SwingUtilities.invokeLater(() -> {
            try {
                AdoptionForm registrationForm = new AdoptionForm();
                registrationForm.setParentDashboard(this);
                registrationForm.setCurrentUser(this.currentUser);
                registrationForm.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error opening form: " + e.getMessage());
            }
        });
    }

    private void createSidebar() {
        sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, SIDEBAR_START, 0, h, SIDEBAR_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(18, 10, 18, 10));

        JLabel logoLabel = new JLabel("🐾", SwingConstants.CENTER);
        try {
            URL url = getClass().getResource("/image/Logo.png");
            if (url != null) {
                BufferedImage img = ImageIO.read(url);
                logoLabel = createCircularLogo(img, 25);
            }
        } catch (Exception e) {}

        JPanel logoWrap = new JPanel(new BorderLayout());
        logoWrap.setOpaque(false);
        JLabel title = new JLabel("PawTrack");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        logoWrap.add(logoLabel, BorderLayout.CENTER);
        logoWrap.add(title, BorderLayout.SOUTH);
        logoWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logoWrap);
        sidebar.add(Box.createVerticalStrut(25));

        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        ButtonGroup navGroup = new ButtonGroup();

        addSidebarBtn(navPanel, navGroup, "🏡  Adopt a Pet", "ADOPT_PANEL", true);
        addSidebarBtn(navPanel, navGroup, "🐾  Paw Management", "PAW_PANEL", false);
        addSidebarBtn(navPanel, navGroup, "📜  Vet Appointments", "VET_PANEL", false);
        addSidebarBtn(navPanel, navGroup, "💝  Breeding", "BREEDING_PANEL", false);
        addSidebarBtn(navPanel, navGroup, "💵  Donations", "DONATION_PANEL", false);
        addSidebarBtn(navPanel, navGroup, "💰  Shop", "SHOP_PANEL", false);
        addSidebarBtn(navPanel, navGroup, "🎯  Trivia", "TRIVIA_PANEL", false);

        sidebar.add(navPanel);
        sidebar.add(Box.createVerticalGlue());

        addSidebarBtn(sidebar, navGroup, "ℹ️  About Us", "ABOUT_PANEL", false);
    }

    private void addSidebarBtn(JComponent container, ButtonGroup group, String text, String cardName, boolean isFirst) {
        NavButton btn = new NavButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        navButtonsMap.put(cardName, btn);

        btn.addActionListener(e -> {
            if (activeButton != null && activeButton != btn) {
                activeButton.setSelected(false);
                activeButton.repaint();
            }
            activeButton = btn;
            btn.setSelected(true);
            btn.repaint();

            if ("VET_PANEL".equals(cardName) && appointmentView != null) {
                appointmentView.loadAppointments();
            }

            loadPanelLazy(cardName);

            if (cardLayout != null && mainContent != null) {
                cardLayout.show(mainContent, cardName);
            }
        });
        group.add(btn);
        if (isFirst) {
            initialPanel = cardName;
            btn.setSelected(true);
            activeButton = btn;
        }
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        centerPanel.setOpaque(false);

        JTextField searchField = new JTextField();
        searchField.setColumns(25);
        searchField.setPreferredSize(new Dimension(300, 38));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(5, 10, 5, 10)
        ));

        Theme.ModernButton searchBtn = new Theme.ModernButton("Search", Theme.PRIMARY);
        searchBtn.setPreferredSize(new Dimension(100, 38));

        Theme.ModernButton registerBtn = new Theme.ModernButton(" Register a Pet", new Color(99, 102, 241));
        registerBtn.setPreferredSize(new Dimension(160, 38));
        registerBtn.addActionListener(e -> handleAddNew());

        centerPanel.add(searchField);
        centerPanel.add(searchBtn);
        centerPanel.add(registerBtn);

        headerPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);

        Theme.ModernButton profileBtn = new Theme.ModernButton("Profile", Theme.PRIMARY);
        profileBtn.setPreferredSize(new Dimension(110, 38));
        profileBtn.addActionListener(e -> {
            loadPanelLazy("PROFILE_PANEL");
            if (userProfile != null) {
                userProfile.refresh();
            }
            cardLayout.show(mainContent, "PROFILE_PANEL");
        });

        Theme.ModernButton logoutBtn = new Theme.ModernButton("Logout", Theme.DANGER);
        logoutBtn.setPreferredSize(new Dimension(90, 38));
        logoutBtn.addActionListener(e -> {
            dispose();
            new PawTrackLogin().setVisible(true);
        });

        rightPanel.add(profileBtn);
        rightPanel.add(logoutBtn);
        headerPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void createMainContent() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
    }

    private void loadPanelLazy(String cardName) {
        if (loadedPanels.containsKey(cardName)) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            switch (cardName) {
                case "ADOPT_PANEL":
                    mainContent.add(new Adopt(), cardName);
                    break;
                case "PAW_PANEL":
                    mainContent.add(pawManagementPanel, cardName);
                    break;
                case "VET_PANEL":
                    if (appointmentView == null) {
                        try {
                            appointmentView = new AppointmentView(this.currentUser);
                            appointmentView.setDashboard(this);
                            mainContent.add(appointmentView, "VET_PANEL");
                        } catch(Exception e) {}
                    }
                    break;
                case "VET_FORM":
                    if (vetForm == null) {
                        try {
                            vetForm = new VetAppointmentSystem(true);
                            vetForm.setCurrentUser(this.currentUser);
                            vetForm.setDashboard(this);
                            mainContent.add(vetForm.getMainPanel(), "VET_FORM");
                        } catch(Exception e) { e.printStackTrace(); }
                    }
                    break;
                case "BREEDING_PANEL":
                    try { mainContent.add(new Breeding(), cardName); } catch(Exception e){}
                    break;
                case "DONATION_PANEL":
                    try {
                        Donation donation = new Donation();
                        mainContent.add(donation.createDonationPanel(), cardName);
                    } catch(Exception e){}
                    break;
                case "SHOP_PANEL":
                    try {
                        PetShop shop = new PetShop(true);
                        mainContent.add(shop.getMainPanel(), cardName);
                    } catch(Exception e){}
                    break;
                case "TRIVIA_PANEL":
                    try { mainContent.add(PetTrivia.createTriviaPanel(), cardName); } catch(Exception e){}
                    break;
                case "PROFILE_PANEL":
                    try {
                        userProfile = new UserProfile(this.currentUser);
                        mainContent.add(userProfile.getMainContentPanel(), cardName);
                    } catch(Exception e){}
                    break;
                case "ABOUT_PANEL":
                    try { mainContent.add(AboutUs.createAboutUsPanel(), cardName); } catch(Exception e){}
                    break;
            }
            loadedPanels.put(cardName, true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private JLabel createCircularLogo(Image image, int size) {
        JLabel label = new JLabel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int min = Math.min(w, h);
                int cx = (w - min) / 2;
                int cy = (h - min) / 2;
                Shape clip = new Ellipse2D.Float(cx, cy, min, min);
                g2.setClip(clip);
                if (image != null) g2.drawImage(image, cx, cy, min, min, null);
                g2.dispose();
            }
        };
        label.setPreferredSize(new Dimension(size, size));
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard("Admin").setVisible(true));
    }
}