import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.ByteArrayOutputStream;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;



public class PawManagement extends JPanel {

    private final Color BACKGROUND_START = new Color(240, 242, 255);
    private final Color BACKGROUND_END = new Color(255, 250, 255);
    private final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private final Color ACCENT_PRIMARY = new Color(99, 102, 241);
    private final Color ACCENT_SECONDARY = new Color(168, 85, 247);

    private JPanel imageGridPanel;

    public PawManagement() {
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        imageGridPanel = new JPanel(new GridLayout(0, 3, 30, 30));
        imageGridPanel.setOpaque(false);
        imageGridPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel contentWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentWrapper.add(imageGridPanel);

        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        reloadPets();
    }

    public void reloadPets() {
        imageGridPanel.removeAll();
        imageGridPanel.setLayout(new BorderLayout());
        JLabel loadingLabel = new JLabel("Loading pets, please wait...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        imageGridPanel.add(loadingLabel, BorderLayout.CENTER);

        imageGridPanel.revalidate();
        imageGridPanel.repaint();

        SwingWorker<List<JPanel>, Void> worker = new SwingWorker<List<JPanel>, Void>() {

            @Override
            protected List<JPanel> doInBackground() throws Exception {
                List<JPanel> petCards = new ArrayList<>();

                try (Connection conn = DBConnector.getConnection()) {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT name, status, image FROM pets_accounts");

                    while (rs.next()) {
                        String name = rs.getString("name");
                        String status = rs.getString("status");
                        byte[] imgBytes = rs.getBytes("image");

                        petCards.add(createModernPetCard(imgBytes, name, status));
                    }
                }

                return petCards;
            }


            @Override
            protected void done() {
                imageGridPanel.removeAll();
                imageGridPanel.setLayout(new GridLayout(0, 3, 30, 30));

                try {
                    List<JPanel> petCards = get(); // This line will now reveal the actual database exception

                    // ... (rest of display logic remains the same)
                    if (petCards.isEmpty()) {
                        imageGridPanel.setLayout(new BorderLayout());
                        JLabel noPetLabel = new JLabel("No pets found. Register a pet using the Adoption Form.", SwingConstants.CENTER);
                        noPetLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                        noPetLabel.setForeground(new Color(100, 100, 100));
                        imageGridPanel.add(noPetLabel, BorderLayout.CENTER);
                    } else {
                        for (JPanel card : petCards) {
                            imageGridPanel.add(card);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                    // --- NEW ERROR DISPLAY LOGIC ---
                    String errorMsg = "A Database error occurred: " + ex.getCause().getMessage();
                    if (ex.getCause().getMessage().contains("pets_accounts")) {
                        errorMsg = "DATABASE ERROR: Is the table 'pets_accounts' created? Or is a column misspelled?";
                    }

                    imageGridPanel.setLayout(new BorderLayout());
                    JLabel errorLabel = new JLabel(errorMsg, SwingConstants.CENTER);
                    errorLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                    errorLabel.setForeground(Color.RED);
                    imageGridPanel.add(errorLabel, BorderLayout.CENTER);
                }

                imageGridPanel.revalidate();
                imageGridPanel.repaint();
            }
        };

        worker.execute();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_PRIMARY, getWidth(), 0, ACCENT_SECONDARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("🐾 Paw Track Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(20, 40, 20, 20));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createModernPetCard(byte[] imgBytes, String petName, String status) {
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(CARD_BACKGROUND);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fill(new RoundRectangle2D.Float(3, 3, getWidth(), getHeight(), 20, 20));

                g2d.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(280, 380));
        cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cardPanel.setLocation(cardPanel.getX(), cardPanel.getY() - 5);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cardPanel.setLocation(cardPanel.getX(), cardPanel.getY() + 5);
            }
        });

        JLabel imageLabel = createRoundedImageLabel(imgBytes);
        imageLabel.setPreferredSize(new Dimension(280, 220));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        JLabel nameLabel = new JLabel(petName != null ? petName : "Unnamed Pet");

        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLabel.setForeground(new Color(30, 30, 30));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statusPanel = createStatusBadge(status);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(statusPanel);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(5, 10, 15, 10));

