import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.geom.RoundRectangle2D;

public class ViewContent extends JFrame {

    private String petName;
    private String breed;
    private String gender;
    private String age;
    private byte[] imageBytes;
    private String description;

    // Updated Constructor
    public ViewContent(JFrame parent, String petName, String breed, String gender, String age, byte[] imageBytes, String description) {
        super(petName + " - Details");
        this.petName = petName;
        this.breed = breed;
        this.gender = gender;
        this.age = age;
        this.imageBytes = imageBytes;
        this.description = description;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(800, 600);
        setLocationRelativeTo(parent);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Theme.MAIN_BG);
        setContentPane(contentPane);

        // Header
        contentPane.add(Theme.createHeader(petName, breed), BorderLayout.NORTH);

        // Main Card
        JPanel mainCard = new Theme.RoundedPanel(20, Color.WHITE);
        mainCard.setLayout(new BorderLayout(20, 20));
        mainCard.setBorder(new EmptyBorder(30, 30, 30, 30));

        // LEFT: Image
        JLabel imageLabel = createPetImageLabel();
        mainCard.add(imageLabel, BorderLayout.WEST);

        // CENTER: Details
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        details.add(createDetailRow("Name", petName));
        details.add(createDetailRow("Breed", breed));
        details.add(createDetailRow("Gender", gender));
        details.add(createDetailRow("Age", age + " years old"));
        details.add(createDetailRow("Status", "Available for Adoption"));

        details.add(Box.createVerticalStrut(20));

        // Use the passed description (or fallback text if null/empty)
        String displayText = (description != null && !description.trim().isEmpty())
                ? description
                : petName + " is a wonderful companion looking for a forever home.";

        JTextArea desc = new JTextArea(displayText);
        desc.setFont(Theme.BODY);
        desc.setForeground(Theme.TEXT_MUTED);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        details.add(desc);

        mainCard.add(details, BorderLayout.CENTER);

        // BOTTOM: Action Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        Theme.ModernButton adoptBtn = new Theme.ModernButton("Adopt " + petName, Theme.ACCENT);
        adoptBtn.setPreferredSize(new Dimension(180, 45));
        adoptBtn.addActionListener(e -> {
            // --- CONNECTIVITY: Open PetAdoptionForm inside Dashboard ---
            Window window = SwingUtilities.getWindowAncestor(this);
            // If the parent of this popup is the dashboard, we use it directly
            if (parent instanceof Dashboard dash) {
                dash.showAdoptionForm(petName);
                dispose(); // Close the details popup
            } else if (window instanceof Dashboard dash) {
                dash.showAdoptionForm(petName);
                dispose();
            } else {
                // Fallback for standalone testing or edge cases
                dispose();
                JOptionPane.showMessageDialog(this, "Please return to dashboard to adopt.");
            }
        });

        buttonPanel.add(adoptBtn);
        mainCard.add(buttonPanel, BorderLayout.SOUTH);

        // Add padding around card
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrapper.add(mainCard, BorderLayout.CENTER);

        contentPane.add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(1000, 30));

        JLabel l = new JLabel(label + ": ");
        l.setFont(Theme.BODY_BOLD);
        l.setForeground(Theme.TEXT_DARK);

        JLabel v = new JLabel(value);
        v.setFont(Theme.BODY);
        v.setForeground(Theme.TEXT_MUTED);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        return row;
    }

    private JLabel createPetImageLabel() {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(300, 300));

        if (imageBytes != null && imageBytes.length > 0) {
            try {
                InputStream in = new ByteArrayInputStream(imageBytes);
                Image img = ImageIO.read(in).getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            } catch (Exception e) {}
        } else {
            label.setText("No Image");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        return label;
    }
}