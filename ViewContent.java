import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class ViewContent extends JFrame {

    private JFrame parentFrame;
    private String petName;

    public ViewContent() {
        this(null, "Buddy");
    }
    
    
    public ViewContent(JFrame parent, String petName) {
        this.parentFrame = parent;
        this.petName = petName != null ? petName : "Buddy";
        
        
        setTitle(this.petName + " - Dog Adoption Profile");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setResizable(false);
        setSize(720, 520);
        setLocationRelativeTo(parent); 
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                    parentFrame.toFront();
                }
            }
        });
        
        getContentPane().setBackground(new Color(229, 231, 235));
        getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));


        JPanel mainCard = new JPanel();
        mainCard.setLayout(new BorderLayout(10, 24));
        mainCard.setBackground(Color.WHITE);
        mainCard.setBorder(new EmptyBorder(32, 32, 32, 32));
        mainCard.setPreferredSize(new Dimension(640, 440));
        
        JPanel topSection = new JPanel(new BorderLayout(32, 0));
        topSection.setOpaque(false);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        detailsPanel.add(createDetailLabel("Name:", this.petName));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(createDetailLabel("Gender:", "Male"));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(createDetailLabel("Age:", "2 years"));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(createDetailLabel("Breed:", "Golden Retriever"));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(createDetailLabel("Color:", "Golden"));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(createDetailLabel("Health:", "Vaccinated & Neutered"));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(createDetailLabel("Personality:", "Friendly, Playful"));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(createDetailLabel("Contact Number:", "123-456-7890"));

        // Replace red placeholder with actual image
        JLabel imageLabel = createPetImageLabel();
        
        topSection.add(detailsPanel, BorderLayout.CENTER);
        topSection.add(imageLabel, BorderLayout.EAST);
        
        JPanel bottomSection = new JPanel(new BorderLayout(0, 12));
        bottomSection.setOpaque(false);
        
        JLabel reasonTitle = new JLabel("Reason for Adoption:");
        reasonTitle.setFont(new Font("Inter", Font.BOLD, 20));
        reasonTitle.setForeground(new Color(31, 41, 55));

        JTextArea reasonText = new JTextArea(
            "Buddy is a wonderful and energetic dog looking for a forever home. " +
            "His previous owner had to move to a location where pets were not allowed. " +
            "He loves to play fetch and enjoys long walks in the park. He is great " +
            "with kids and other dogs, making him a perfect family companion."
        );
        reasonText.setFont(new Font("Inter", Font.PLAIN, 14));
        reasonText.setForeground(new Color(55, 65, 81));
        reasonText.setWrapStyleWord(true);
        reasonText.setLineWrap(true);
        reasonText.setEditable(false);
        reasonText.setOpaque(false); 
        reasonText.setHighlighter(null);

        bottomSection.add(reasonTitle, BorderLayout.NORTH);
        bottomSection.add(reasonText, BorderLayout.CENTER);

        mainCard.add(topSection, BorderLayout.NORTH);
        mainCard.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);
        mainCard.add(bottomSection, BorderLayout.SOUTH);
        
        add(mainCard);
    }

    private JLabel createDetailLabel(String key, String value) {
        String labelText = "<html><body style='font-family: Inter; font-size: 11px;'>" +
                           "<b style='color: rgb(31,41,55);'>" + key + "</b>" +
                           "<span style='color: rgb(55,65,81);'> " + value + "</span>" +
                           "</body></html>";
        return new JLabel(labelText);
    }

    private JLabel createPetImageLabel() {
        JLabel imageLabel = new JLabel();
        Dimension imageSize = new Dimension(160, 160);
        imageLabel.setPreferredSize(imageSize);
        imageLabel.setMaximumSize(imageSize);
        imageLabel.setMinimumSize(imageSize);
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 2));
        
        // Try to load pet image
        ImageIcon petImage = loadPetImage(this.petName);
        if (petImage != null) {
            imageLabel.setIcon(petImage);
        } else {
            // Fallback to a default placeholder
            imageLabel.setBackground(new Color(229, 231, 235));
            imageLabel.setOpaque(true);
            imageLabel.setText("No Image");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            imageLabel.setForeground(new Color(107, 114, 128));
        }
        
        return imageLabel;
    }
    
    private ImageIcon loadPetImage(String petName) {
        try {
            // Try multiple image formats and locations
            String[] extensions = {".jpg", ".jpeg", ".png", ".gif"};
            String[] directories = {
                "images/pets/",
                "src/images/pets/",
                "resources/images/pets/",
                ""
            };
            
            for (String dir : directories) {
                for (String ext : extensions) {
                    String imagePath = dir + petName.toLowerCase().replaceAll("\\s+", "_") + ext;
                    File imageFile = new File(imagePath);
                    
                    if (imageFile.exists()) {
                        BufferedImage originalImage = ImageIO.read(imageFile);
                        Image scaledImage = originalImage.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading image for " + petName + ": " + e.getMessage());
        }
        
        return null; // Return null if no image found
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewContent frame = new ViewContent();
            frame.setVisible(true);
        });
    }
}

