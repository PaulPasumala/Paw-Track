import javax.swing.*;
import java.awt.*;

public class Donation {
    
    public void displayImage() {
        JFrame frame = new JFrame("Donation Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize);
        
        // Load and scale the image to fit the screen
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/image/DONATION.png"));
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(screenSize.width - 20, screenSize.height - 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        
        frame.add(imageLabel);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null); // Center the window
    }
    
    public JPanel createDonationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Load the original image
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/image/DONATION.png"));
        
        // Create a custom JLabel that scales the image to fit the panel
        JLabel imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (originalIcon.getIconWidth() > 0) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imageWidth = originalIcon.getIconWidth();
                    int imageHeight = originalIcon.getIconHeight();
                    
                    // Calculate scaling factor to fit image within panel while maintaining aspect ratio
                    double scaleX = (double) panelWidth / imageWidth;
                    double scaleY = (double) panelHeight / imageHeight;
                    double scale = Math.min(scaleX, scaleY);
                    
                    int scaledWidth = (int) (imageWidth * scale);
                    int scaledHeight = (int) (imageHeight * scale);
                    
                    // Center the image
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;
                    
                    g2d.drawImage(originalIcon.getImage(), x, y, scaledWidth, scaledHeight, this);
                    g2d.dispose();
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
