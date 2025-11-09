import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * A Java Swing application that replicates the Pet Shop UI with only 2 repeating items for Dashboard integration.
 */
public class PetShop extends JFrame {

    // Only keep the allProductNames for the full shop display
    private static final String[] allProductNames = {
        "Pedigree Dog Food", "Meow Mix Cat Food", "Dog Leash", "Pet Toys", 
        "Grooming Supplies", "Fish Supplies", "Bird Supplies", "Small Animal", 
        "Pet Health", "Reptile Heat Lamp", "Aquarium Filter", "Rabbit Hutch", 
        "Cat Tree", "Dog Bed", "Fish Flakes", "Hamster Wheel", "Parrot Cage", 
        "Dog Shampoo", "Cat Litter", "Flea Treatment", "Dog Crate", "Bird Seed", 
        "Guinea Pig Hay", "Turtle Dock", "Cat Scratch Post", "Dog Harness", 
        "Pet Wipes", "Aquarium Gravel", "Lizard Food", "Chew Toys", "Feather Wand", 
        "Dog Treats", "Cat Treats", "Water Bowl", "Food Bowl", "Pet Carrier", 
        "Nail Clippers", "Brush", "Pet Cologne", "Vitamin Drops", "Fish Tank", 
        "Dog Jacket", "Cat Bed", "Hamster Bedding", "Bird Bath", "Snake Substrate", 
        "Dog Training Pads", "Poop Bags", "Dental Chews", "Muzzle"
    };

    private Dashboard parentDashboard; // Add reference to parent Dashboard

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PetShop().setVisible(true));
    }

    /**
     * Constructor for the PetShop JFrame with parent Dashboard reference.
     */
    public PetShop(Dashboard parent) {
        this.parentDashboard = parent;
        initializeShop();
    }

    /**
     * Default constructor for standalone usage.
     */
    public PetShop() {
        this.parentDashboard = null;
        initializeShop();
    }

    /**
     * Initialize the shop UI components.
     */
    private void initializeShop() {
        setTitle("Pet Shop");
        setSize(1200, 900);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set window to maximized state (full display screen)
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Create header with neon title and back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#3B82F6"));
        headerPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Back button on the left - simplified and more stable
        JButton backButton = new JButton("← BACK");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(true);
        backButton.setContentAreaFilled(true);
        backButton.setOpaque(true);
        backButton.setPreferredSize(new Dimension(100, 40));
        backButton.addActionListener(e -> {
            this.setVisible(false);
            if (parentDashboard != null) {
                // Return to existing Dashboard
                parentDashboard.setVisible(true);
                parentDashboard.toFront();
                parentDashboard.requestFocus();
            } else {
                // Fallback: create new Dashboard if no parent reference
                try {
                    new Dashboard().setVisible(true);
                } catch (Exception ex) {
                    System.err.println("Error opening Dashboard: " + ex.getMessage());
                }
            }
        });
        
        // Title in the center
        NeonLabel titleLabel = new NeonLabel("SHOP", 70);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);

        // Create main grid with improved spacing
        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 25, 25));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Add all 50 products
        for (String productName : allProductNames) {
            gridPanel.add(createProductCard(productName));
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates a single product card panel for the full shop view.
     */
    private JPanel createProductCard(String productName) {
        GradientPanel cardPanel = new GradientPanel();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        cardPanel.setPreferredSize(new Dimension(320, 380));

        // Image container with better spacing
        JPanel imageBgPanel = new JPanel(new BorderLayout());
        imageBgPanel.setBackground(Color.WHITE);
        imageBgPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel imageLabel = new JLabel("Loading...", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Inter", Font.BOLD, 16));
        imageLabel.setPreferredSize(new Dimension(220, 220));
        imageBgPanel.add(imageLabel, BorderLayout.CENTER);

        loadProductImage(imageLabel, productName);
        cardPanel.add(imageBgPanel, BorderLayout.CENTER);

        // Product name label with spacing
        JLabel nameLabel = new JLabel(productName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Inter", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        cardPanel.add(nameLabel, BorderLayout.NORTH);

        // Button panel with improved spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 10, 10, 10));

        JButton viewButton = new JButton("VIEW");
        viewButton.setBackground(Color.decode("#3B82F6"));
        viewButton.setForeground(Color.WHITE);
        viewButton.setFont(new Font("Inter", Font.BOLD, 14));
        viewButton.setFocusPainted(false);
        viewButton.setBorder(new EmptyBorder(12, 30, 12, 30));
        viewButton.setPreferredSize(new Dimension(120, 40));
        
        buttonPanel.add(viewButton);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        return cardPanel;
    }

    /**
     * Loads a product image in a background thread using SwingWorker.
     */
    private void loadProductImage(JLabel label, String productName) {
        String imageUrl = "https://placehold.co/400x400/ffffff/cccccc?text=" + 
                          productName.replace(" ", "+");

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                try {
                    URL url = new URL(imageUrl);
                    BufferedImage image = ImageIO.read(url);
                    Image scaledImage = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + productName);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        label.setIcon(icon);
                        label.setText(null);
                    } else {
                        label.setText("Image Error");
                        label.setIcon(null);
                    }
                } catch (Exception e) {
                    label.setText("Load Failed");
                    label.setIcon(null);
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }
}

/**
 * A custom JPanel that paints a vertical gradient background.
 */
class GradientPanel extends JPanel {
    // Gradient colors from the HTML (red-400, pink-500, purple-600)
    private static final Color START_COLOR = Color.decode("#F87171"); // 'from-red-400'
    private static final Color MID_COLOR = Color.decode("#EC4899");   // 'via-pink-500'
    private static final Color END_COLOR = Color.decode("#A855F7");   // 'to-purple-600'

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Create a multi-stop gradient
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(0, getHeight());
        float[] fractions = {0.0f, 0.5f, 1.0f};
        Color[] colors = {START_COLOR, MID_COLOR, END_COLOR};
        
        LinearGradientPaint gp = new LinearGradientPaint(start, end, fractions, colors);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}

/**
 * A custom JLabel that renders text with a neon glow effect.
 */
class NeonLabel extends JLabel {
    private int fontSize;
    private static final Color GLOW_COLOR = Color.decode("#8A2BE2"); // Violet/Purple glow

    public NeonLabel(String text, int fontSize) {
        super(text);
        this.fontSize = fontSize;
        setFont(new Font("Inter", Font.BOLD, fontSize));
        setForeground(Color.WHITE);
    }

    @Override
    public Dimension getPreferredSize() {
        // Add extra space for the glow effect
        Dimension d = super.getPreferredSize();
        d.width += 30;
        d.height += 30;
        return d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Enable anti-aliasing for smooth text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        // 1. Draw the blurred glow layers
        g2.setColor(new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), 70)); // Faint outer glow
        g2.drawString(getText(), x, y);
        g2.setStroke(new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(getFont().createGlyphVector(g2.getFontRenderContext(), getText()).getOutline(x, y));

        g2.setColor(new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), 100)); // Medium glow
        g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(getFont().createGlyphVector(g2.getFontRenderContext(), getText()).getOutline(x, y));

        // 2. Draw the inner white glow
        g2.setColor(new Color(255, 255, 255, 200)); // White glow
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(getFont().createGlyphVector(g2.getFontRenderContext(), getText()).getOutline(x, y));

        // 3. Draw the crisp white text on top
        g2.setColor(Color.WHITE);
        g2.drawString(getText(), x, y);
        
        g2.dispose();
    }
}

