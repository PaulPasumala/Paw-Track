import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class AppointmentView extends JPanel {

    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JPanel statsPanel;
    private String currentUser;
    private Dashboard parentDashboard;

    // --- MODERN GRADIENT COLORS ---
    private final Color GRADIENT_START = new Color(236, 72, 153); // Pink
    private final Color GRADIENT_END = new Color(59, 130, 246);   // Blue
    private final Color GRADIENT_MID = new Color(147, 101, 199);  // Purple blend
    private final Color CARD_BG = new Color(255, 255, 255, 240);  // Semi-transparent white
    private final Color TEXT_MAIN = new Color(31, 41, 55);
    private final Color TEXT_MUTED = new Color(107, 114, 128);
    private final Color ACCENT_PINK = new Color(236, 72, 153);
    private final Color ACCENT_BLUE = new Color(59, 130, 246);
    private final Color ACCENT_PURPLE = new Color(147, 51, 234);

    public AppointmentView(String username) {
        this.currentUser = username;
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Create gradient background panel
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    getWidth(), getHeight(), GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        gradientPanel.setLayout(new BorderLayout());
        
        gradientPanel.add(createHeader(), BorderLayout.NORTH);

        // Wrapper for main content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        contentPanel.add(createStatsRow());
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(createTableSection());

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        gradientPanel.add(scroll, BorderLayout.CENTER);
        
        add(gradientPanel, BorderLayout.CENTER);

        loadAppointments();
        updateStats();
        applyAdvancedFeatures();
    }

    public void setDashboard(Dashboard d) { this.parentDashboard = d; }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
                // Subtle bottom border with gradient
                GradientPaint borderGradient = new GradientPaint(
                    0, getHeight() - 3, ACCENT_PINK,
                    getWidth(), getHeight() - 3, ACCENT_BLUE
                );
                g2d.setPaint(borderGradient);
                g2d.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2d.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 90));
        header.setBorder(new EmptyBorder(0, 35, 0, 35));

        JLabel title = new JLabel("\u2728 Appointment Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_MAIN);
        // Apply emoji font support to title
        applyEmojiFontSupport(title, "\u2728", 28, Font.BOLD);
        header.add(title, BorderLayout.WEST);

        JButton newApptBtn = createModernButton("+ New Appointment", ACCENT_BLUE);
        newApptBtn.setPreferredSize(new Dimension(180, 45));

        newApptBtn.addActionListener(e -> {
            if (parentDashboard != null) parentDashboard.createVetAppointment();
        });

        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 22));
        actionBox.setOpaque(false);
        actionBox.add(newApptBtn);
        header.add(actionBox, BorderLayout.EAST);

        return header;
    }

    private JButton createModernButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(baseColor.brighter());
                } else {
                    g2d.setColor(baseColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createStatsRow() {
        statsPanel = new JPanel(new GridLayout(1, 4, 25, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(2000, 150));
        statsPanel.setPreferredSize(new Dimension(0, 150));

        statsPanel.add(createStatCard("Today's Visits", "0", new Color(139, 92, 246), "\uD83D\uDCC5"));
        statsPanel.add(createStatCard("Pending", "0", new Color(234, 179, 8), "\u231B"));
        statsPanel.add(createStatCard("Completed", "0", new Color(16, 185, 129), "\u2714"));
        statsPanel.add(createStatCard("Total Bookings", "0", ACCENT_PINK, "\uD83D\uDCCA"));

        return statsPanel;
    }

    private JPanel createStatCard(String label, String value, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow effect
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Glass morphism effect
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Gradient border
                GradientPaint borderGradient = new GradientPaint(
                    0, 0, accent.brighter(),
                    getWidth(), getHeight(), accent
                );
                g2.setPaint(borderGradient);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setOpaque(false);
        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(TEXT_MAIN);
        lblValue.setName("VAL_" + label);

        textPanel.add(lblTitle);
        textPanel.add(lblValue);

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient circle background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 100),
                    getWidth(), getHeight(), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50)
                );
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setLayout(new GridBagLayout());
        JLabel ico = new JLabel(icon);
        // Apply emoji font support
        applyEmojiFontSupport(ico, icon, 24, Font.PLAIN);
        iconPanel.add(ico);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(iconPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel createTableSection() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Glass card background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Subtle gradient border
                GradientPaint border = new GradientPaint(
                    0, 0, ACCENT_PINK,
                    getWidth(), 0, ACCENT_BLUE
                );
                g2.setPaint(border);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(2, 2, 2, 2));

        JLabel lblTitle = new JLabel("\uD83D\uDCC5 Upcoming Appointments");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_MAIN);
        // Apply emoji font support
        applyEmojiFontSupport(lblTitle, "\uD83D\uDCC5", 18, Font.BOLD);

        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(209, 213, 219), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText());
            }
        });

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.add(lblTitle, BorderLayout.WEST);
        headerWrapper.add(searchField, BorderLayout.EAST);
        headerWrapper.setBorder(new EmptyBorder(20, 25, 20, 25));

        String[] cols = {"Pet Info", "Owner", "Doctor", "Date & Time", "Status", "Actions", "ID", "ImageBytes"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col == 5; }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setRowHeight(70);
        appointmentTable.setShowVerticalLines(false);
        appointmentTable.setIntercellSpacing(new Dimension(0, 8));
        appointmentTable.getTableHeader().setReorderingAllowed(false);
        appointmentTable.getTableHeader().setResizingAllowed(false);

        appointmentTable.removeColumn(appointmentTable.getColumnModel().getColumn(7));
        appointmentTable.removeColumn(appointmentTable.getColumnModel().getColumn(6));

        appointmentTable.setGridColor(new Color(243, 244, 246));
        appointmentTable.setSelectionBackground(new Color(239, 246, 255));
        appointmentTable.setSelectionForeground(TEXT_MAIN);
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        appointmentTable.setBackground(new Color(255, 255, 255, 0));
        appointmentTable.getTableHeader().setBackground(new Color(248, 250, 252));
        appointmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        appointmentTable.getTableHeader().setForeground(TEXT_MUTED);
        appointmentTable.getTableHeader().setPreferredSize(new Dimension(0, 45));

        appointmentTable.getColumnModel().getColumn(0).setCellRenderer(new PetInfoRenderer());
        appointmentTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        appointmentTable.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        appointmentTable.getColumnModel().getColumn(5).setCellEditor(new ActionEditor(new JCheckBox()));

        JScrollPane tableScroll = new JScrollPane(appointmentTable);
        tableScroll.setBorder(null);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.getViewport().setBackground(new Color(255, 255, 255, 0));

        card.add(headerWrapper, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);

        return card;
    }

    public void loadAppointments() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                tableModel.setRowCount(0);
                String sql = "SELECT v.appt_id, v.pet_name, v.owner_name, v.vet_name, v.appt_date, v.appt_time, v.status, p.image " +
                        "FROM vet_appointments v " +
                        "LEFT JOIN pets_accounts p ON v.pet_name = p.name AND p.owner_username = v.username " +
                        "WHERE v.username = ? ORDER BY v.appt_date DESC";

                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, currentUser);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getString("pet_name"),
                                rs.getString("owner_name"),
                                rs.getString("vet_name"),
                                rs.getString("appt_date") + " " + rs.getString("appt_time"),
                                rs.getString("status"),
                                "Actions",
                                rs.getInt("appt_id"),
                                rs.getBytes("image")
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }
            @Override
            protected void done() {
                // Refresh stats AFTER table loads to ensure sync
                updateStats();
            }
        };
        worker.execute();
    }

    // --- [FIXED] STATS LOGIC ---
    private void updateStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int today = 0, pending = 0, completed = 0, total = 0;

                // Get today's date in MM/dd/yyyy format
                String todayStr = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

                String sql = "SELECT status, appt_date FROM vet_appointments WHERE username = ?";

                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, currentUser);
                    ResultSet rs = stmt.executeQuery();
                    while(rs.next()) {
                        String status = rs.getString("status");
                        String date = rs.getString("appt_date");

                        total++; // Count every appointment

                        // 1. Today's Visits Check
                        if (date != null && date.equals(todayStr)) {
                            // Only count active appointments for today
                            if (!"Cancelled".equalsIgnoreCase(status)) {
                                today++;
                            }
                        }

                        // 2. Pending Check
                        if (status != null && (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Booked"))) {
                            pending++;
                        }

                        // 3. Completed Check
                        if (status != null && status.equalsIgnoreCase("Completed")) {
                            completed++;
                        }
                    }
                } catch(Exception e) { e.printStackTrace(); }
                return new int[]{today, pending, completed, total};
            }

            @Override
            protected void done() {
                try {
                    int[] data = get();
                    updateStatLabel("VAL_Today's Visits", String.valueOf(data[0]));
                    updateStatLabel("VAL_Pending", String.valueOf(data[1]));
                    updateStatLabel("VAL_Completed", String.valueOf(data[2]));
                    updateStatLabel("VAL_Total Bookings", String.valueOf(data[3]));
                } catch(Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void updateStatLabel(String name, String val) {
        for (Component c : statsPanel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel card = (JPanel) c;
                // Find components inside the card
                for (Component inner : card.getComponents()) {
                    if (inner instanceof JPanel) {
                        JPanel textPanel = (JPanel) inner;
                        for (Component l : textPanel.getComponents()) {
                            if (name.equals(l.getName()) && l instanceof JLabel) {
                                ((JLabel) l).setText(val);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void filterTable(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        appointmentTable.setRowSorter(sorter);
        if (query.trim().length() == 0) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }

    class PetInfoRenderer extends JPanel implements TableCellRenderer {
        private JLabel avatar = new JLabel("", SwingConstants.CENTER);
        private JLabel nameLabel = new JLabel();
        private JLabel breedLabel = new JLabel("View Details");

        public PetInfoRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            setBackground(new Color(255, 255, 255, 0));
            avatar.setPreferredSize(new Dimension(45, 45));
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            breedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            breedLabel.setForeground(TEXT_MUTED);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 10, 0, 12);
            gbc.gridheight = 2;
            add(avatar, gbc);
            gbc.gridx = 1; gbc.gridheight = 1; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(0, 0, 0, 0);
            add(nameLabel, gbc);
            gbc.gridy = 1;
            add(breedLabel, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String petName = (String) value;
            nameLabel.setText(petName);

            int modelRow = table.convertRowIndexToModel(row);
            byte[] imgBytes = (byte[]) table.getModel().getValueAt(modelRow, 7);

            if (imgBytes != null && imgBytes.length > 0) {
                try {
                    InputStream in = new ByteArrayInputStream(imgBytes);
                    Image img = ImageIO.read(in).getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    BufferedImage masked = new BufferedImage(45, 45, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = masked.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new Ellipse2D.Float(0, 0, 45, 45));
                    g2.drawImage(img, 0, 0, 45, 45, null);
                    g2.dispose();
                    avatar.setIcon(new ImageIcon(masked));
                    avatar.setText("");
                    avatar.setOpaque(false);
                } catch(Exception e) { setInitials(petName); }
            } else {
                setInitials(petName);
            }

            if (isSelected) setBackground(new Color(239, 246, 255, 180));
            else setBackground(new Color(255, 255, 255, 0));
            return this;
        }

        private void setInitials(String name) {
            avatar.setIcon(null);
            avatar.setText(name.isEmpty() ? "?" : name.substring(0, 1));
            int hash = (name != null) ? name.hashCode() : 0;
            Color[] avColors = {ACCENT_PINK, new Color(245, 158, 11), new Color(16, 185, 129), ACCENT_BLUE, ACCENT_PURPLE};
            final Color avBg = avColors[Math.abs(hash) % avColors.length];
            avatar.setBackground(avBg);
            avatar.setOpaque(true);
            avatar.setForeground(Color.WHITE);
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        }
    }

    class StatusRenderer extends JPanel implements TableCellRenderer {
        private JLabel lbl = new JLabel();
        public StatusRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
            setOpaque(true);
            add(lbl);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (String) value;
            lbl.setText(" " + status + " ");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setOpaque(true);

            if ("Confirmed".equalsIgnoreCase(status) || "Booked".equalsIgnoreCase(status)) {
                lbl.setBackground(new Color(219, 234, 254));
                lbl.setForeground(new Color(29, 78, 216));
            } else if ("Pending".equalsIgnoreCase(status)) {
                lbl.setBackground(new Color(254, 249, 195));
                lbl.setForeground(new Color(161, 98, 7));
            } else if ("Completed".equalsIgnoreCase(status)) {
                lbl.setBackground(new Color(209, 250, 229));
                lbl.setForeground(new Color(5, 150, 105));
            } else {
                lbl.setBackground(new Color(254, 226, 226));
                lbl.setForeground(new Color(185, 28, 28));
            }
            lbl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(lbl.getForeground(), 1, true),
                new EmptyBorder(4, 10, 4, 10)
            ));
            if (isSelected) setBackground(new Color(239, 246, 255, 180));
            else setBackground(new Color(255, 255, 255, 0));
            return this;
        }
    }

    class ActionEditor extends DefaultCellEditor {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton editBtn = new JButton("\u270E");
        JButton delBtn = new JButton("\u2715");

        public ActionEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);
            styleMiniBtn(editBtn, new Color(59, 130, 246));
            styleMiniBtn(delBtn, new Color(239, 68, 68));

            editBtn.addActionListener(e -> {
                int row = appointmentTable.getSelectedRow();
                if (row != -1 && parentDashboard != null) {
                    int modelRow = appointmentTable.convertRowIndexToModel(row);
                    TableModel model = appointmentTable.getModel();
                    String pet = (String) model.getValueAt(modelRow, 0);
                    String owner = (String) model.getValueAt(modelRow, 1);
                    String vet = (String) model.getValueAt(modelRow, 2);
                    String dateTime = (String) model.getValueAt(modelRow, 3);
                    int id = (int) model.getValueAt(modelRow, 6);
                    String date = dateTime.split(" ")[0];
                    String time = dateTime.substring(dateTime.indexOf(" ") + 1);
                    fireEditingStopped();
                    parentDashboard.editVetAppointment(id, pet, owner, vet, date, time);
                }
            });
            delBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(panel, "Cancel appointment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    fireEditingStopped();
                    JOptionPane.showMessageDialog(panel, "Cancelled.");
                }
            });
            panel.add(editBtn);
            panel.add(delBtn);
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            return panel;
        }
    }

    class ActionRenderer extends JPanel implements TableCellRenderer {
        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            setOpaque(true);
            JButton edit = new JButton("\u270E");
            JButton del = new JButton("\u2715");
            styleMiniBtn(edit, new Color(59, 130, 246));
            styleMiniBtn(del, new Color(239, 68, 68));
            add(edit); add(del);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            if(s) setBackground(t.getSelectionBackground()); else setBackground(Color.WHITE);
            return this;
        }
    }

    private void styleMiniBtn(JButton b, Color c) {
        b.setPreferredSize(new Dimension(35, 35));
        b.setForeground(Color.WHITE);
        b.setBackground(c);
        b.setBorder(null);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(c.brighter());
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(c);
            }
        });
    }

    // [ADDED] Method to apply the new logic for Buttons and Stats
    public void applyAdvancedFeatures() {
        // 1. Swap the "Actions" column to use the new Logic
        appointmentTable.getColumnModel().getColumn(5).setCellEditor(new AdvancedActionEditor(new JCheckBox()));

        // 2. Fix Stats: Auto-move old 'CONFIRMED' appts to 'COMPLETED'
        // This makes the "Completed" container work automatically based on date
        new Thread(() -> {
            try (Connection conn = DBConnector.getConnection()) {
                // If date is passed and status was confirmed, mark as Completed
                String sql = "UPDATE vet_appointments SET status = 'Completed' WHERE status = 'CONFIRMED' AND STR_TO_DATE(appt_date, '%m/%d/%Y') < CURDATE()";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.executeUpdate();

                // Refresh UI stats immediately
                SwingUtilities.invokeLater(this::updateStats);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // [ADDED] New Editor to handle Cancel -> 30s Wait -> Delete
    class AdvancedActionEditor extends DefaultCellEditor {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton editBtn = new JButton("\u270E");
        JButton delBtn = new JButton("\u2715");

        public AdvancedActionEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);

            // Styling
            styleMiniBtn(editBtn, new Color(59, 130, 246));
            styleMiniBtn(delBtn, new Color(239, 68, 68));

            // EDIT LOGIC
            editBtn.addActionListener(e -> {
                int row = appointmentTable.getSelectedRow();
                if (row != -1 && parentDashboard != null) {
                    int modelRow = appointmentTable.convertRowIndexToModel(row);
                    TableModel model = appointmentTable.getModel();
                    String pet = (String) model.getValueAt(modelRow, 0);
                    String owner = (String) model.getValueAt(modelRow, 1);
                    String vet = (String) model.getValueAt(modelRow, 2);
                    String dateTime = (String) model.getValueAt(modelRow, 3);
                    int id = (int) model.getValueAt(modelRow, 6);
                    String date = dateTime.split(" ")[0];
                    String time = dateTime.substring(dateTime.indexOf(" ") + 1);
                    fireEditingStopped();
                    parentDashboard.editVetAppointment(id, pet, owner, vet, date, time);
                }
            });

            // DELETE LOGIC (The specific fix you asked for)
            delBtn.addActionListener(e -> {
                int row = appointmentTable.getSelectedRow();
                if (row == -1) return;

                int modelRow = appointmentTable.convertRowIndexToModel(row);
                int apptId = (int) tableModel.getValueAt(modelRow, 6);

                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Cancel this appointment?\nStatus will change to CANCELLED, then delete in 30s.",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    fireEditingStopped(); // Stop editing first

                    // 1. Update Status to CANCELLED immediately
                    DBDual.executeUpdateBoth("UPDATE vet_appointments SET status = 'CANCELLED' WHERE appt_id = ?", s -> {
                        s.setInt(1, apptId);
                    });

                    // Update UI immediately
                    tableModel.setValueAt("CANCELLED", modelRow, 4);
                    JOptionPane.showMessageDialog(panel, "Appointment Cancelled. Will be deleted in 30 seconds.");

                    // 2. Schedule Permanent Deletion (30 Seconds)
                    Timer deleteTimer = new Timer(30000, evt -> {
                        DBDual.executeUpdateBoth("DELETE FROM vet_appointments WHERE appt_id = ?", s -> {
                            s.setInt(1, apptId);
                        });
                        // Refresh table to remove the row
                        loadAppointments();
                    });
                    deleteTimer.setRepeats(false);
                    deleteTimer.start();
                }
            });

            panel.add(editBtn);
            panel.add(delBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            return panel;
        }
    }

    // Add helper method to apply emoji font support
    private void applyEmojiFontSupport(JLabel label, String text, int size, int style) {
        Font[] fonts = {
            new Font("Segoe UI Emoji", style, size),
            new Font("Apple Color Emoji", style, size),
            new Font("Noto Color Emoji", style, size),
            new Font("Symbola", style, size),
            new Font("Arial Unicode MS", style, size)
        };
        
        for (Font font : fonts) {
            if (font.canDisplayUpTo(text) == -1) {
                label.setFont(font);
                return;
            }
        }
        // Fallback to default with mixed font support
        label.setFont(new Font("Dialog", style, size));
    }
}