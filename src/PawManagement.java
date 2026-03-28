import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class PawManagement extends JPanel {

    private final Color BACKGROUND_START = new Color(147, 197, 253);
    private final Color BACKGROUND_END = new Color(249, 168, 212);
    private final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private final Color ACCENT_PRIMARY = new Color(99, 102, 241);
    private final Color ACCENT_SECONDARY = new Color(168, 85, 247);
    private final Color TEXT_MUTED = new Color(100, 116, 139);

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
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scrollPane, BorderLayout.CENTER);

        reloadPets();
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

    public void reloadPets() {
        imageGridPanel.removeAll();
        imageGridPanel.setLayout(new BorderLayout());
        JLabel loadingLabel = new JLabel("Loading pets from database...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        imageGridPanel.add(loadingLabel, BorderLayout.CENTER);
        imageGridPanel.revalidate();
        imageGridPanel.repaint();

        SwingWorker<List<JPanel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JPanel> doInBackground() throws Exception {
                List<JPanel> cards = new ArrayList<>();
                try (Connection conn = DBConnector.getConnection()) {
                    Statement stmt = conn.createStatement();

                    // [ADDED] pet_id to query
                    String sql = "SELECT pet_id, name, status, breed, gender, age, image, reason_for_adoption FROM pets_accounts WHERE status = 'Available'";

                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        cards.add(createModernPetCard(
                                rs.getInt("pet_id"), // [ADDED]
                                rs.getBytes("image"),
                                rs.getString("name"),
                                rs.getString("status"),
                                rs.getString("breed"),
                                rs.getString("gender"),
                                rs.getString("age"),
                                rs.getString("reason_for_adoption")
                        ));
                    }
                }
                return cards;
            }

            @Override
            protected void done() {
                imageGridPanel.removeAll();
                imageGridPanel.setLayout(new GridLayout(0, 3, 30, 30));
                try {
                    List<JPanel> cards = get();
                    if (cards.isEmpty()) {
                        imageGridPanel.setLayout(new BorderLayout());
                        JLabel noPetLabel = new JLabel("No available pets for adoption right now.", SwingConstants.CENTER);
                        noPetLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                        imageGridPanel.add(noPetLabel, BorderLayout.CENTER);
                    } else {
                        for (JPanel card : cards) {
                            imageGridPanel.add(card);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    imageGridPanel.setLayout(new BorderLayout());
                    JLabel errorLabel = new JLabel("DB Error: " + ex.getMessage(), SwingConstants.CENTER);
                    errorLabel.setForeground(Color.RED);
                    imageGridPanel.add(errorLabel, BorderLayout.CENTER);
                }
                imageGridPanel.revalidate();
                imageGridPanel.repaint();
            }
        };
        worker.execute();
    }

    // [UPDATED] Method signature includes int petId
    private JPanel createModernPetCard(int petId, byte[] imgBytes, String petName, String status, String breed, String gender, String age, String description) {
        final Color cardColor;
        if ("Female".equalsIgnoreCase(gender)) {
            cardColor = new Color(252, 231, 243);
        } else if ("Male".equalsIgnoreCase(gender)) {
            cardColor = new Color(219, 234, 254);
        } else {
            cardColor = CARD_BACKGROUND;
        }

        JPanel cardPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(cardColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fill(new RoundRectangle2D.Float(3, 3, getWidth(), getHeight(), 20, 20));
                g2d.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(280, 420));
        cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cardPanel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { cardPanel.setLocation(cardPanel.getX(), cardPanel.getY() - 5); }
            public void mouseExited(MouseEvent e) { cardPanel.setLocation(cardPanel.getX(), cardPanel.getY() + 5); }
        });

        JLabel imageLabel = createRoundedImageLabel(imgBytes);
        imageLabel.setPreferredSize(new Dimension(280, 200));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(12, 20, 5, 20));

        JLabel nameLabel = new JLabel(petName == null ? "Unknown" : petName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel breedLabel = new JLabel(breed == null || breed.isEmpty() ? "Unknown Breed" : breed);
        breedLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        breedLabel.setForeground(TEXT_MUTED);
        breedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        metaPanel.setOpaque(false);
        metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        String genderText = (gender != null && !gender.isEmpty()) ? gender : "?";
        String ageText = (age != null && !age.isEmpty()) ? age + " yrs" : "?";
        JLabel metaLabel = new JLabel(genderText + " • " + ageText);
        metaLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        metaLabel.setForeground(TEXT_MUTED);
        metaPanel.add(metaLabel);

        JPanel statusPanel = createStatusBadge(status);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        detailsPanel.add(breedLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        detailsPanel.add(metaPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(statusPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(5, 10, 15, 10));

        JButton viewButton = createModernButton("View Details", new Color(99, 102, 241), false);
        viewButton.addActionListener(e -> {
            ViewContent view = new ViewContent(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    petName, breed, gender, age, imgBytes, description
            );
            view.setVisible(true);
        });

        JButton adoptButton = createModernButton("Adopt Now", new Color(236, 72, 153), true);

        if ("Adopted".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
            adoptButton.setEnabled(false);
            adoptButton.setText(status);
        }

        adoptButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof Dashboard dash) {
                // [UPDATED] Pass petId
                dash.showAdoptionForm(petId, petName);
            }
        });

        buttonPanel.add(viewButton);
        buttonPanel.add(adoptButton);

        cardPanel.add(imageLabel, BorderLayout.NORTH);
        cardPanel.add(detailsPanel, BorderLayout.CENTER);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        return cardPanel;
    }

    private JLabel createRoundedImageLabel(byte[] imgBytes) {
        return new JLabel() {
            Image img;
            {
                if(imgBytes!=null && imgBytes.length>0) {
                    try {
                        InputStream in = new ByteArrayInputStream(imgBytes);
                        img = ImageIO.read(in);
                    } catch(Exception e){}
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (img != null) {
                    g2.setClip(new RoundRectangle2D.Float(5, 5, getWidth() - 10, getHeight() - 10, 15, 15));
                    g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
                }
                g2.dispose();
            }
        };
    }

    private JPanel createStatusBadge(String status) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c;
                if (status == null) c = Color.GRAY;
                else if (status.equalsIgnoreCase("Available")) c = new Color(34, 197, 94);
                else if (status.equalsIgnoreCase("Adopted")) c = new Color(239, 68, 68);
                else if (status.equalsIgnoreCase("In Foster")) c = new Color(251, 146, 60);
                else if (status.equalsIgnoreCase("Pending")) c = new Color(234, 179, 8);
                else c = Color.GRAY;

                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        badge.setMaximumSize(new Dimension(150, 30));

        JLabel statusLabel = new JLabel("● " + (status == null ? "Unknown" : status));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(34, 197, 94));

        badge.add(statusLabel);
        return badge;
    }

    private JButton createModernButton(String text, Color baseColor, boolean filled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (filled && isEnabled()) {
                    g2.setColor(getModel().isRollover() ? baseColor.darker() : baseColor);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                } else if (!isEnabled()) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                } else {
                    g2.setColor(getModel().isRollover() ? new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 30) : new Color(0, 0, 0, 0));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(baseColor);
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(filled ? Color.WHITE : baseColor);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(110, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
            JButton jbutton = new JButton();
            jbutton.setPreferredSize(new Dimension(0, 0));
            return jbutton;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }
    }
}