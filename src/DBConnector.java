// src/DBConnector.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    // --- 1. ONLINE DATABASE CONFIGURATION (Aiven) ---
    private static final String ONLINE_HOST = "mysql-1cd59eaa-pasumalapaul384-1f5a.e.aivencloud.com";
    private static final String ONLINE_PORT = "25034";
    private static final String ONLINE_DB_NAME = "defaultdb";
    private static final String ONLINE_USER = "avnadmin";
    private static final String ONLINE_PASS = "AVNS_v8f4dTqCGKvFyWwE6Ik";

    // --- 2. OFFLINE (LOCAL) DATABASE CONFIGURATION ---
    private static final String LOCAL_HOST = "localhost";
    private static final String LOCAL_PORT = "3306";
    private static final String LOCAL_DB_NAME = "pawpatrol_db"; // Your local DB name
    private static final String LOCAL_USER = "root";
    private static final String LOCAL_PASS = "";

    // Dynamic IP for "Find Database" feature (defaults to localhost)
    private static String serverIP = "localhost";
    public static boolean isOfflineMode = false;

    // --- 3. CONNECTION METHODS ---


    public static Connection getConnection() throws SQLException {
        if (isOfflineMode) return getLocalConnection();

        try {
            return getOnlineConnection();
        } catch (SQLException e) {
            System.err.println("⚠️ Online Connection Failed: " + e.getMessage());
            System.out.println("🔄 Auto-Switching to OFFLINE Mode (" + LOCAL_DB_NAME + ")...");
            isOfflineMode = true;
            return getLocalConnection();
        }
    }

    public static Connection getOnlineConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + ONLINE_HOST + ":" + ONLINE_PORT + "/" + ONLINE_DB_NAME +
                    "?useSSL=true&requireSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&connectTimeout=5000";
            return DriverManager.getConnection(url, ONLINE_USER, ONLINE_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }

    public static Connection getLocalConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Uses 'serverIP' which defaults to localhost but can be changed by setServerIP()
            String targetHost = (serverIP != null && !serverIP.isEmpty()) ? serverIP : LOCAL_HOST;

            // ✅ CHANGED: Increased timeout from 2000 to 10000 (10 seconds)
            // This gives the internet enough time to send the data to your laptop
            String url = "jdbc:mysql://" + targetHost + ":" + LOCAL_PORT + "/" + LOCAL_DB_NAME +
                    "?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=90000";

            return DriverManager.getConnection(url, LOCAL_USER, LOCAL_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }

    public static void setServerIP(String ip) {
        System.out.println("🔌 DBConnector: Switching Target IP to " + ip);
        serverIP = ip;
        isOfflineMode = true; // If we set a manual IP, we are usually targeting a local network peer
    }
}