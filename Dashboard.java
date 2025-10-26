import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.geom.Ellipse2D;

public class Dashboard extends JFrame {

    private JPanel sidebar;
    private JPanel mainContent;
    private JPanel headerPanel;
    private CardLayout cardLayout;

    private final Color SIDEBAR_BACKGROUND = new Color(34, 40, 49);
    private final Color BUTTON_BACKGROUND = new Color(57, 62, 70);
    private final Color TEXT_COLOR = new Color(238, 238, 238);
    private final Color MAIN_BACKGROUND = new Color(245, 245, 245);

    public Dashboard() {
        setTitle("Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        
        // Maximize the window
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        createSidebar();
        createHeaderPanel();
        createMainContentPanel();

        add(sidebar, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(180, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

        String logoPath = "C://Users//Paul//IdeaProjects//Paw-Track/image/Logo.png";
        JLabel logoLabel;

        File logoFile = new File(logoPath);
        if (logoFile.exists()) {
            try {
                //  Use CustomImageComponent with circle clipping
                logoLabel = new CustomImageComponent(logoPath, 0);
                // Force square size so the circle is perfect
                logoLabel.setPreferredSize(new Dimension(100, 100));
                logoLabel.setMinimumSize(new Dimension(100, 100));
                logoLabel.setMaximumSize(new Dimension(100, 100));
                System.out.println("Logo loaded successfully");
            } catch (Exception e) {
                logoLabel = createFallbackLogo();
                System.err.println("Error loading logo: " + e.getMessage());
            }
        } else {
            logoLabel = createFallbackLogo();
            System.err.println("Logo file not found");
        }

        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        sidebar.add(logoLabel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        // Sidebar buttons
        sidebar.add(createSidebarButton("🐾 Paw", "PAW_PANEL"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(createSidebarButton("📜 Vet", "LICENSE_PANEL"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(createSidebarButton("🛠️ Support", "SUPPORT_PANEL"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(createSidebarButton("About Us !", "ABOUT_PANEL"));
    }
    private JLabel createFallbackLogo() {
        JLabel logoLabel = new JLabel("PawTrack");
        logoLabel.setForeground(TEXT_COLOR);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Reduced font size
        logoLabel.setPreferredSize(new Dimension(100, 100)); // Reduced size
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        return logoLabel;
    }

    private void createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12) // Further reduced padding
        ));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 45)); // Further reduced height

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(248, 249, 250));
        
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setBackground(Color.WHITE);
        searchFieldPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(1, 1, 1, 1) // Reduced padding
        ));
        searchFieldPanel.setPreferredSize(new Dimension(250, 26)); // Further reduced size
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Arial", Font.PLAIN, 14)); // Reduced font
        searchIcon.setForeground(new Color(100, 100, 100));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4)); // Reduced padding
        
