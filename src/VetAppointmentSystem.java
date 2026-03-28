import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.MaskFormatter;

public class VetAppointmentSystem extends JFrame {

    // --- THEME CONSTANTS ---
    private static final Color COL_PRIMARY = new Color(79, 70, 229);    // Indigo
    private static final Color COL_PRIMARY_HOVER = new Color(67, 56, 202);
    private static final Color COL_BACKGROUND = new Color(243, 244, 246); // Light Gray
    private static final Color COL_SURFACE = Color.WHITE;
    private static final Color COL_TEXT_MAIN = new Color(17, 24, 39);
    private static final Color COL_TEXT_SEC = new Color(107, 114, 128);
    private static final Color COL_ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color COL_BORDER = new Color(229, 231, 235);

    private static final Font FONT_H1 = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_H2 = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);

    // --- DATA STRUCTURES ---
    record Doctor(String name, String specialty, String phone, String email, String schedule, String clinic, String rating, int yearsExp) {}

    private int currentVetIndex = 0;
    private String currentUser = "Guest";
    private Dashboard parentDashboard;

    // [NEW] Edit Mode State
    private int editingApptId = -1; // -1 means creating new
    private ModernButton btnBook;   // Reference to change button text

    private final Doctor[] doctors = {
            new Doctor("Dr. Miguel Antonio Dela Cruz", "Nuclear Medicine Consultant – PET/CT", "0917-555-0101", "madelacruz@lakesidemed.ph", "Mon-Fri: 8am-4pm", "Fairview General Hospital – Quezon City", "", 15),
            new Doctor("Dr. Joanna Marie R. Santos", "Radiologist – PET Imaging", "0917-555-0102", "jmsantos@northgreenhosp.ph    ", "Tue-Sat: 10am-6pm", "North Greenfields General Hospital – Quezon City", "4.9", 12),
            new Doctor("Dr. Paulo C. Villanueva", "PET/CT Imaging Specialist", "0917-555-0103", "pvillanueva@westbayclinic.ph", "Mon-Thu: 9am-5pm", "North Avenue Medical Center – Quezon City", "", 8),
            new Doctor("Dr. Angela M. Soriano", "Cardiac PET/CT Specialist", "0917-555-0104", "james.w@paws.ph", "Wed-Sun: 11am-8pm", "Katipunan Specialty Medical Center – Quezon City", "", 20),
            new Doctor("Dr. Emily Rose", "PET Imaging Specialist – Whole-Body Screening", "0917-555-0105", "asoriano@stisidoremed.ph", "Fri-Mon: 8am-6pm", "Project 7 Medical & Diagnostic Center – Quezon City", "", 6),
            new Doctor("Dr. Adrian K. Valenzuela", "PET/CT Consultant – Oncology & Whole-Body Imaging", "0917-555-0106", "avalenzuela@eastviewmed.ph", "Daily: 6pm-2am", "Timog Heart & Lung Center – Quezon City", "", 10)
    };

    // --- COMPONENTS ---
    private JPanel vetCardPanel;
    private JComboBox<String> vetSelector;
    private JTextField txtOwner, txtPetName, txtPetType, txtBreed, txtWeight, txtEmergency;
    private JComboBox<String> cmbGender;
    private JFormattedTextField txtDate, txtPhone, txtVaccine;
    private JTextArea txtHistory, txtAllergies;
    private JSpinner timeSpinner;
    private JPanel builtMainPanel;

    public VetAppointmentSystem() {
        this(false);
    }

    public VetAppointmentSystem(boolean embedded) {
        if (!embedded) {
            setTitle("Paws & Claws | Modern Appointment System");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1200, 850);
            setLocationRelativeTo(null);
            setBackground(COL_BACKGROUND);
            builtMainPanel = buildMainPanel();
            add(builtMainPanel);
        } else {
            builtMainPanel = buildMainPanel();
        }
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public void setDashboard(Dashboard d) {
        this.parentDashboard = d;
    }

    public JPanel getMainPanel() {
        return builtMainPanel;
    }

    // [NEW] Load data for editing
    public void setEditData(int apptId, String pet, String owner, String vet, String date, String time) {
        this.editingApptId = apptId;

        txtPetName.setText(pet);
        txtOwner.setText(owner);
        txtDate.setText(date);
        vetSelector.setSelectedItem(vet);

        // Parse time string back to Date object for spinner
        try {
            // Try standard format first
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date t = sdf.parse(time);
            timeSpinner.setValue(t);
        } catch (Exception e) {
            // Fallback or ignore if format differs
        }

        if (btnBook != null) btnBook.setText("Update Appointment");
    }

    // [NEW] Reset form for new entry
    public void resetForm() {
        this.editingApptId = -1;
        clearForm();
        if (btnBook != null) btnBook.setText("Confirm Booking");
    }

    private JPanel buildMainPanel() {
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
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

        mainPanel.add(contentGrid, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_SURFACE);
        header.setPreferredSize(new Dimension(getWidth(), 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER));

        // [NEW] Back Button
        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        leftContainer.setOpaque(false);

        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setForeground(COL_TEXT_SEC);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            if (parentDashboard != null) parentDashboard.showVetList();
        });
        leftContainer.add(backBtn);

        JPanel internal = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        internal.setOpaque(false);

        JLabel logo = new JLabel("✚") {
            @Override public void setForeground(Color fg) { super.setForeground(COL_PRIMARY); }
        };
        logo.setFont(new Font("Segoe UI Symbol", Font.BOLD, 32));

        JPanel textContainer = new JPanel(new GridLayout(2, 1));
        textContainer.setOpaque(false);
        JLabel title = new JLabel("Veterinary Appointment System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(COL_TEXT_MAIN);
        JLabel subtitle = new JLabel("New Appointment Booking");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(COL_TEXT_SEC);

        textContainer.add(title);
        textContainer.add(subtitle);
        internal.add(logo);
        internal.add(textContainer);

        JPanel westWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        westWrapper.setOpaque(false);
        westWrapper.add(leftContainer);
        westWrapper.add(internal);

        header.add(westWrapper, BorderLayout.WEST);
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
        addInput(formContent, gbc, "Pet Type (e.g. Dog)", txtPetType = createTextField());
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

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(COL_BORDER);
        gbc.insets = new Insets(20, 10, 20, 10);
        formContent.add(sep, gbc);
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = 1;

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
        JSpinner.DateEditor de = new JSpinner.DateEditor(timeSpinner, "hh:mm a");
        timeSpinner.setEditor(de);
        styleSpinner(timeSpinner);
        addInput(formContent, gbc, "Preferred Time", timeSpinner);

        gbc.gridx = 1;
        txtVaccine = createFormattedField("##/##/####");
        addInput(formContent, gbc, "Last Vaccination", txtVaccine);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        txtHistory = createTextArea(3);
        addInput(formContent, gbc, "Medical History", new JScrollPane(txtHistory));

        gbc.gridy++;
        txtAllergies = createTextArea(2);
        addInput(formContent, gbc, "Known Allergies", new JScrollPane(txtAllergies));

        gbc.gridy++;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(COL_SURFACE);

        ModernButton btnClear = new ModernButton("Clear Form", COL_BACKGROUND, COL_TEXT_MAIN);
        btnClear.addActionListener(e -> clearForm());

        // [MODIFIED] Store reference to button
        btnBook = new ModernButton("Confirm Booking", COL_PRIMARY, Color.WHITE);
        btnBook.addActionListener(e -> handleSubmit());

        btnPanel.add(btnClear);
        btnPanel.add(btnBook);

        gbc.insets = new Insets(20, 10, 0, 10);
        formContent.add(btnPanel, gbc);

        JScrollPane scroll = new JScrollPane(formContent);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
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

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel navPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        navPanel.setOpaque(false);
        navPanel.setMaximumSize(new Dimension(300, 40));

        ModernButton btnPrev = new ModernButton(" Prev", COL_SURFACE, COL_TEXT_MAIN);
        btnPrev.setBorderColor(COL_BORDER);
        btnPrev.addActionListener(e -> {
            currentVetIndex = (currentVetIndex - 1 + doctors.length) % doctors.length;
            vetSelector.setSelectedIndex(currentVetIndex);
        });

        ModernButton btnNext = new ModernButton("Next ", COL_SURFACE, COL_TEXT_MAIN);
        btnNext.setBorderColor(COL_BORDER);
        btnNext.addActionListener(e -> {
            currentVetIndex = (currentVetIndex + 1) % doctors.length;
            vetSelector.setSelectedIndex(currentVetIndex);
        });

        navPanel.add(btnPrev);
        navPanel.add(btnNext);
        sidebar.add(navPanel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        RoundedPanel mapPanel = new RoundedPanel(20, new Color(219, 234, 254));
        mapPanel.setLayout(new GridBagLayout());
        mapPanel.setPreferredSize(new Dimension(0, 200));
        mapPanel.setMaximumSize(new Dimension(3000, 250));

        JLabel mapIcon = new JLabel("📍");
        mapIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        JLabel mapText = new JLabel("Clinic Location Map");
        mapText.setFont(FONT_LABEL);
        mapText.setForeground(COL_PRIMARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        mapPanel.add(mapIcon, gbc);
        gbc.gridy++;
        mapPanel.add(mapText, gbc);

        sidebar.add(mapPanel);
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
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_PRIMARY);
                g2.fillOval(getWidth()/2 - 35, 0, 70, 70);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                String[] parts = doc.name.split(" ");
                String initials = (parts.length >= 3) ? parts[1].substring(0,1) + parts[2].substring(0,1) : "DR";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = (70 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, x, y);
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(300, 80));
        avatarPanel.setMaximumSize(new Dimension(300, 80));
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblName = new JLabel(doc.name);
        lblName.setFont(FONT_H2);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(doc.specialty);
        lblTitle.setFont(FONT_BODY);
        lblTitle.setForeground(COL_ACCENT_GREEN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRating = new JLabel(" " + doc.rating + " (" + doc.yearsExp + " yrs exp)");
        lblRating.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRating.setForeground(new Color(245, 158, 11));
        lblRating.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 10));
        sep.setForeground(COL_BORDER);

        JPanel infoBox = new JPanel(new GridLayout(4, 1, 0, 8));
        infoBox.setOpaque(false);
        infoBox.add(createIconLabel("  " + doc.phone));
        infoBox.add(createIconLabel("  " + doc.email));
        infoBox.add(createIconLabel("  " + doc.schedule));
        infoBox.add(createIconLabel("  " + doc.clinic));
        infoBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(avatarPanel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(lblName);
        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(lblRating);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(sep);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(infoBox);

        vetCardPanel.add(card);
        vetCardPanel.revalidate();
        vetCardPanel.repaint();
    }

    // --- [UPDATED] SUBMIT LOGIC ---
    private void handleSubmit() {
        // 1. Capture Data
        String owner = txtOwner.getText().trim();
        String pet = txtPetName.getText().trim();
        String vet = (String) vetSelector.getSelectedItem();
        String date = txtDate.getText().trim();

        String time;
        try {
            JSpinner.DateEditor editor = (JSpinner.DateEditor) timeSpinner.getEditor();
            time = editor.getFormat().format(timeSpinner.getValue());
        } catch (Exception e) { time = "N/A"; }

        // 2. Validate
        if (owner.isEmpty() || pet.isEmpty() || date.contains("_")) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (Owner, Pet, Date).",
                    "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VetClinicSystem.AppointmentDetails details = new VetClinicSystem.AppointmentDetails(
                pet, owner, vet, date, time, this.currentUser
        );

        // 3. Logic for Create vs Update
        boolean success;
        String successMsg;

        if (editingApptId == -1) {
            // INSERT
            success = VetClinicSystem.processNewAppointment(details);
            successMsg = "Appointment for " + pet + " confirmed.";
        } else {
            // UPDATE
            success = VetClinicSystem.updateAppointment(editingApptId, details);
            successMsg = "Appointment for " + pet + " updated.";
        }

        // 4. Result
        if (success) {
            JOptionPane.showMessageDialog(this, successMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();

            // [MODIFIED] Return to List View on success
            if (parentDashboard != null) {
                parentDashboard.showVetList();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Operation Failed. Check Connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- HELPERS ---
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

    private JLabel createIconLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(COL_TEXT_SEC);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private JTextField createTextField() { return new ModernTextField(); }

    private JFormattedTextField createFormattedField(String format) {
        try {
            MaskFormatter mf = new MaskFormatter(format);
            mf.setPlaceholderCharacter('_');
            return new ModernFormattedTextField(mf);
        } catch (ParseException e) { return new ModernFormattedTextField(); }
    }

    private JTextArea createTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 0);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(8, 8, 8, 8));
        ta.setBackground(new Color(249, 250, 251));
        return ta;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(FONT_BODY);
        box.setBackground(Color.WHITE);
        box.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = super.createArrowButton();
                b.setBackground(Color.WHITE);
                b.setBorder(BorderFactory.createEmptyBorder());
                return b;
            }
        });
        ((JComponent) box.getRenderer()).setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT_BODY);
        spinner.setBorder(BorderFactory.createLineBorder(COL_BORDER));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setBorder(new EmptyBorder(8,8,8,8));
        }
    }

    private void clearForm() {
        txtOwner.setText(""); txtPetName.setText(""); txtBreed.setText(""); txtPetType.setText("");
        txtWeight.setText(""); txtEmergency.setText(""); txtDate.setValue(null); txtPhone.setValue(null);
        txtHistory.setText(""); txtAllergies.setText("");
    }

    // --- UI CLASSES ---
    class GradientPanel extends JPanel {
        private Color color1 = new Color(147, 197, 253);
        private Color color2 = new Color(251, 207, 232);
        public GradientPanel() { setOpaque(true); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, radius, radius);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, radius, radius);
            g2.setColor(COL_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-4, getHeight()-4, radius, radius);
            super.paintComponent(g);
        }
    }

    class ModernButton extends JButton {
        private Color normalColor, hoverColor, textColor, borderColor = null;
        public ModernButton(String text, Color bg, Color fg) {
            super(text);
            this.normalColor = bg;
            this.hoverColor = bg.equals(COL_SURFACE) ? new Color(243, 244, 246) : bg.darker();
            this.textColor = fg;
            setFont(FONT_LABEL);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(140, 40));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(hoverColor); repaint(); }
                public void mouseExited(MouseEvent e) { setBackground(normalColor); repaint(); }
            });
        }
        public void setBorderColor(Color c) { this.borderColor = c; }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? hoverColor : normalColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
        }
    }

    class ModernTextField extends JTextField {
        public ModernTextField() {
            setFont(FONT_BODY);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COL_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            setBackground(new Color(255, 255, 255));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(COL_PRIMARY, 1),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)
                    ));
                }
                public void focusLost(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(COL_BORDER, 1),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)
                    ));
                }
            });
        }
    }

    class ModernFormattedTextField extends JFormattedTextField {
        public ModernFormattedTextField() { super(); init(); }
        public ModernFormattedTextField(MaskFormatter mf) { super(mf); init(); }
        private void init() {
            setFont(FONT_BODY);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COL_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            setBackground(Color.WHITE);
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(COL_PRIMARY, 1),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)
                    ));
                }
                public void focusLost(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(COL_BORDER, 1),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)
                    ));
                }
            });
        }
    }

    class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = new Color(209, 213, 219);
            this.trackColor = COL_SURFACE;
        }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton jbutton = new JButton();
            jbutton.setPreferredSize(new Dimension(0, 0));
            return jbutton;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new VetAppointmentSystem().setVisible(true));
    }
}