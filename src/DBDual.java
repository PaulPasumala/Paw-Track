import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBDual {

    public interface StatementPreparer {
        void setParameters(PreparedStatement stmt) throws SQLException;
    }

    // Executes an INSERT/UPDATE/DELETE on BOTH databases (Online & Local)
    public static void executeUpdateBoth(String sql, StatementPreparer preparer) {
        // 1. Try Online
        new Thread(() -> {
            try (Connection conn = DBConnector.getOnlineConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        preparer.setParameters(stmt);
                        stmt.executeUpdate();
                        System.out.println("✅ [DualDB] Saved to Online DB");
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ [DualDB] Online Save Failed: " + e.getMessage());
            }
        }).start();

        // 2. Try Local (Offline)
        new Thread(() -> {
            try (Connection conn = DBConnector.getLocalConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        preparer.setParameters(stmt);
                        stmt.executeUpdate();
                        System.out.println("✅ [DualDB] Saved to Local DB");
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ [DualDB] Local Save Failed: " + e.getMessage());
            }
        }).start();
    }
}