        final JTextField searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12)); // Reduced font
        searchField.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 8)); // Reduced padding
        searchField.setBackground(Color.WHITE);
        
        final String placeholder = "Search paws, licenses, or contacts...";
        searchField.setText(placeholder);
        searchField.setForeground(Color.GRAY);
        
        searchField.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        
        searchField.addActionListener((ActionEvent e) -> {
            String searchText = searchField.getText();
            if (!searchText.equals(placeholder) && !searchText.trim().isEmpty()) {
                performSearch(searchText);
            }
        });
        
        searchFieldPanel.add(searchIcon, BorderLayout.WEST);
        searchFieldPanel.add(searchField, BorderLayout.CENTER);
        
        searchPanel.add(searchFieldPanel);

        JButton searchButton = new JButton("🔍 Search");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 11)); // Further reduced font
        searchButton.setBackground(new Color(40, 167, 69));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(new EmptyBorder(5, 10, 5, 10)); // Further reduced padding
        searchButton.setPreferredSize(new Dimension(75, 26)); // Further reduced size
        
        searchButton.addActionListener((ActionEvent e) -> {
            String searchText = searchField.getText();
            if (!searchText.equals(placeholder) && !searchText.trim().isEmpty()) {
                performSearch(searchText);
            } else {
                JOptionPane.showMessageDialog(Dashboard.this, 
                        "Please enter something to search for.",
                        "Search", 
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        
        searchPanel.add(Box.createRigidArea(new Dimension(8, 0))); // Reduced spacing
        searchPanel.add(searchButton);

        JButton addNewButton = new JButton("🏠 Adoption");
        addNewButton.setFont(new Font("SansSerif", Font.PLAIN, 11)); // Further reduced font
        addNewButton.setBackground(new Color(0, 123, 255));
        addNewButton.setForeground(Color.WHITE);
        addNewButton.setFocusPainted(false);
        addNewButton.setBorder(new EmptyBorder(5, 10, 5, 10)); // Further reduced padding
        addNewButton.setPreferredSize(new Dimension(85, 26)); // Further reduced size
        
        addNewButton.addActionListener((ActionEvent e) -> {
            handleAddNew();
        });
        
        searchPanel.add(Box.createRigidArea(new Dimension(8, 0))); // Reduced spacing
        searchPanel.add(addNewButton);

        JButton logoutButton = new JButton("🚪 Logout");
        logoutButton.setFont(new Font("SansSerif", Font.PLAIN, 11)); // Further reduced font
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(new EmptyBorder(5, 10, 5, 10)); // Further reduced padding
        logoutButton.setPreferredSize(new Dimension(75, 28)); // Further reduced size
        
        logoutButton.addActionListener((ActionEvent e) -> {
            handleLogout();
        });

        headerPanel.add(searchPanel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
    }

    protected void handleAddNew() {
        JButton adoptionButton = findAdoptionButton();
        if (adoptionButton != null) {
            adoptionButton.setEnabled(false);
        }
        
        try {
            JFrame adoptionFrame = new JFrame("Adoption Form - Paw Track Management");
            adoptionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            // Create the FormCanvas from AdoptionForm
            FormCanvas formCanvas = new FormCanvas();
            formCanvas.setParentFrame(this); // Set reference to dashboard
            
            JScrollPane scrollPane = new JScrollPane(formCanvas);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            adoptionFrame.add(scrollPane);
            
            // Get screen dimensions for proper sizing
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int maxWidth = Math.min(1220, (int)(screenSize.width * 0.9));
            int maxHeight = Math.min(940, (int)(screenSize.height * 0.9));
            
            adoptionFrame.setSize(maxWidth, maxHeight);
            adoptionFrame.setLocationRelativeTo(this);
            adoptionFrame.setResizable(true);
            
            // Maximize if screen is large enough
            if (screenSize.width >= 1024 && screenSize.height >= 768) {
                adoptionFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            
            adoptionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    returnToDashboard(adoptionButton);
                }
                
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    returnToDashboard(adoptionButton);
                }
            });
            
            this.setVisible(false);
            adoptionFrame.setVisible(true);
            
        } catch (HeadlessException e) {
            System.err.println("Error opening adoption form: " + e.getMessage());
            
            JOptionPane.showMessageDialog(this, 
                "Error opening adoption form: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
                
            if (adoptionButton != null) {
                adoptionButton.setEnabled(true);
            }
        }
    }
    
    private JButton findAdoptionButton() {
        return findButtonInPanel(headerPanel, "Adoption");
    }
    
    private JButton findButtonInPanel(Container container, String buttonText) {
        for (Component comp : container.getComponents()) {
            switch (comp) {
                case JButton button -> {
                    if (button.getText().contains(buttonText)) {
                        return button;
                    }
                }
                case Container container1 -> {
                    JButton found = findButtonInPanel(container1, buttonText);
                    if (found != null) {
                        return found;
                    }
                }
                default -> {
                }
            }
        }
        return null;
    }

    private void returnToDashboard(JButton adoptionButton) {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            if (adoptionButton != null) {
                adoptionButton.setEnabled(true);
            }

            // 🔄 Refresh PawManagement panel
            for (Component comp : mainContent.getComponents()) {
                if (comp instanceof PawManagement pawPanel) {
                    pawPanel.reloadPets();
                }
            }

            this.toFront();
            this.requestFocus();
        });
    }

    private void performSearch(String searchText) {
        JOptionPane.showMessageDialog(this, 
            "Searching for: " + searchText + "\n\nThis is where you would implement the actual search functionality.",
            "Search", 
            JOptionPane.INFORMATION_MESSAGE);
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

    private void createMainContentPanel() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(MAIN_BACKGROUND);

        try {
            mainContent.add(new PawManagement(), "PAW_PANEL");
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
        
        mainContent.add(createContentPanel("Support Center"), "SUPPORT_PANEL");
        mainContent.add(createContentPanel("Contact Us"), "CONTACT_PANEL");
        
        try {
            mainContent.add(AboutUs.createAboutUsPanel(), "ABOUT_PANEL");
        } catch (Exception e) {
            System.err.println("AboutUs class not found, using placeholder");
            mainContent.add(createContentPanel("About Us"), "ABOUT_PANEL");
        }
    }

    private JButton createSidebarButton(String text, final String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Further reduced from 16 to 14
        button.setBackground(BUTTON_BACKGROUND);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15)); // Further reduced padding
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));

        button.addActionListener((ActionEvent e) -> {
            cardLayout.show(mainContent, panelName);
        });

        return button;
    }

    private JPanel createContentPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BACKGROUND);
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 32));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }
}

class CustomImageComponent extends JLabel {
    @SuppressWarnings("FieldMayBeFinal")
    private Image originalImage;
    private final int cornerRadius;

    public CustomImageComponent(String imagePath, int cornerRadius) {
        this.cornerRadius = cornerRadius;
        try {
            this.originalImage = ImageIO.read(new File(imagePath));
            if (originalImage != null) {
                System.out.println("Image loaded successfully: " + imagePath + 
                    " (Size: " + originalImage.getWidth(null) + "x" + originalImage.getHeight(null) + ")");
            }
        } catch (IOException e) {
            setText("Image not found");
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            System.err.println("Could not load image from path: " + imagePath);
            this.originalImage = null;
        }
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (originalImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int componentWidth = getWidth();
            int componentHeight = getHeight();

            int imageWidth = originalImage.getWidth(this);
            int imageHeight = originalImage.getHeight(this);

            // --- Scaling logic ---
            double scaleX = (double) componentWidth / imageWidth;
            double scaleY = (double) componentHeight / imageHeight;
            double scale = Math.min(scaleX, scaleY);

            int newWidth = (int) (imageWidth * scale);
            int newHeight = (int) (imageHeight * scale);

            //  Step 2: clamp so the scaled image never exceeds the circle bounds
            newWidth = Math.min(newWidth, componentWidth);
            newHeight = Math.min(newHeight, componentHeight);

            int x = (componentWidth - newWidth) / 2;
            int y = (componentHeight - newHeight) / 2;

            // --- Circle clip ---
            Shape circle = new Ellipse2D.Float(0, 0, componentWidth, componentHeight);
            g2d.setClip(circle);

            // --- Draw the scaled image ---
            g2d.drawImage(originalImage, x, y, newWidth, newHeight, this);
            g2d.dispose();
        } else {
            super.paintComponent(g);
        }
    }}