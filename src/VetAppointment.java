// src/VetAppointment.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;

public class VetAppointment extends JPanel {

    // --- THEME CONSTANTS ---
    private static final Color COL_PRIMARY = new Color(79, 70, 229);
    private static final Color COL_BACKGROUND = new Color(243, 244, 246);
    private static final Color COL_SURFACE = Color.WHITE;
    private static final Color COL_TEXT_MAIN = new Color(17, 24, 39);
    private static final Color COL_TEXT_SEC = new Color(107, 114, 128);
    private static final Color COL_BORDER = new Color(229, 231, 235);

    private static final Font FONT_H2 = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);

    // --- DATA ---
    record Doctor(String name, String specialty, String phone, String email, String schedule, String clinic, String rating, int yearsExp) {}

    // [FIX 1] Add currentUser field
    private String currentUser = "Guest";
    private int currentVetIndex = 0;

    private final Doctor[] doctors = {
            new Doctor("Dr. Olivia Sterling", "Chief of Surgery", "0917-555-0101", "olivia.s@paws.ph", "Mon-Fri: 8am-4pm", "Sterling Surgical Center", "5.0", 15),
            new Doctor("Dr. Marcus Chen", "Exotic Animal Specialist", "0917-555-0102", "marcus.c@paws.ph", "Tue-Sat: 10am-6pm", "Wild & Free Clinic", "4.9", 12),
            new Doctor("Dr. Sarah Jenkins", "Internal Medicine", "0917-555-0103", "sarah.j@paws.ph", "Mon-Thu: 9am-5pm", "City Vet Hospital", "4.8", 8),
            new Doctor("Dr. James Wilson", "Orthopedic Specialist", "0917-555-0104", "james.w@paws.ph", "Wed-Sun: 11am-8pm", "Bone & Joint Center", "4.9", 20),
            new Doctor("Dr. David Park", "Emergency Care", "0917-555-0106", "david.p@paws.ph", "Daily: 6pm-2am", "24/7 Pet Emergency", "5.0", 10)
    };

    // --- COMPONENTS ---
    private JPanel vetCardPanel;
    private JComboBox<String> vetSelector;
    private JTextField txtOwner, txtPetName, txtPetType, txtBreed, txtWeight, txtEmergency;
    private JComboBox<String> cmbGender;
    private JFormattedTextField txtDate, txtPhone, txtVaccine;
    private JSpinner timeSpinner;

    public VetAppointment() {
        setLayout(new BorderLayout());
        setBackground(COL_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setBackground(COL_BACKGROUND);
        contentGrid.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 0, 20);
        contentGrid.add(createFormSection(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentGrid.add(createSideBar(), gbc);

        add(contentGrid, BorderLayout.CENTER);
    }

    // [FIX 2] Add Setter method so Dashboard can pass the username
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(99, 102, 241), getWidth(), 0, new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("Veterinary Appointment System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel createFormSection() {
        RoundedPanel panel = new RoundedPanel(20, COL_SURFACE);
        panel.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Patient Information");
        lblTitle.setFont(FONT_H2);
        lblTitle.setForeground(COL_TEXT_MAIN);
        lblTitle.setBorder(new EmptyBorder(20, 25, 10, 25));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setBackground(COL_SURFACE);
        formContent.setBorder(new EmptyBorder(10, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.5;
        addInput(formContent, gbc, "Owner Name", txtOwner = createTextField());
        gbc.gridx = 1;
        addInput(formContent, gbc, "Pet Name", txtPetName = createTextField());

        gbc.gridy++; gbc.gridx = 0;
        addInput(formContent, gbc, "Pet Type", txtPetType = createTextField());
        gbc.gridx = 1;
        addInput(formContent, gbc, "Breed/Color", txtBreed = createTextField());

        gbc.gridy++; gbc.gridx = 0;
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        styleComboBox(cmbGender);
        addInput(formContent, gbc, "Gender", cmbGender);
        gbc.gridx = 1;
        addInput(formContent, gbc, "Weight (kg)", txtWeight = createTextField());

        gbc.gridy++; gbc.gridx = 0;
        txtPhone = createFormattedField("+63 ###-###-####");
        addInput(formContent, gbc, "Contact Number", txtPhone);
        gbc.gridx = 1;
        addInput(formContent, gbc, "Emergency Contact", txtEmergency = createTextField());

        gbc.gridy++; gbc.gridx = 0;
        vetSelector = new JComboBox<>();
        for(Doctor d : doctors) vetSelector.addItem(d.name);
        styleComboBox(vetSelector);
        vetSelector.addActionListener(e -> {
            currentVetIndex = vetSelector.getSelectedIndex();
            updateVetCard();
        });
        addInput(formContent, gbc, "Select Veterinarian", vetSelector);

        gbc.gridx = 1;
        txtDate = createFormattedField("##/##/####");
        addInput(formContent, gbc, "Date (MM/DD/YYYY)", txtDate);

        gbc.gridy++; gbc.gridx = 0;
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "hh:mm a"));
        timeSpinner.setBorder(BorderFactory.createLineBorder(COL_BORDER));
        addInput(formContent, gbc, "Preferred Time", timeSpinner);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(COL_SURFACE);

        JButton btnBook = new ModernButton("Confirm Booking", COL_PRIMARY, Color.WHITE);
        btnBook.addActionListener(e -> handleSubmit());

        btnPanel.add(btnBook);
        formContent.add(btnPanel, gbc);

        JScrollPane scroll = new JScrollPane(formContent);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSideBar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);

        vetCardPanel = new JPanel(new BorderLayout());
        vetCardPanel.setOpaque(false);
        updateVetCard();
        sidebar.add(vetCardPanel);

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void updateVetCard() {
        vetCardPanel.removeAll();
        Doctor doc = doctors[currentVetIndex];

        RoundedPanel card = new RoundedPanel(20, COL_SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel avatarPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_PRIMARY);
                g2.fillOval(getWidth()/2 - 35, 0, 70, 70);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                String initials = "DR";
                if(doc.name.length() > 4) initials = doc.name.substring(4, 5);
                g2.drawString(initials, (getWidth() - 20)/2, 45);
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(300, 80));
        avatarPanel.setMaximumSize(new Dimension(300, 80));

        JLabel lblName = new JLabel(doc.name);
        lblName.setFont(FONT_H2);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRating = new JLabel("★ " + doc.rating + " (" + doc.yearsExp + " yrs exp)");
        lblRating.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRating.setForeground(new Color(245, 158, 11));
        lblRating.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel infoBox = new JPanel(new GridLayout(3, 1, 0, 5));
        infoBox.setOpaque(false);
        infoBox.add(new JLabel("📞 " + doc.phone));
        infoBox.add(new JLabel("✉ " + doc.email));
        infoBox.add(new JLabel("🏥 " + doc.clinic));
        infoBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(avatarPanel);
        card.add(lblName);
        card.add(lblRating);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(infoBox);

        vetCardPanel.add(card);
        vetCardPanel.revalidate();
        vetCardPanel.repaint();
    }

    private void handleSubmit() {
        String pet = txtPetName.getText();
        String owner = txtOwner.getText();
        String date = txtDate.getText();
        String time = ((JSpinner.DateEditor) timeSpinner.getEditor()).getFormat().format(timeSpinner.getValue());
        String vet = (String) vetSelector.getSelectedItem();

        if(pet.isEmpty() || owner.isEmpty() || date.contains("_")) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        // [FIX 3] Call backend with 6 arguments (including currentUser)
        VetClinicSystem.AppointmentDetails details = new VetClinicSystem.AppointmentDetails(
                pet, owner, vet, date, time, this.currentUser
        );

        boolean success = VetClinicSystem.processNewAppointment(details);

        if(success) {
            JOptionPane.showMessageDialog(this, "Appointment Booked Successfully for " + this.currentUser + "!");
            txtPetName.setText(""); txtOwner.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Booking Failed. Database Error.");
        }
    }

    // UI Helpers
    private void addInput(JPanel p, GridBagConstraints gbc, String label, Component c) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 5));
        wrapper.setBackground(COL_SURFACE);
        JLabel l = new JLabel(label);
        l.setFont(FONT_LABEL);
        l.setForeground(COL_TEXT_SEC);
        wrapper.add(l, BorderLayout.NORTH);
        wrapper.add(c, BorderLayout.CENTER);
        p.add(wrapper, gbc);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    private JFormattedTextField createFormattedField(String format) {
        try {
            MaskFormatter mf = new MaskFormatter(format);
            mf.setPlaceholderCharacter('_');
            JFormattedTextField tf = new JFormattedTextField(mf);
            tf.setFont(FONT_BODY);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COL_BORDER),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            return tf;
        } catch (ParseException e) { return new JFormattedTextField(); }
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(FONT_BODY);
        box.setBackground(Color.WHITE);
        box.setUI(new BasicComboBoxUI());
    }

    static class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) { this.radius = radius; this.bgColor = bgColor; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    static class ModernButton extends JButton {
        public ModernButton(String text, Color bg, Color fg) {
            super(text); setBackground(bg); setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setFocusPainted(false); setBorderPainted(false);
            setPreferredSize(new Dimension(160, 40));
        }
    }
}