        JButton viewButton = createModernButton("View Details", new Color(99, 102, 241), false);
        viewButton.addActionListener(e -> {
            ViewContent viewWindow = new ViewContent((JFrame) SwingUtilities.getWindowAncestor(this), petName);
            viewWindow.setVisible(true);
        });

        JButton adoptButton = createModernButton("Adopt Now", new Color(236, 72, 153), true);

        adoptButton.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow instanceof Dashboard dashboard) {
                PetAdoptionForm petAdoptionForm = new PetAdoptionForm();
                petAdoptionForm.setParentDashboard(dashboard);
                petAdoptionForm.setVisible(true);
            }
        });

        buttonPanel.add(viewButton);
        buttonPanel.add(adoptButton);

        cardPanel.add(imageLabel, BorderLayout.NORTH);
        cardPanel.add(detailsPanel, BorderLayout.CENTER);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        return cardPanel;
    }

    // --- THIS IS THE CORRECTED METHOD ---
    private JLabel createRoundedImageLabel(byte[] imgBytes) {
        JLabel imageLabel = new JLabel() {
            private Image image;

            {
                if (imgBytes != null && imgBytes.length > 0) {
                    try {
                        // 1. Create an input stream from the byte array
                        InputStream in = new ByteArrayInputStream(imgBytes);

                        // 2. Read the image using ImageIO (much more reliable than new ImageIcon(bytes))
                        BufferedImage bufferedImage = ImageIO.read(in);

                        // 3. Scale the image
                        if (bufferedImage != null) {
                            image = bufferedImage.getScaledInstance(280, 220, Image.SCALE_SMOOTH);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // If it fails, 'image' will remain null, and the placeholder will show
                    }
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (image != null) {
                    g2d.setClip(new RoundRectangle2D.Float(5, 5, getWidth() - 10, getHeight() - 10, 15, 15));
                    g2d.drawImage(image, 5, 5, getWidth() - 10, getHeight() - 10, this);
                } else {
                    g2d.setColor(new Color(248, 250, 252));
                    g2d.fill(new RoundRectangle2D.Float(5, 5, getWidth() - 10, getHeight() - 10, 15, 15));
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 72));
                    g2d.setColor(new Color(203, 213, 225));
                    String emoji = "🐾";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(emoji)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(emoji, x, y);
                }

                g2d.dispose();
            }
        };
        return imageLabel;
    }
    // --- END OF FIX ---

    private JPanel createStatusBadge(String status) {
        JPanel badgePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color badgeColor;
                switch (status.toLowerCase()) {
                    case "available" -> badgeColor = new Color(34, 197, 94);
                    case "adopted" -> badgeColor = new Color(239, 68, 68);
                    case "in foster" -> badgeColor = new Color(251, 146, 60);
                    default -> badgeColor = new Color(148, 163, 184);
                }

                g2d.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 30));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                g2d.dispose();
            }
        };
        badgePanel.setOpaque(false);
        badgePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        badgePanel.setMaximumSize(new Dimension(150, 30));

        JLabel statusLabel = new JLabel("● " + status);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        switch (status.toLowerCase()) {
            case "available" -> statusLabel.setForeground(new Color(34, 197, 94));
            case "adopted" -> statusLabel.setForeground(new Color(239, 68, 68));
            case "in foster" -> statusLabel.setForeground(new Color(251, 146, 60));
            default -> statusLabel.setForeground(new Color(148, 163, 184));
        }

        badgePanel.add(statusLabel);
        return badgePanel;
    }

    private JButton createModernButton(String text, Color baseColor, boolean filled) {
        JButton button = new JButton(text) {
            private boolean hover = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (filled) {
                    if (hover) {
                        g2d.setColor(baseColor.darker());
                    } else {
                        g2d.setColor(baseColor);
                    }
                    g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                } else {
                    g2d.setColor(hover ? new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 30) :
                            new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 15));
                    g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                    g2d.setColor(baseColor);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
                }

                g2d.dispose();
                super.paintComponent(g);
            }

            {
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

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(filled ? Color.WHITE : baseColor);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(110, 36));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}
