import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PawManagement extends JPanel {

    private final Color MAIN_BACKGROUND = Color.WHITE; // ✅ force white background

    public PawManagement() {
        setLayout(new BorderLayout());
        setBackground(MAIN_BACKGROUND);
        reloadPets(); // load pets immediately
    }

    // 🔄 Reload pets from the database
    public void reloadPets() {
        removeAll(); // clear old content

        // ✅ FlowLayout instead of GridLayout for consistent card sizing
        JPanel imageGridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        imageGridPanel.setBackground(Color.WHITE);

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/pawpatrol_db", "root", "")) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, status, image FROM pets");

            while (rs.next()) {
                String name = rs.getString("name");
                String status = rs.getString("status");
                byte[] imgBytes = rs.getBytes("image");

                String imagePath = null;
                if (imgBytes != null) {
                    File tempFile = File.createTempFile("pet_", ".png");
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(imgBytes);
                    }
                    imagePath = tempFile.getAbsolutePath();
                }

                imageGridPanel.add(createPetCard(imagePath, name, status));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(imageGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE); // ✅ white background

        add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createPetCard(String imagePath, String petName, String status) {
        JPanel cardPanel = new JPanel(new BorderLayout(0, 10));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // ✅ Fix card sizing
        cardPanel.setPreferredSize(new Dimension(280, 320));
        cardPanel.setMaximumSize(new Dimension(280, 320));
        cardPanel.setMinimumSize(new Dimension(280, 320));

        JLabel imageLabel;
        if (imagePath != null && new File(imagePath).exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaledImage = icon.getImage().getScaledInstance(250, 200, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(scaledImage));
        } else {
            imageLabel = new JLabel("🐾", SwingConstants.CENTER);
            imageLabel.setFont(new Font("SansSerif", Font.BOLD, 100));
            imageLabel.setForeground(new Color(230, 230, 230));
        }

        // ✅ Step 3: enforce consistent image area
        imageLabel.setPreferredSize(new Dimension(250, 200));
        imageLabel.setMinimumSize(new Dimension(250, 200));
        imageLabel.setMaximumSize(new Dimension(250, 200));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel nameLabel = new JLabel(petName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        switch (status.toLowerCase()) {
            case "available" -> statusLabel.setForeground(new Color(40, 167, 69));
            case "adopted" -> statusLabel.setForeground(new Color(220, 53, 69));
            case "in foster" -> statusLabel.setForeground(new Color(255, 193, 7));
            default -> statusLabel.setForeground(Color.DARK_GRAY);
        }

        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 10, 5));

        JButton viewButton = new JButton("View");
        viewButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        viewButton.addActionListener(e -> {
            ViewContent viewWindow = new ViewContent((JFrame) SwingUtilities.getWindowAncestor(this), petName);
            viewWindow.setVisible(true);
        });

        JButton adoptButton = new JButton("Adopt");
        adoptButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        adoptButton.setBackground(new Color(23, 162, 184));
        adoptButton.setForeground(Color.WHITE);
        adoptButton.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

            PetAdoptionForm adoptionForm = new PetAdoptionForm(parentFrame, petName);
            adoptionForm.setVisible(true);

            if (parentFrame != null) {
                parentFrame.setVisible(false);
            }
        });

        buttonPanel.add(viewButton);
        buttonPanel.add(adoptButton);

        cardPanel.add(imageLabel, BorderLayout.NORTH);
        cardPanel.add(detailsPanel, BorderLayout.CENTER);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        return cardPanel;
    }
}