import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Vector;

public class TransactionHistory {

    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private static final Color ACCENT_COLOR = new Color(248, 250, 252);
    private static final Color TEXT_COLOR = new Color(30, 41, 59);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private static final Color PENDING_COLOR = new Color(245, 158, 11);
    private static final Color DEBIT_COLOR = new Color(239, 68, 68);

    public static JPanel createTransactionPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(ACCENT_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT_COLOR);
        JLabel titleLabel = new JLabel("💳 Transaction History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        header.add(titleLabel, BorderLayout.WEST);
        mainPanel.add(header, BorderLayout.NORTH);

        String[] columnNames = {"Date", "Description", "Amount", "Method", "Status", "Ref No."};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(card, BorderLayout.CENTER);

        loadData(model);

        return mainPanel;
    }

    private static void loadData(DefaultTableModel model) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // Use DBConnector. If table doesn't exist, it handles gracefully or user sees error.
                // Assuming 'transactions' table exists.
                try (Connection conn = DBConnector.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT date, description, amount, method, status, reference_no FROM transactions ORDER BY date DESC")) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getString("date"), rs.getString("description"),
                                rs.getDouble("amount"), rs.getString("method"),
                                rs.getString("status"), rs.getString("reference_no")
                        });
                    }
                } catch (Exception e) {
                    // Fallback if table missing or error
                    model.addRow(new Object[]{"-", "No transactions found / DB Error", 0.0, "-", "-", "-"});
                }
                return null;
            }
        };
        worker.execute();
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(TEXT_COLOR);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_COLOR);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(226, 232, 240)));
        // Make columns not movable
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final DecimalFormat fmt = new DecimalFormat("₱ #,##0.00");
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(noFocusBorder);
                if(!isSelected) c.setBackground(Color.WHITE);

                if (value instanceof Double d) {
                    setText(fmt.format(d));
                    setForeground(d < 0 ? DEBIT_COLOR : SUCCESS_COLOR);
                    setHorizontalAlignment(JLabel.RIGHT);
                } else if (col == 4) {
                    String status = value.toString();
                    setForeground("Pending".equals(status) ? PENDING_COLOR : SUCCESS_COLOR);
                    setFont(getFont().deriveFont(Font.BOLD));
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setForeground(TEXT_COLOR);
                    setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        });
    }
}