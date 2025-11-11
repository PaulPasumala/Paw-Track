// src/Donation.java

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Donation {

    public void displayImage() {
        JFrame frame = new JFrame("Donation Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize);

        // Load and scale the image to fit the screen
        ImageIcon originalIcon = null;
        try {
            URL imageUrl = getClass().getResource("/image/DONATION.png");
            if (imageUrl != null) {
                originalIcon = new ImageIcon(ImageIO.read(imageUrl));
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
        }

        final ImageIcon finalIcon = originalIcon;

        JLabel imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (finalIcon != null && finalIcon.getIconWidth() > 0) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imageWidth = finalIcon.getIconWidth();
                    int imageHeight = finalIcon.getIconHeight();

                    // Calculate scaling factor to fit image within panel while maintaining aspect ratio
                    double scaleX = (double) panelWidth / imageWidth;
                    double scaleY = (double) panelHeight / imageHeight;
                    // Scale to 90% for padding
                    double scale = Math.min(scaleX, scaleY) * 0.9;

                    int scaledWidth = (int) (imageWidth * scale);
                    int scaledHeight = (int) (imageHeight * scale);

                    // Center the image
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;

                    g2d.drawImage(finalIcon.getImage(), x, y, scaledWidth, scaledHeight, this);
                    g2d.dispose();
                } else {
                    // Fallback text if image not found
                    g.setFont(new Font("SansSerif", Font.BOLD, 32));
                    g.setColor(new Color(71, 85, 105));
                    FontMetrics fm = g.getFontMetrics();
                    String text = "💝 Donation Panel - Image Not Found";
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    g.drawString(text, (getWidth() - textWidth) / 2, (getHeight() - textHeight) / 2 + fm.getAscent());
                }
            }
        };

        frame.add(imageLabel);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null); // Center the window
    }

    public JPanel createDonationPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Add gradient background like other dashboard panels
                GradientPaint gp = new GradientPaint(0, 0, new Color(243, 244, 246), 0, getHeight(), new Color(229, 231, 235));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Load the original image from resources
        ImageIcon originalIcon = null;
        try {
            URL imageUrl = getClass().getResource("/image/DONATION.png");
            if (imageUrl != null) {
                originalIcon = new ImageIcon(ImageIO.read(imageUrl));
            }
        } catch (IOException e) {
            System.err.println("Error loading donation image resource: " + e.getMessage());
        }

        // Create a custom JLabel that scales the image to fit the panel
        final ImageIcon finalIcon = originalIcon;
        JLabel imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (finalIcon != null && finalIcon.getIconWidth() > 0) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imageWidth = finalIcon.getIconWidth();
                    int imageHeight = finalIcon.getIconHeight();

                    // Calculate scaling factor to fit image within panel while maintaining aspect ratio
                    double scaleX = (double) panelWidth / imageWidth;
                    double scaleY = (double) panelHeight / imageHeight;
                    // Scale to 90% for padding
                    double scale = Math.min(scaleX, scaleY) * 0.9;

                    int scaledWidth = (int) (imageWidth * scale);
                    int scaledHeight = (int) (imageHeight * scale);

                    // Center the image
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;

                    g2d.drawImage(finalIcon.getImage(), x, y, scaledWidth, scaledHeight, this);
                    g2d.dispose();
                } else {
                    // Fallback text if image not found
                    g.setFont(new Font("SansSerif", Font.BOLD, 32));
                    g.setColor(new Color(71, 85, 105));
                    FontMetrics fm = g.getFontMetrics();
                    String text = "💝 Donation Panel";
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    g.drawString(text, (getWidth() - textWidth) / 2, (getHeight() - textHeight) / 2 + fm.getAscent());
                }
            }
        };

        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        panel.add(imageLabel, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        Donation donation = new Donation();
        donation.displayImage();
    }
}