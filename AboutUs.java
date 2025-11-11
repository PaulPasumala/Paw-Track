import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class AboutUs {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("About Us - Test Mode");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.getContentPane().add(createAboutUsPanel());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Creates and returns the AboutUs panel for integration into other components
    public static JPanel createAboutUsPanel() {
        // --- Main Container Panel ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        // Changed to white background
        mainPanel.setBackground(Color.WHITE);
        // Add padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));


        String julianDesc = "Julian Agustino is a passionate software developer with expertise in Java and UI design. He loves creating user-friendly applications and has a keen eye for detail. His dedication to clean code and excellent user experience makes him a valuable team member.";
        String rodmarkDesc = "Rodmark Bautista  is a full-stack developer with strong leadership skills. He has experience in project management and team coordination. His ability to bridge technical and business requirements makes him an excellent team lead.";
        String marlonDesc = "Marlon Lozano is a creative designer and front-end developer who brings visual concepts to life. She specializes in user interface design and has a talent for creating engaging user experiences that are both beautiful and functional.";
        String paulDesc = "Paul Pasumala is a skilled developer specializing in backend systems and database management. He has extensive experience in building robust and scalable applications. His problem-solving skills and technical expertise drive innovative solutions.";
        String dayritDesc = "Axzel Dayrit is a quality assurance specialist who ensures our applications meet the highest standards. Her attention to detail and systematic testing approach helps deliver bug-free software that users can rely on.";


        // Image paths relative to the resources folder (src/)
        String julianImagePath = "/PERSON/AGUSTINO ENHANCE.png";
        String rodmarkImagePath = "/PERSON/BAUSTITA ENHANCE.png";
        String marlonImagePath = "/PERSON/LOZANO ENHANCE.png";
        String paulImagePath = "/PERSON/PASUMALA ENHANCE.png";
        String dayritImagePath = "/PERSON/DAYRIT ENHANCE.png";


        PersonPanel julianPanel = new PersonPanel("JULIAN AGUSTINO", julianDesc, julianImagePath, false);
        PersonPanel rodmarkPanel = new PersonPanel("RODMARK BAUTISTA", rodmarkDesc, rodmarkImagePath, false);
        PersonPanel marlonPanel = new PersonPanel("MARLON LOZANO", marlonDesc, marlonImagePath, false);
        PersonPanel paulPanel = new PersonPanel("PAUL PASUMALA", paulDesc, paulImagePath, false);
        PersonPanel dayritPanel = new PersonPanel("AXZEL DAYRIT", dayritDesc, dayritImagePath, false);

        mainPanel.add(julianPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        mainPanel.add(rodmarkPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        mainPanel.add(marlonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        mainPanel.add(paulPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        mainPanel.add(dayritPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);


        JPanel wrapperPanel = new JPanel(new BorderLayout());
        // Changed to white background
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        return wrapperPanel;
    }
}

class PersonPanel extends JPanel {

    private static final int IMAGE_SIZE = 180;
    private static final Font NAME_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font DESC_FONT = new Font("SansSerif", Font.PLAIN, 14);
    // Changed to black for visibility on white
    private static final Color TEXT_COLOR = Color.BLACK;
    // Changed to white background
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    // Blue-green color
    private static final Color GLOW_COLOR = new Color(0, 255, 200);

    @SuppressWarnings("CallToPrintStackTrace")
    public PersonPanel(String name, String description, String imagePath, boolean reverse) {
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);

        // Create glowing blue-green border effect
        setBorder(BorderFactory.createCompoundBorder(
                new GlowBorder(GLOW_COLOR, 3, 8),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));
        imageLabel.setMinimumSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));

        try {
            // Load image from resource path
            Image image = ImageIO.read(getClass().getResource(imagePath));
            if (image != null) {
                // Create circular image with white background
                BufferedImage circularImage = createCircularImage(image, IMAGE_SIZE);
                imageLabel.setIcon(new ImageIcon(circularImage));
            } else {
                imageLabel.setText("Image not found at path");
            }
        } catch (IOException e) {
            e.printStackTrace();
            imageLabel.setText("Error loading image");
            imageLabel.setForeground(Color.RED);
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(BACKGROUND_COLOR);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(NAME_FONT);
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setFont(DESC_FONT);
        descriptionArea.setForeground(TEXT_COLOR);
        descriptionArea.setBackground(BACKGROUND_COLOR);
        descriptionArea.setOpaque(false);
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setColumns(30);
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(descriptionArea);

        if (reverse) {
            nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            nameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            descriptionArea.setAlignmentX(Component.RIGHT_ALIGNMENT);
            textPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 0, 0, 40);
            gbc.anchor = GridBagConstraints.NORTHEAST;
            add(textPanel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.anchor = GridBagConstraints.NORTH;
            add(imageLabel, gbc);
        } else {

            nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 0, 40);
            gbc.anchor = GridBagConstraints.NORTH;
            add(imageLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(0, 0, 0, 0);
            add(textPanel, gbc);
        }
    }

    // Creates a circular image with white background
    private BufferedImage createCircularImage(Image sourceImage, int size) {
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();

        // Enable anti-aliasing for smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill with white background
        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, size, size);

        // Create circular clip
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));

        // Draw the scaled image
        Image scaledImage = sourceImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        g2.drawImage(scaledImage, 0, 0, null);

        g2.dispose();
        return output;
    }
}

// Custom border class to create a glowing effect
class GlowBorder implements javax.swing.border.Border {
    private Color glowColor;
    private int thickness;
    private int glowSize;

    public GlowBorder(Color glowColor, int thickness, int glowSize) {
        this.glowColor = glowColor;
        this.thickness = thickness;
        this.glowSize = glowSize;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw multiple layers for glow effect
        for (int i = glowSize; i >= 0; i--) {
            float alpha = (float) (1.0 - (i / (float) glowSize)) * 0.3f;
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int) (alpha * 255)));
            g2.setStroke(new BasicStroke(thickness + i));
            g2.drawRoundRect(x + i/2, y + i/2, width - i - 1, height - i - 1, 15, 15);
        }

        // Draw main border
        g2.setColor(glowColor);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRoundRect(x + glowSize/2, y + glowSize/2, width - glowSize - 1, height - glowSize - 1, 15, 15);

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(glowSize + thickness, glowSize + thickness, glowSize + thickness, glowSize + thickness);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}