import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DashboardAdmin extends JFrame {

    // Color Palette (Clinic Theme)
    private final Color SIDEBAR_BG_TOP = new Color(33, 42, 60);
    private final Color SIDEBAR_BG_BOTTOM = new Color(25, 32, 45);
    private final Color ACTIVE_BTN_BG = new Color(255, 255, 255, 20);
    private final Color HOVER_BTN_BG = new Color(255, 255, 255, 10);
    private final Color ACCENT_COLOR = new Color(74, 144, 226);
    private final Color MAIN_BG = new Color(245, 247, 250);
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color TEXT_MUTED = new Color(160, 170, 185);

    // Action Colors
    private final Color BTN_APPROVE = new Color(34, 197, 94); // Green
    private final Color BTN_DENY = new Color(239, 68, 68);    // Red

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    // Lazy Loading Tracker
    private Map<String, Boolean> loadedPanels = new HashMap<>();

    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);

    // Tables
    private JTable accountsTable;
    private JTable adoptionTable;
    private JTable appointmentTable;

    // Security
    private String currentUser;
    private String currentSessionToken;
    private Timer securityTimer;

    // --- CONSTRUCTORS ---
    public DashboardAdmin() {
        this("admin", "test_token");
    }

    public DashboardAdmin(String username, String sessionToken) {
        this.currentUser = username;
        this.currentSessionToken = sessionToken;

        setTitle("Vet Clinic Command Center");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        mainContentPanel = new JPanel(new CardLayout());
        cardLayout = (CardLayout) mainContentPanel.getLayout();
        mainContentPanel.setBackground(MAIN_BG);

        // Load Dashboard immediately (Default View)
        loadPanelLazy("Dashboard");

        add(mainContentPanel, BorderLayout.CENTER);

        startSecurityCheck();

        // [ADDED] Fix for Window Settings and Emojis
        applyWindowFixes();
    }

    // --- LAZY LOADING LOGIC ---
    private void loadPanelLazy(String cardName) {
        if (loadedPanels.containsKey(cardName)) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            switch (cardName) {
                case "Dashboard":
                    mainContentPanel.add(createDashboardPanel(), "Dashboard");
                    break;
                case "Vet Requests":
                    mainContentPanel.add(createAppointmentRequestsPanel(), "Vet Requests");
                    loadAppointments();
                    break;
                case "Adoption Approvals":
                    mainContentPanel.add(createAdoptionApprovalPanel(), "Adoption Approvals");
                    loadAdoptions();
                    break;
                case "User Accounts":
                    mainContentPanel.add(createRegisteredAccountsPanel(), "User Accounts");
                    loadAccounts();
                    break;
            }
            loadedPanels.put(cardName, true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void switchCard(String cardName) {
        loadPanelLazy(cardName);

        if (cardName.equals("Dashboard")) updateDashboardStats();
        if (cardName.equals("Vet Requests") && appointmentTable != null) loadAppointments();
        if (cardName.equals("Adoption Approvals") && adoptionTable != null) loadAdoptions();
        if (cardName.equals("User Accounts") && accountsTable != null) loadAccounts();

        cardLayout.show(mainContentPanel, cardName);

        for (SidebarButton btn : sidebarButtons) btn.setActive(btn.getText().contains(cardName));
    }

    private void startSecurityCheck() {
        if ("test_token".equals(currentSessionToken)) return;
        securityTimer = new Timer(5000, e -> {
            new Thread(() -> {
                String dbToken = getSessionTokenFromDB(currentUser);
                if (dbToken != null && !dbToken.equals(currentSessionToken)) {
                    SwingUtilities.invokeLater(() -> {
                        securityTimer.stop();
                        JOptionPane.showMessageDialog(this, "Session Expired. Logged in from another device.");
                        dispose();
                        new PawTrackLogin().setVisible(true);
                    });
                }
            }).start();
        });
        securityTimer.start();
    }

    private String getSessionTokenFromDB(String username) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT session_token FROM user_accounts WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("session_token");
        } catch (Exception e) {}
        return null;
    }

    private void generateInviteKey() {
        String key = "KEY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        try (Connection conn = DBConnector.getConnection()) {
            DBDual.executeUpdateBoth("UPDATE invite_keys SET is_used = TRUE WHERE generated_by = ? AND is_used = FALSE", s->s.setString(1, currentUser));
            DBDual.executeUpdateBoth("INSERT INTO invite_keys (access_key, generated_by) VALUES (?, ?)", s->{
                s.setString(1, key);
                s.setString(2, currentUser);
            });
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(key), null);
            JOptionPane.showMessageDialog(this, "New Key Generated & Copied:\n" + key);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, SIDEBAR_BG_TOP, 0, getHeight(), SIDEBAR_BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setLayout(new BorderLayout());

        JPanel profilePanel = new JPanel();
        profilePanel.setOpaque(false);
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel avatar = new JLabel("🏥", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Vet Clinic");
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Admin Panel");
        roleLabel.setForeground(TEXT_MUTED);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profilePanel.add(avatar);
        profilePanel.add(Box.createVerticalStrut(10));
        profilePanel.add(nameLabel);
        profilePanel.add(roleLabel);

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        addSidebarBtn(menuPanel, "Dashboard", "⊞", e -> switchCard("Dashboard"));
        addSidebarBtn(menuPanel, "Vet Requests", "📅", e -> switchCard("Vet Requests"));
        addSidebarBtn(menuPanel, "Adoption Approvals", "🐶", e -> switchCard("Adoption Approvals"));
        addSidebarBtn(menuPanel, "User Accounts", "👥", e -> switchCard("User Accounts"));

        menuPanel.add(Box.createVerticalGlue());

        SidebarButton btnKey = new SidebarButton("Generate Key", "🔑", e -> generateInviteKey());
        menuPanel.add(btnKey);

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);

        JButton btnLogout = new JButton("LOGOUT");
        styleLogoutButton(btnLogout);
        btnLogout.addActionListener(e -> {
            if (securityTimer != null) securityTimer.stop();
            dispose();
            new PawTrackLogin().setVisible(true);
        });
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(20, 20, 30, 20));
        bot.add(btnLogout);
        sidebar.add(bot, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addSidebarBtn(JPanel panel, String text, String icon, ActionListener action) {
        SidebarButton btn = new SidebarButton(text, icon, action);
        if (sidebarButtons.isEmpty()) btn.setActive(true);
        sidebarButtons.add(btn);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(5));
    }

    // --- DASHBOARD OVERVIEW ---
    private JLabel lblPendingAppts;
    private JLabel lblPendingAdoptions;
    private JLabel lblActivePairs;

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Clinic Overview");
        title.setFont(HEADER_FONT);
        title.setForeground(SIDEBAR_BG_TOP);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setOpaque(false);

        lblPendingAppts = new JLabel("0", SwingConstants.CENTER);
        lblPendingAdoptions = new JLabel("0", SwingConstants.CENTER);
        lblActivePairs = new JLabel("0", SwingConstants.CENTER);

        styleStatLabel(lblPendingAppts);
        styleStatLabel(lblPendingAdoptions);
        styleStatLabel(lblActivePairs);

        statsGrid.add(createStatCard("Pending Appts", lblPendingAppts, new Color(99, 102, 241)));
        statsGrid.add(createStatCard("Adoption Requests", lblPendingAdoptions, new Color(236, 72, 153)));
        statsGrid.add(createStatCard("Active Pairs", lblActivePairs, new Color(16, 185, 129)));

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(title, BorderLayout.NORTH);
        container.add(Box.createVerticalStrut(30), BorderLayout.CENTER);
        container.add(statsGrid, BorderLayout.SOUTH);

        panel.add(container, BorderLayout.NORTH);

        updateDashboardStats();
        return panel;
    }

    private void styleStatLabel(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lbl.setForeground(SIDEBAR_BG_TOP);
    }

    private void updateDashboardStats() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String c1, c2, c3;
            @Override
            protected Void doInBackground() {
                c1 = getCount("vet_appointments", "PENDING");
                c2 = getCount("adoption_applications", "Pending");
                c3 = getCount("active_breeding_pairs", "Expecting");
                return null;
            }
            @Override
            protected void done() {
                if(lblPendingAppts != null) lblPendingAppts.setText(c1);
                if(lblPendingAdoptions != null) lblPendingAdoptions.setText(c2);
                if(lblActivePairs != null) lblActivePairs.setText(c3);
            }
        };
        worker.execute();
    }

    private String getCount(String table, String status) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE status = ?")) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) {}
        return "0";
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color c) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, c));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.GRAY);
        titleLbl.setBorder(new EmptyBorder(0,0,15,0));

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(0, 150));
        return card;
    }

    // --- 1. APPOINTMENT REQUESTS PANEL ---
    private JPanel createAppointmentRequestsPanel() {
        JPanel panel = createTablePanel("Pending Vet Appointments");

        String[] cols = {"ID", "Pet", "Owner", "Vet", "Date", "Time", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col == 6; }
        };
        appointmentTable = new JTable(model);
        styleTable(appointmentTable);

        appointmentTable.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        appointmentTable.getColumnModel().getColumn(6).setCellEditor(new AppointmentActionEditor());

        JScrollPane scroll = new JScrollPane(appointmentTable);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadAppointments() {
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT appt_id, pet_name, owner_name, vet_name, appt_date, appt_time FROM vet_appointments WHERE status = 'PENDING'")) {
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("appt_id"), rs.getString("pet_name"), rs.getString("owner_name"),
                        rs.getString("vet_name"), rs.getString("appt_date"), rs.getString("appt_time"), ""
                });
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    // --- 2. ADOPTION APPROVALS PANEL ---
    private JPanel createAdoptionApprovalPanel() {
        JPanel panel = createTablePanel("Pending Adoptions");

        String[] cols = {"ID", "Applicant", "Pet", "Income/Job", "Housing", "Other Pets", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col == 6; }
        };
        adoptionTable = new JTable(model);
        styleTable(adoptionTable);

        adoptionTable.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        adoptionTable.getColumnModel().getColumn(6).setCellEditor(new AdoptionActionEditor());

        panel.add(new JScrollPane(adoptionTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadAdoptions() {
        DefaultTableModel model = (DefaultTableModel) adoptionTable.getModel();
        model.setRowCount(0);
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT app_id, applicant_name, pet_name, occupation, residency_type, has_other_pets FROM adoption_applications WHERE status = 'Pending'")) {
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("app_id"), rs.getString("applicant_name"), rs.getString("pet_name"),
                        rs.getString("occupation"), rs.getString("residency_type"), rs.getString("has_other_pets"), ""
                });
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    // --- 3. REGISTERED ACCOUNTS PANEL (Restored Editing) ---
    private JPanel createRegisteredAccountsPanel() {
        JPanel panel = createTablePanel("All User Accounts");
        String[] cols = {"Name", "Username", "Email", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // [RESTORED] Role column is editable
            }
        };
        accountsTable = new JTable(model);
        styleTable(accountsTable);
        panel.add(new JScrollPane(accountsTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadAccounts() {
        DefaultTableModel model = (DefaultTableModel) accountsTable.getModel();
        model.setRowCount(0);
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT first_name, last_name, username, email_address, role FROM user_accounts")) {
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("username"), rs.getString("email_address"), rs.getString("role")
                });
            }
        } catch(Exception e) {}

        // [RESTORED] Role Editor
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"ADMIN", "USER"});
        accountsTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(roleComboBox));

        // [RESTORED] Update Listener
        model.addTableModelListener(e -> {
            if (e.getColumn() == 3) {
                int row = e.getFirstRow();
                String username = (String) model.getValueAt(row, 1);
                String newRole = (String) model.getValueAt(row, 3);
                updateUserRole(username, newRole);
            }
        });
    }

    // [RESTORED] Logic
    private void updateUserRole(String username, String newRole) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE user_accounts SET role = ? WHERE username = ?")) {
            stmt.setString(1, newRole);
            stmt.setString(2, username);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Role updated for " + username);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // --- HELPER CLASSES & RENDERERS ---

    private JPanel createTablePanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        JLabel lbl = new JLabel(title);
        lbl.setFont(HEADER_FONT);
        lbl.setForeground(SIDEBAR_BG_TOP);
        lbl.setBorder(new EmptyBorder(0,0,20,0));
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setFont(MAIN_FONT);
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setForeground(TEXT_MUTED);

        // [FIXED] Columns are locked
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
    }

    // -- ACTION EDITORS FOR BUTTONS IN TABLE --

    class AppointmentActionEditor extends DefaultCellEditor {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        public AppointmentActionEditor() {
            super(new JCheckBox());
            JButton approve = createBtn("Confirm", BTN_APPROVE);
            JButton deny = createBtn("Decline", BTN_DENY);

            approve.addActionListener(e -> {
                int id = (int) appointmentTable.getValueAt(appointmentTable.getSelectedRow(), 0);
                updateAppt(id, "CONFIRMED");
                fireEditingStopped();
            });
            deny.addActionListener(e -> {
                int id = (int) appointmentTable.getValueAt(appointmentTable.getSelectedRow(), 0);
                updateAppt(id, "CANCELLED");
                fireEditingStopped();
            });
            p.add(approve); p.add(deny);
            p.setBackground(Color.WHITE);
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return p; }
    }

    private void updateAppt(int id, String status) {
        DBDual.executeUpdateBoth("UPDATE vet_appointments SET status = ? WHERE appt_id = ?", s -> {
            s.setString(1, status);
            s.setInt(2, id);
        });
        loadAppointments();
        JOptionPane.showMessageDialog(this, "Appointment marked as " + status);
    }

    class AdoptionActionEditor extends DefaultCellEditor {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        public AdoptionActionEditor() {
            super(new JCheckBox());
            JButton approve = createBtn("Approve", BTN_APPROVE);
            JButton reject = createBtn("Reject", BTN_DENY);

            approve.addActionListener(e -> processAdoption(true));
            reject.addActionListener(e -> processAdoption(false));

            p.add(approve); p.add(reject);
            p.setBackground(Color.WHITE);
        }
        private void processAdoption(boolean approved) {
            int row = adoptionTable.getSelectedRow();
            int appId = (int) adoptionTable.getValueAt(row, 0);
            String petName = (String) adoptionTable.getValueAt(row, 2);

            if (approved) {
                DBDual.executeUpdateBoth("UPDATE adoption_applications SET status = 'Approved' WHERE app_id = ?", s->s.setInt(1, appId));
                DBDual.executeUpdateBoth("UPDATE pets_accounts SET status = 'Adopted' WHERE name = ?", s->s.setString(1, petName));
                JOptionPane.showMessageDialog(null, petName + " has been successfully adopted!");
            } else {
                DBDual.executeUpdateBoth("UPDATE adoption_applications SET status = 'Rejected' WHERE app_id = ?", s->s.setInt(1, appId));
                DBDual.executeUpdateBoth("UPDATE pets_accounts SET status = 'Available' WHERE name = ?", s->s.setString(1, petName));
                JOptionPane.showMessageDialog(null, "Application rejected. " + petName + " is available again.");
            }
            fireEditingStopped();
            loadAdoptions();
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return p; }
    }

    private JButton createBtn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(80, 25));
        return b;
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            p.removeAll();
            p.add(createBtn("✔", BTN_APPROVE));
            p.add(createBtn("✖", BTN_DENY));
            p.setBackground(s ? t.getSelectionBackground() : Color.WHITE);
            return p;
        }
    }

    private void styleLogoutButton(JButton btn) {
        btn.setBackground(new Color(231, 76, 60, 20));
        btn.setForeground(new Color(255, 100, 100));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(231, 76, 60), 1));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 45));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(231, 76, 60)); btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(231, 76, 60, 20)); btn.setForeground(new Color(255, 100, 100)); }
        });
    }

    private class SidebarButton extends JButton {
        private boolean isActive = false;
        public SidebarButton(String text, String icon, ActionListener action) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(TEXT_COLOR);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addActionListener(action);
            setBorder(new EmptyBorder(12, 20, 12, 20));
            if(icon != null) setText(icon + "  " + text);
        }
        public void setActive(boolean active) { this.isActive = active; repaint(); }
        protected void paintComponent(Graphics g) {
            if (isActive) { g.setColor(ACTIVE_BTN_BG); g.fillRect(0,0,getWidth(),getHeight()); g.setColor(ACCENT_COLOR); g.fillRect(0,0,5,getHeight()); }
            else if (getModel().isRollover()) { g.setColor(HOVER_BTN_BG); g.fillRect(0,0,getWidth(),getHeight()); }
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new DashboardAdmin().setVisible(true)); }

    // [ADDED] Fix for Window Settings and Emojis
    public void applyWindowFixes() {
        // 1. Make Full Screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 2. Fix Sidebar Icons (Use text characters instead of emojis)
        for (SidebarButton btn : sidebarButtons) {
            String txt = btn.getText();
            if (txt.contains("Dashboard")) btn.setText("::  Dashboard");
            else if (txt.contains("Vet Requests")) btn.setText("+  Vet Requests");
            else if (txt.contains("Adoption")) btn.setText("O  Adoption Approvals");
            else if (txt.contains("User")) btn.setText("@  User Accounts");
            else if (txt.contains("Key")) btn.setText("* Generate Key");
        }

        // 3. Fix Table Action Buttons (Add listener for lazy-loaded tables)
        mainContentPanel.addContainerListener(new java.awt.event.ContainerAdapter() {
            @Override
            public void componentAdded(java.awt.event.ContainerEvent e) {
                fixTables();
            }
        });
        // Also try fixing immediately
        fixTables();

        // Refresh visuals
        revalidate();
        repaint();
    }

    private void fixTables() {
        if (appointmentTable != null) {
            appointmentTable.getColumnModel().getColumn(6).setCellRenderer(new SafeActionRenderer());
            appointmentTable.getColumnModel().getColumn(6).setCellEditor(new SafeAppointmentActionEditor(new JCheckBox()));
        }
        if (adoptionTable != null) {
            adoptionTable.getColumnModel().getColumn(6).setCellRenderer(new SafeActionRenderer());
            adoptionTable.getColumnModel().getColumn(6).setCellEditor(new SafeAdoptionActionEditor(new JCheckBox()));
        }
    }

    // [ADDED] Safe Renderer using Text instead of Emojis
    class SafeActionRenderer extends DefaultTableCellRenderer {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            p.removeAll();
            p.add(createBtn("YES", BTN_APPROVE));
            p.add(createBtn("NO", BTN_DENY));
            p.setBackground(s ? t.getSelectionBackground() : Color.WHITE);
            return p;
        }
    }

    // [ADDED] Safe Editors
    class SafeAppointmentActionEditor extends DefaultCellEditor {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        public SafeAppointmentActionEditor(JCheckBox checkBox) {
            super(checkBox);
            JButton approve = createBtn("YES", BTN_APPROVE);
            JButton deny = createBtn("NO", BTN_DENY);

            approve.addActionListener(e -> {
                int id = (int) appointmentTable.getValueAt(appointmentTable.getSelectedRow(), 0);
                updateAppt(id, "CONFIRMED");
                fireEditingStopped();
            });
            deny.addActionListener(e -> {
                int id = (int) appointmentTable.getValueAt(appointmentTable.getSelectedRow(), 0);
                updateAppt(id, "CANCELLED");
                fireEditingStopped();
            });
            p.add(approve); p.add(deny);
            p.setBackground(Color.WHITE);
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return p; }
    }

    class SafeAdoptionActionEditor extends DefaultCellEditor {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        public SafeAdoptionActionEditor(JCheckBox checkBox) {
            super(checkBox);
            JButton approve = createBtn("YES", BTN_APPROVE);
            JButton reject = createBtn("NO", BTN_DENY);

            approve.addActionListener(e -> processAdoption(true));
            reject.addActionListener(e -> processAdoption(false));

            p.add(approve); p.add(reject);
            p.setBackground(Color.WHITE);
        }
        private void processAdoption(boolean approved) {
            int row = adoptionTable.getSelectedRow();
            int appId = (int) adoptionTable.getValueAt(row, 0);
            String petName = (String) adoptionTable.getValueAt(row, 2);

            if (approved) {
                DBDual.executeUpdateBoth("UPDATE adoption_applications SET status = 'Approved' WHERE app_id = ?", s->s.setInt(1, appId));
                DBDual.executeUpdateBoth("UPDATE pets_accounts SET status = 'Adopted' WHERE name = ?", s->s.setString(1, petName));
                JOptionPane.showMessageDialog(null, petName + " has been successfully adopted!");
            } else {
                DBDual.executeUpdateBoth("UPDATE adoption_applications SET status = 'Rejected' WHERE app_id = ?", s->s.setInt(1, appId));
                DBDual.executeUpdateBoth("UPDATE pets_accounts SET status = 'Available' WHERE name = ?", s->s.setString(1, petName));
                JOptionPane.showMessageDialog(null, "Application rejected. " + petName + " is available again.");
            }
            fireEditingStopped();
            loadAdoptions();
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return p; }
    }
}