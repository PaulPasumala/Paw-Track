import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class BreedingTinderDialog extends JDialog {

    private final Color ACCENT_PINK = new Color(200, 150, 220);
    private final Color ACCENT_PURPLE = new Color(180, 140, 210);
    private final Color LIKE_COLOR = new Color(34, 197, 94);
    private final Color NOPE_COLOR = new Color(239, 68, 68);
    private final Color BG_COLOR = new Color(200, 150, 220);
    private final Color CARD_BG = new Color(245, 240, 250);

    private JPanel cardPanel;
    private List<PetCard> petCards = new ArrayList<>();
    private int currentIndex = 0;
    private PetCard selectedFemale = null;
    private JLabel instructionLabel;
    private Breeding parentPanel;

    private Point initialClick;

    public BreedingTinderDialog(JFrame parent, Breeding breedingPanel) {
        super(parent, "Find a Perfect Match 💕", true);
        this.parentPanel = breedingPanel;

        setSize(600, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        add(createHeader(), BorderLayout.NORTH);
        add(createCardArea(), BorderLayout.CENTER);
        add(createControls(), BorderLayout.SOUTH);

        loadPets();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 80));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel title = new JLabel("🔥 Pet Matcher");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        instructionLabel = new JLabel("Select a female pet to start");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instructionLabel.setForeground(new Color(255, 255, 255, 200));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(3));
        content.add(instructionLabel);

        header.add(content, BorderLayout.CENTER);
        return header;
    }

    private JPanel createCardArea() {
        JPanel container = new JPanel(null);
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 50, 30, 50));

        cardPanel = new JPanel(null);
        cardPanel.setOpaque(false);
        cardPanel.setBounds(0, 0, 500, 450);

        container.add(cardPanel);
        return container;
    }

    private JPanel createControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 20));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(0, 0, 40, 0));

        JButton nopeBtn = createIconButton("✕", NOPE_COLOR, 80);
        nopeBtn.addActionListener(e -> swipeLeft());

        JButton likeBtn = createIconButton("✓", LIKE_COLOR, 80);
        likeBtn.addActionListener(e -> swipeRight());

        controls.add(nopeBtn);
        controls.add(likeBtn);

        return controls;
    }

    private JButton createIconButton(String icon, Color color, int size) {
        JButton btn = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillOval(3, 5, getWidth() - 6, getHeight() - 6);

                g2.setColor(new Color(220, 220, 230));
                g2.fillOval(0, 0, getWidth() - 3, getHeight() - 3);

                g2.setColor(color);
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(icon, x, y);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(size, size));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void loadPets() {
        petCards.clear();
        String sql = "SELECT pet_id, name, breed, gender, age FROM pets_accounts WHERE status = 'Pairing'";

        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                petCards.add(new PetCard(
                        rs.getInt("pet_id"),
                        rs.getString("name"),
                        "Pet",
                        rs.getString("breed"),
                        rs.getString("gender"),
                        rs.getInt("age")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        showNextCard();
    }

    private void showNextCard() {
        cardPanel.removeAll();

        if (currentIndex >= petCards.size()) {
            if (selectedFemale == null) {
                JOptionPane.showMessageDialog(this, "No more pets available!", "Info", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                instructionLabel.setText("No more males available. Restarting...");
                currentIndex = 0;
                selectedFemale = null;
                instructionLabel.setText("Select a female pet to start");
                showNextCard();
            }
            return;
        }

        PetCard card = petCards.get(currentIndex);

        if (selectedFemale != null && !"Male".equalsIgnoreCase(card.gender)) {
            currentIndex++;
            showNextCard();
            return;
        }
        if (selectedFemale == null && !"Female".equalsIgnoreCase(card.gender)) {
            currentIndex++;
            showNextCard();
            return;
        }

        JPanel visualCard = createVisualCard(card);
        visualCard.setBounds(0, 0, 500, 450);
        cardPanel.add(visualCard);
        cardPanel.revalidate();
        cardPanel.repaint();

        addDragListener(visualCard, card);
    }

    private JPanel createVisualCard(PetCard pet) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(8, 8, getWidth() - 10, getHeight() - 10, 35, 35));
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 10, getHeight() - 15, 35, 35));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color1 = new Color(190, 140, 220);
                Color color2 = new Color(170, 120, 200);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };
        imagePanel.setPreferredSize(new Dimension(380, 240));
        imagePanel.setMaximumSize(new Dimension(380, 240));
        imagePanel.setLayout(new GridBagLayout());

        JLabel imageLabel = new JLabel("IMAGE");
        imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        imageLabel.setForeground(new Color(60, 40, 80));
        imagePanel.add(imageLabel);

        JLabel nameLabel = new JLabel(pet.name.toUpperCase());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        nameLabel.setForeground(new Color(20, 20, 20));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel breedLabel = new JLabel(pet.breed);
        breedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        breedLabel.setForeground(new Color(100, 100, 100));
        breedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(imagePanel);
        content.add(Box.createVerticalStrut(20));
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(breedLabel);
        content.add(Box.createVerticalStrut(30));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void addDragListener(JPanel card, PetCard pet) {
        card.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        card.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = card.getLocation().x;
                int thisY = card.getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                card.setLocation(X, Y);
            }
        });

        card.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                int x = card.getLocation().x;
                if (x < -100) {
                    animateSwipe(card, -600, () -> swipeLeft());
                } else if (x > 100) {
                    animateSwipe(card, 600, () -> swipeRight());
                } else {
                    animateReturn(card);
                }
            }
        });
    }

    private void animateSwipe(JPanel card, int targetX, Runnable onComplete) {
        Timer timer = new Timer(5, null);
        timer.addActionListener(e -> {
            int currentX = card.getLocation().x;
            int newX = currentX + (targetX > 0 ? 20 : -20);
            if ((targetX > 0 && newX >= targetX) || (targetX < 0 && newX <= targetX)) {
                timer.stop();
                onComplete.run();
            } else {
                card.setLocation(newX, card.getLocation().y);
            }
        });
        timer.start();
    }

    private void animateReturn(JPanel card) {
        Timer timer = new Timer(5, null);
        timer.addActionListener(e -> {
            int currentX = card.getLocation().x;
            int diff = -currentX / 5;
            if (Math.abs(currentX) < 5) {
                card.setLocation(0, 0);
                timer.stop();
            } else {
                card.setLocation(currentX + diff, card.getLocation().y);
            }
        });
        timer.start();
    }

    private void swipeLeft() {
        currentIndex++;
        showNextCard();
    }

    private void swipeRight() {
        PetCard current = petCards.get(currentIndex);
        if (selectedFemale == null) {
            selectedFemale = current;
            instructionLabel.setText("Great! Now find a male match for " + selectedFemale.name);
            currentIndex++;
            showNextCard();
        } else {
            createBreedingPair(selectedFemale, current);
        }
    }

    // [UPDATED] Direct Pairing Logic
    private void createBreedingPair(PetCard female, PetCard male) {
        String pairingDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String dueDate = LocalDate.now().plusDays(65).format(DateTimeFormatter.ISO_DATE);

        String sql = "INSERT INTO active_breeding_pairs (female_pet_name, male_pet_name, pairing_date, expected_due_date, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, female.name);
            pstmt.setString(2, male.name);
            pstmt.setString(3, pairingDate);
            pstmt.setString(4, dueDate);

            // [CHANGED] Status to Expecting immediately
            pstmt.setString(5, "Expecting");

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Perfect Match! 💕\n" + female.name + " & " + male.name + " are now paired!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            parentPanel.loadBreedingPairs();
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating pair: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class PetCard {
        int id;
        String name;
        String species;
        String breed;
        String gender;
        int age;

        PetCard(int id, String name, String species, String breed, String gender, int age) {
            this.id = id;
            this.name = name;
            this.species = species;
            this.breed = breed;
            this.gender = gender;
            this.age = age;
        }
    }
}