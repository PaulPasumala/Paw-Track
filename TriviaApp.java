// src/TriviaApp.java

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TriviaApp extends JFrame {
    private JScrollPane scrollPane;
    private GradientPanel mainPanel;
    private int screenWidth;

    public TriviaApp() {
        setTitle("Trivia App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Get screen dimensions and set window to full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) screenSize.getWidth();
        setSize(screenSize);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        initializeComponents();
        loadImages();

        setVisible(true);
    }

    // Static method to create trivia panel for Dashboard integration
    public static JPanel createTriviaPanel() {
        return new TriviaPanel();
    }

    private void initializeComponents() {
        mainPanel = new GradientPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadImages() {
        // Use corrected image paths assuming they are in the resources folder
        String[] imagePaths = {
                "/image/trivia.png",
                "/image/trivia (2).png",
                "/image/trivia (3).png"
        };

        for (String imagePath : imagePaths) {
            try {
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl == null) {
                    throw new IOException("Resource not found: " + imagePath);
                }
                BufferedImage image = ImageIO.read(imageUrl);

                // Scale image to fit screen width while maintaining aspect ratio
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();
                int targetWidth = screenWidth - 100; // Leave some margin
                int targetHeight = (int) ((double) imageHeight * targetWidth / imageWidth);

                Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                ImageIcon imageIcon = new ImageIcon(scaledImage);
                JLabel imageLabel = new JLabel(imageIcon);
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                mainPanel.add(imageLabel);
            } catch (IOException e) {
                JLabel errorLabel = new JLabel("Could not load image: " + imagePath);
                errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                errorLabel.setForeground(Color.RED);
                mainPanel.add(errorLabel);
            }
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Panel class for Dashboard integration
    private static class TriviaPanel extends JPanel {
        private JScrollPane scrollPane;
        private GradientPanel mainPanel;
        private boolean imagesLoaded = false;

        public TriviaPanel() {
            setLayout(new BorderLayout());
            initializeComponents();
            // Load images after a short delay to ensure proper sizing
            Timer loadTimer = new Timer(100, e -> {
                if (!imagesLoaded) {
                    loadImages();
                    imagesLoaded = true;
                }
                ((Timer) e.getSource()).stop();
            });
            loadTimer.setRepeats(false);
            loadTimer.start();
        }

        private void initializeComponents() {
            mainPanel = new GradientPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

            scrollPane = new JScrollPane(mainPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setBorder(null);

            add(scrollPane, BorderLayout.CENTER);
        }

        private void loadImages() {
            // Use corrected image paths assuming they are in the resources folder
            String[] imagePaths = {
                    "/image/trivia.png",
                    "/image/trivia (2).png",
                    "/image/trivia (3).png"
            };

            // Clear any existing components
            mainPanel.removeAll();

            // Get viewport width for proper sizing
            int viewportWidth = scrollPane.getViewport().getWidth();
            if (viewportWidth <= 0) {
                viewportWidth = 800; // Fallback default
            }

            for (String imagePath : imagePaths) {
                try {
                    URL imageUrl = getClass().getResource(imagePath);
                    if (imageUrl == null) {
                        throw new IOException("Resource not found: " + imagePath);
                    }
                    BufferedImage originalImage = ImageIO.read(imageUrl);
                    // Calculate target dimensions
                    int targetWidth = Math.max(viewportWidth - 60, 400); // Leave margin for padding
                    int originalWidth = originalImage.getWidth();
                    int originalHeight = originalImage.getHeight();
                    int targetHeight = (int) ((double) originalHeight * targetWidth / originalWidth);

                    // Create scaled image using high-quality scaling
                    BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = scaledImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
                    g2d.dispose();

                    // Create image icon and label
                    ImageIcon imageIcon = new ImageIcon(scaledImage);
                    JLabel imageLabel = new JLabel(imageIcon);
                    imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    imageLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

                    mainPanel.add(imageLabel);

                } catch (IOException e) {
                    // Create error label for missing images
                    JPanel errorPanel = new JPanel(new BorderLayout());
                    errorPanel.setOpaque(false);
                    errorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                    JLabel errorLabel = new JLabel("Could not load image: " + new File(imagePath).getName(), SwingConstants.CENTER);
                    errorLabel.setForeground(Color.WHITE);
                    errorLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                    errorLabel.setOpaque(true);
                    errorLabel.setBackground(new Color(220, 53, 69));
                    errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

                    errorPanel.add(errorLabel, BorderLayout.CENTER);
                    errorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    mainPanel.add(errorPanel);
                }
            }

            // Add bottom padding
            mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

            // Update layout
            mainPanel.revalidate();
            mainPanel.repaint();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            // Load images when component is first displayed if not already loaded
            if (!imagesLoaded) {
                SwingUtilities.invokeLater(() -> {
                    loadImages();
                    imagesLoaded = true;
                });
            }
        }
    }

    // Custom panel with optimized animated gradient background
    private static class GradientPanel extends JPanel {
        private Timer timer;
        private float hue = 0.0f;

        public GradientPanel() {
            // Reduce animation frequency for better performance
            timer = new Timer(100, e -> {
                hue += 0.005f;
                if (hue > 1.0f) hue = 0.0f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

            // Create gradient colors
            Color color1 = Color.getHSBColor(hue, 0.3f, 0.95f);
            Color color2 = Color.getHSBColor((hue + 0.3f) % 1.0f, 0.4f, 0.85f);

            GradientPaint gradient = new GradientPaint(
                    0, 0, color1,
                    getWidth(), getHeight(), color2
            );

            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            // Stop timer when component is removed to prevent memory leaks
            if (timer != null) {
                timer.stop();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new TriviaApp();
        });
    }
}