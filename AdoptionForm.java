import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.*;
import java.io.ByteArrayOutputStream;


public class AdoptionForm {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Adoption Form - Java Swing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            FormCanvas formCanvas = new FormCanvas();
            frame.add(formCanvas);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(true); // Changed to true to allow maximizing
            
            // Maximize the window
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            
            frame.setVisible(true);
        });
    }
}

class FormCanvas extends JPanel {

    private final Map<String, String> formState = new HashMap<>();
    private final Map<String, Rectangle> uiElements = new LinkedHashMap<>();
    private BufferedImage petImage = null;
    private String activeElementId = null;
    private boolean cursorVisible = true;
    private JFrame parentFrame = null;

    private final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
    private final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 12);
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 22);
    private final Font HEADER_FONT = new Font("Arial", Font.BOLD, 28);
    private final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 16);


    public FormCanvas() {
        setBackground(Color.decode("#ffffff"));
        setPreferredSize(new Dimension(1200, 900)); // Set preferred size
        initializeFormState();
        initializeUIElements(); // Initialize UI elements immediately
        setupInputHandling();
        SwingUtilities.invokeLater(this::setupCursorBlinking);
        
        // Add component listener to handle resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initializeUIElements();
                repaint();
            }
        });
    }

    private void initializeFormState() {
        String[] keys = {
            "name", "gender", "age", "breed", "health",
            "personality", "contactNumber", "reasonForAdoption"
        };
        for (String key : keys) {
            formState.put(key, "");
        }
    }

    private void initializeUIElements() {
        // Get current panel dimensions with fallback values
        int panelWidth = getWidth() > 0 ? getWidth() : 1200;
        int panelHeight = getHeight() > 0 ? getHeight() : 900;
        
        // Clear existing elements to avoid duplicates
        uiElements.clear();
        
        // Calculate scaling factors
        double scaleX = panelWidth / 1200.0;
        double scaleY = panelHeight / 900.0;
        
        // Header buttons - scale position and size
        int backButtonX = (int) (930 * scaleX);
        int submitButtonX = (int) (1060 * scaleX);
        int buttonY = (int) (15 * scaleY);
        int buttonWidth = (int) (110 * scaleX);
        int buttonHeight = (int) (40 * scaleY);
        
        uiElements.put("backButton", new Rectangle(backButtonX, buttonY, buttonWidth, buttonHeight));
        uiElements.put("submitButton", new Rectangle(submitButtonX, buttonY, buttonWidth, buttonHeight));

        // Right panel elements
        int rightPanelX = (int) (865 * scaleX);
        int nameY = (int) (340 * scaleY);
        int uploadButtonY = (int) (400 * scaleY);
        int rightPanelWidth = (int) (280 * scaleX);
        int inputHeight = (int) (40 * scaleY);
        int uploadHeight = (int) (45 * scaleY);
        
        uiElements.put("name", new Rectangle(rightPanelX, nameY, rightPanelWidth, inputHeight));
        uiElements.put("uploadButton", new Rectangle(rightPanelX, uploadButtonY, rightPanelWidth, uploadHeight));

        // Left panel form fields - all using same input height as gender field
        int leftMargin = (int) (50 * scaleX);
        int middleX = (int) (300 * scaleX);
        int rightX = (int) (550 * scaleX);
        
        int row1Y = (int) (170 * scaleY);
        int row2Y = (int) (250 * scaleY);
        int row3Y = (int) (330 * scaleY);
        int smallWidth = (int) (230 * scaleX);
        int largeWidth = (int) (480 * scaleX);
        
        uiElements.put("gender", new Rectangle(leftMargin, row1Y, smallWidth, inputHeight));
        uiElements.put("age", new Rectangle(middleX, row1Y, smallWidth, inputHeight));
        uiElements.put("breed", new Rectangle(rightX, row1Y, smallWidth, inputHeight));
        uiElements.put("health", new Rectangle(leftMargin, row2Y, largeWidth, inputHeight));
        uiElements.put("personality", new Rectangle(leftMargin, row3Y, largeWidth, inputHeight));
        uiElements.put("contactNumber", new Rectangle(rightX, row2Y, smallWidth, inputHeight));
        
        // reasonForAdoption now uses same traits as gender field (fixed height, single-line)
        uiElements.put("reasonForAdoption", new Rectangle(rightX, row3Y, smallWidth, inputHeight));
    }

    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    private void setupInputHandling() {
        setFocusable(true);
        requestFocusInWindow();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                Point p = e.getPoint();
                boolean clickedOnInput = false;

                for (Map.Entry<String, Rectangle> entry : uiElements.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().contains(p)) {
                        if (isInputField(entry.getKey())) {
                            activeElementId = entry.getKey();
                            clickedOnInput = true;
                        } else if (entry.getKey().equals("uploadButton")) {
                            handleImageUpload();
                        } else if (entry.getKey().equals("submitButton")) {
                            handleSubmit();
                        } else if (entry.getKey().equals("backButton")) {
                            handleBackButton();
                        }
                    }
                }

                if (!clickedOnInput) {
                    activeElementId = null;
                }
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (activeElementId != null) {
                    char typedChar = e.getKeyChar();
                    String currentText = formState.get(activeElementId);
                    if (currentText == null) {
                        currentText = "";
                    }
                    
                    if (typedChar == KeyEvent.VK_BACK_SPACE) {
                        if (!currentText.isEmpty()) {
                            formState.put(activeElementId, currentText.substring(0, currentText.length() - 1));
                        }
                    } else if (typedChar == '\n' || typedChar == '\r') {
                        // Ignore Enter key for all fields (all are single-line now)
                        return;
                    } else if (typedChar == ' ') {
                        // Handle space character
                        String newText = currentText + " ";
                        if (doesTextFitInField(newText, activeElementId)) {
                            formState.put(activeElementId, newText);
                        }
                    } else if (Character.isDefined(typedChar) && typedChar > ' ') {
                        // Convert lowercase letters to uppercase
                        char charToAdd = Character.toUpperCase(typedChar);
                        String newText = currentText + charToAdd;
                        
                        // Check if text fits in single-line field
                        if (doesTextFitInField(newText, activeElementId)) {
                            formState.put(activeElementId, newText);
                        }
                    }
                    repaint();
                }
            }
        });
    }
    
    private boolean doesTextFitInField(String text, String fieldId) {
        Rectangle rect = uiElements.get(fieldId);
        if (rect == null) return true;
        
        FontMetrics fm = getFontMetrics(TEXT_FONT);
        int availableWidth = rect.width - 20; // Account for padding
        
        return fm.stringWidth(text) <= availableWidth;
    }


    @SuppressWarnings("CallToPrintStackTrace")
    private void handleImageUpload() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                petImage = ImageIO.read(selectedFile);
                repaint();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading image.", "Image Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isInputField(String id) {
        return !id.equals("uploadButton") && !id.equals("submitButton") && !id.equals("backButton");
    }

    private void setupCursorBlinking() {
        Timer timer = new Timer(500, _ -> {
            cursorVisible = !cursorVisible;
            if (activeElementId != null) {
                repaint();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Scale header
        int headerHeight = (int) (70 * (getHeight() / 900.0));
        g2d.setColor(Color.decode("#343a40"));
        g2d.fillRect(0, 0, getWidth(), headerHeight);
        g2d.setColor(Color.WHITE);
        g2d.setFont(HEADER_FONT);
        g2d.drawString("PET", (int) (30 * (getWidth() / 1200.0)), (int) (45 * (getHeight() / 900.0)));
        
        // Check if buttons exist before drawing
        Rectangle backButton = uiElements.get("backButton");
        Rectangle submitButton = uiElements.get("submitButton");
        if (backButton != null) {
            drawButton(g2d, backButton, "BACK", Color.decode("#dc3545"));
        }
        if (submitButton != null) {
            drawButton(g2d, submitButton, "SUBMIT", Color.decode("#28a745"));
        }
        
        // Scale sections
        double scaleX = getWidth() / 1200.0;
        double scaleY = getHeight() / 900.0;
        
        Rectangle leftSection = new Rectangle(
            (int) (30 * scaleX), 
            (int) (100 * scaleY), 
            (int) (780 * scaleX), 
            (int) (420 * scaleY)
        );
        
        Rectangle rightSection = new Rectangle(
            (int) (840 * scaleX), 
            (int) (100 * scaleY), 
            (int) (330 * scaleX), 
            (int) (440 * scaleY)
        );
        
        drawSection(g2d, leftSection, "PET INFORMATION");
        drawSection(g2d, rightSection, null);
        
        Rectangle uploadButton = uiElements.get("uploadButton");
        if (uploadButton != null) {
            drawButton(g2d, uploadButton, "UPLOAD IMAGE", Color.decode("#6c757d"));
        }
        
        // Scale image placeholder
        Rectangle placeholder = new Rectangle(
            (int) (865 * scaleX), 
            (int) (120 * scaleY), 
            (int) (280 * scaleX), 
            (int) (200 * scaleY)
        );
        
        if (petImage != null) {
            g2d.drawImage(petImage, placeholder.x, placeholder.y, placeholder.width, placeholder.height, null);
        } else {
            g2d.setFont(new Font("SansSerif", Font.PLAIN, (int) (80 * Math.min(scaleX, scaleY))));
            drawCenteredString(g2d, "🐕", placeholder, Color.decode("#adb5bd"));
        }
        g2d.setColor(Color.decode("#adb5bd"));
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0);
        g2d.setStroke(dashed);
        g2d.draw(new RoundRectangle2D.Double(placeholder.x, placeholder.y, placeholder.width, placeholder.height, 15, 15));
        g2d.setStroke(new BasicStroke(1));

        // Draw inputs with null checks
        Rectangle nameRect = uiElements.get("name");
        Rectangle genderRect = uiElements.get("gender");
        Rectangle ageRect = uiElements.get("age");
        Rectangle breedRect = uiElements.get("breed");
        Rectangle healthRect = uiElements.get("health");
        Rectangle personalityRect = uiElements.get("personality");
        Rectangle contactRect = uiElements.get("contactNumber");
        Rectangle reasonRect = uiElements.get("reasonForAdoption");
        
        if (nameRect != null) drawInput(g2d, nameRect, "PET NAME");
        if (genderRect != null) drawInput(g2d, genderRect, "GENDER");
        if (ageRect != null) drawInput(g2d, ageRect, "AGE");
        if (breedRect != null) drawInput(g2d, breedRect, "BREED");
        if (healthRect != null) drawInput(g2d, healthRect, "HEALTH STATUS");
        if (personalityRect != null) drawInput(g2d, personalityRect, "PERSONALITY");
        if (contactRect != null) drawInput(g2d, contactRect, "CONTACT NUMBER");
        if (reasonRect != null) drawInput(g2d, reasonRect, "REASON FOR ADOPTION");
    }

    private void drawSection(Graphics2D g2d, Rectangle bounds, String title) {
        g2d.setColor(Color.decode("#f8f9fa"));
        g2d.fill(new RoundRectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15));
        g2d.setColor(Color.decode("#e9ecef"));
        g2d.draw(new RoundRectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15));

        if (title != null) {
            g2d.setColor(Color.decode("#343a40"));
            g2d.setFont(TITLE_FONT);
            g2d.drawString(title, bounds.x + 20, bounds.y + 35);
            g2d.setColor(Color.decode("#e9ecef"));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(bounds.x + 20, bounds.y + 45, bounds.x + bounds.width - 20, bounds.y + 45);
            g2d.setStroke(new BasicStroke(1));
        }
    }

    private void drawButton(Graphics2D g2d, Rectangle rect, String text, Color color) {
        if (rect != null && text != null && color != null) {
            g2d.setColor(color);
            g2d.fill(new RoundRectangle2D.Double(rect.x, rect.y, rect.width, rect.height, 10, 10));
            g2d.setFont(BUTTON_FONT);
            drawCenteredString(g2d, text, rect, Color.WHITE);
        }
    }
    
    private void drawInput(Graphics2D g2d, Rectangle rect, String label) {
        if (rect == null || label == null) {
            return;
        }
        
        g2d.setFont(LABEL_FONT);
        g2d.setColor(Color.decode("#6c757d"));
        g2d.drawString(label, rect.x, rect.y - 8);
        boolean isActive = rect.equals(uiElements.get(activeElementId));
        g2d.setColor(Color.WHITE);
        g2d.fill(new RoundRectangle2D.Double(rect.x, rect.y, rect.width, rect.height, 10, 10));
        g2d.setColor(isActive ? Color.decode("#007bff") : Color.decode("#ced4da"));
        g2d.setStroke(new BasicStroke(isActive ? 2 : 1));
        g2d.draw(new RoundRectangle2D.Double(rect.x, rect.y, rect.width, rect.height, 10, 10));
        g2d.setStroke(new BasicStroke(1));
        
        String fieldKey = getKeyFromRect(rect);
        String text = fieldKey != null ? formState.get(fieldKey) : "";
        if (text == null) {
            text = "";
        }
        
        g2d.setFont(TEXT_FONT);
        g2d.setColor(Color.decode("#495057"));
        
        Shape oldClip = g2d.getClip();
        g2d.clip(new Rectangle(rect.x + 10, rect.y + 5, rect.width - 20, rect.height - 10));
        
        FontMetrics fm = g2d.getFontMetrics();
        
        // All fields now use single-line text handling
        drawSingleLineText(g2d, rect, text, fm, isActive);
        
        g2d.setClip(oldClip);
    }
    
    private void drawSingleLineText(Graphics2D g2d, Rectangle rect, String text, FontMetrics fm, boolean isActive) {
        int textY = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        
        if (!text.isEmpty()) {
            String displayText = text;
            int availableWidth = rect.width - 20;
            
            // Truncate text if it's too long for single line
            if (fm.stringWidth(displayText) > availableWidth) {
                while (fm.stringWidth(displayText + "...") > availableWidth && displayText.length() > 1) {
                    displayText = displayText.substring(0, displayText.length() - 1);
                }
                displayText += "...";
            }
            
            g2d.drawString(displayText, rect.x + 10, textY);
            
            if (isActive && cursorVisible) {
                // For single line, show cursor at end of actual text (not display text)
                int textWidth = fm.stringWidth(text);
                int maxAvailableWidth = rect.width - 20;
                int cursorX = rect.x + 10 + Math.min(textWidth, maxAvailableWidth);
                g2d.setColor(Color.BLACK);
                int cursorY1 = rect.y + 8;
                int cursorY2 = rect.y + rect.height - 8;
                g2d.drawLine(cursorX, cursorY1, cursorX, cursorY2);
            }
        } else {
            // Empty single line field
            if (isActive && cursorVisible) {
                int cursorX = rect.x + 10;
                g2d.setColor(Color.BLACK);
                int cursorY1 = rect.y + 8;
                int cursorY2 = rect.y + rect.height - 8;
                g2d.drawLine(cursorX, cursorY1, cursorX, cursorY2);
            }
        }
    }
    // Helper method to wrap text for display purposes


    private void handleBackButton() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to go back? Any unsaved changes will be lost.",
            "Confirm Back",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (option == JOptionPane.YES_OPTION) {
            Window currentWindow = SwingUtilities.getWindowAncestor(FormCanvas.this);
            if (currentWindow != null) {
                currentWindow.dispose();
            }
            
            // Show the parent dashboard if it exists
            if (parentFrame != null) {
                SwingUtilities.invokeLater(() -> {
                    parentFrame.setVisible(true);
                    parentFrame.toFront();
                    parentFrame.requestFocus();
                    
                    // Re-enable the adoption button
                    JButton adoptionButton = findAdoptionButtonInFrame(parentFrame);
                    if (adoptionButton != null) {
                        adoptionButton.setEnabled(true);
                    }
                });
            }
        }
    }

    private void handleSubmit() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to DB
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pawpatrol_db", "root", "");

            // SQL insert
            String sql = "INSERT INTO pets (name, gender, age, breed, health, personality, contact_number, reason_for_adoption, image) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, formState.get("name"));
            stmt.setString(2, formState.get("gender"));
            stmt.setString(3, formState.get("age"));
            stmt.setString(4, formState.get("breed"));
            stmt.setString(5, formState.get("health"));
            stmt.setString(6, formState.get("personality"));
            stmt.setString(7, formState.get("contactNumber"));
            stmt.setString(8, formState.get("reasonForAdoption"));

            // Handle image upload (if any)
            if (petImage != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(petImage, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                stmt.setBytes(9, imageBytes);
            } else {
                stmt.setNull(9, java.sql.Types.BLOB);
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Pet adoption form saved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            stmt.close();
            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving form: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Helper method to find the adoption button in the dashboard frame
     */
    private JButton findAdoptionButtonInFrame(JFrame frame) {
        return findButtonInContainer(frame.getContentPane(), "Adoption");
    }

    private JButton findButtonInContainer(Container container, String buttonText) {
        for (Component comp : container.getComponents()) {
            switch (comp) {
                case JButton button -> {
                    if (button.getText().contains(buttonText)) {
                        return button;
                    }
                }
                case Container container1 -> {
                    JButton found = findButtonInContainer(container1, buttonText);
                    if (found != null) {
                        return found;
                    }
                }
                default -> {
                }
            }
        }
        return null;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 900);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(800, 600);
    }

    private void drawCenteredString(Graphics2D g2d, String text, Rectangle rect, Color color) {
        if (text != null && rect != null && color != null) {
            FontMetrics metrics = g2d.getFontMetrics();
            int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
            int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
            g2d.setColor(color);
            g2d.drawString(text, x, y);
        }
    }

    private String getKeyFromRect(Rectangle rect) {
        if (rect == null) {
            return null;
        }
        for (Map.Entry<String, Rectangle> entry : uiElements.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(rect)) {
                return entry.getKey();
            }
        }
        return null;
    }
}

