import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class AutoInstaller {

    private static final String DB_NAME = "pawpatrol_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static void runSilentSetup() {
        try {
            // 1. Connect to MySQL Server (No Database selected yet)
            String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";
            try (Connection conn = DriverManager.getConnection(serverUrl, USER, PASS);
                 Statement stmt = conn.createStatement()) {
                // Create Database
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            // 2. Connect to the 'pawpatrol_db'
            String dbUrl = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";
            try (Connection dbConn = DriverManager.getConnection(dbUrl, USER, PASS)) {

                // 3. Check if tables exist. If NOT, run the import.
                if (!areTablesPresent(dbConn)) {
                    // Check for the SQL file
                    File sqlFile = new File("pawpatrol_db.sql");

                    if (sqlFile.exists()) {
                        System.out.println("📂 Found database.sql, importing...");
                        importSQL(dbConn, sqlFile);
                    } else {
                        // DEBUGGING: Show error if file is missing
                        JOptionPane.showMessageDialog(null,
                                "❌ Setup Error: 'database.sql' not found!\n" +
                                        "Looking in: " + System.getProperty("user.dir") + "\n" +
                                        "Please ensure 'database.sql' is in the same folder as the .exe",
                                "Database Missing", JOptionPane.ERROR_MESSAGE);

                        // Fallback to internal setup (creates empty tables)
                        DatabaseSetup.main(null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Error:\n" + e.getMessage());
        }
    }

    private static boolean areTablesPresent(Connection conn) {
        try {
            // Try to select from a key table
            conn.createStatement().executeQuery("SELECT 1 FROM user_accounts LIMIT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --- ROBUST IMPORT METHOD ---
    private static void importSQL(Connection conn, File inputFile) {
        String line;
        StringBuilder sqlStatement = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false); // Speed up import

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("//") || line.startsWith("#") || line.startsWith("/*")) {
                    continue;
                }

                sqlStatement.append(line);

                // If line ends with semicolon, it's the end of a statement
                if (line.endsWith(";")) {
                    String finalSQL = sqlStatement.toString();
                    // Remove the semicolon at the end for JDBC
                    finalSQL = finalSQL.substring(0, finalSQL.length() - 1);

                    try {
                        stmt.execute(finalSQL);
                    } catch (Exception e) {
                        System.out.println("⚠️ SQL Warning: " + e.getMessage());
                    }

                    sqlStatement.setLength(0); // Reset for next statement
                } else {
                    sqlStatement.append(" "); // Add space for multi-line statements
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            JOptionPane.showMessageDialog(null, "✅ Database & Data Imported Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Import Failed: " + e.getMessage());
        }
    }
}