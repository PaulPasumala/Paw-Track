import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    // --- EXISTING TABLES ---
    private static final String CREATE_ACTIVE_BREEDING_PAIRS_TABLE = "CREATE TABLE IF NOT EXISTS active_breeding_pairs (pair_id INT AUTO_INCREMENT PRIMARY KEY, female_pet_name VARCHAR(255), male_pet_name VARCHAR(255), pairing_date DATE, expected_due_date DATE, litter_size_estimate VARCHAR(50), status VARCHAR(50), last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
    private static final String CREATE_LITTER_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS litter_history (litter_id INT AUTO_INCREMENT PRIMARY KEY, female_pet_name VARCHAR(255), litter_date DATE, puppy_kitten_count INT, adoption_status VARCHAR(50), notes TEXT, status VARCHAR(50))";
    private static final String CREATE_VET_APPOINTMENTS_TABLE = "CREATE TABLE IF NOT EXISTS vet_appointments (appt_id INT AUTO_INCREMENT PRIMARY KEY, pet_name VARCHAR(255), owner_name VARCHAR(255), vet_name VARCHAR(255), appt_date VARCHAR(50), appt_time VARCHAR(50), booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, status VARCHAR(50))";
    private static final String CREATE_PETS_ACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS pets_accounts (pet_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), gender VARCHAR(50), age VARCHAR(50), breed VARCHAR(100), health_status VARCHAR(255), contact_number VARCHAR(50), personal_traits TEXT, reason_for_adoption TEXT, image LONGBLOB, status VARCHAR(50) DEFAULT 'Available', owner_username VARCHAR(100))";

    // --- [UPDATED] Added pet_id column ---
    private static final String CREATE_ADOPTION_APPS_TABLE = "CREATE TABLE IF NOT EXISTS adoption_applications (" +
            "app_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "pet_id INT, " + // <--- ADDED THIS
            "applicant_name VARCHAR(255), " +
            "middle_name VARCHAR(100), " +
            "email VARCHAR(255), " +
            "contact_number VARCHAR(50), " +
            "dob VARCHAR(20), " +
            "age VARCHAR(10), " +
            "gender VARCHAR(20), " +
            "occupation VARCHAR(100), " +
            "province VARCHAR(100), " +
            "city VARCHAR(100), " +
            "barangay VARCHAR(100), " +
            "address TEXT, " +
            "residency_type VARCHAR(50), " +
            "has_other_pets VARCHAR(10), " +
            "pet_name VARCHAR(100), " +
            "status VARCHAR(50) DEFAULT 'Pending', " +
            "application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

    private static final String CREATE_USER_ACCOUNTS_TABLE =
            "CREATE TABLE IF NOT EXISTS user_accounts (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "last_name VARCHAR(100) NOT NULL, " +
                    "first_name VARCHAR(100) NOT NULL, " +
                    "middle_name VARCHAR(100), " +
                    "username VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "contact_number VARCHAR(50), " +
                    "email_address VARCHAR(255), " +
                    "role VARCHAR(20) DEFAULT 'USER', " +
                    "last_ip VARCHAR(50), " +
                    "session_token VARCHAR(100), " +
                    "trusted_devices TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    private static final String CREATE_INVITE_KEYS_TABLE =
            "CREATE TABLE IF NOT EXISTS invite_keys (" +
                    "key_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "access_key VARCHAR(50) UNIQUE NOT NULL, " +
                    "generated_by VARCHAR(100), " +
                    "is_used BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    public static void main(String[] args) {
        System.out.println("Starting database setup...");

        Connection conn = null;

        try {
            conn = DBConnector.getConnection();
            System.out.println("✅ Connected to Standard Database.");
        } catch (SQLException e) {
            System.err.println("⚠️ Standard Connection Failed (" + e.getMessage() + ")");
            System.out.println("🔄 Switching to OFFLINE Mode (Local Database)...");
            DBConnector.isOfflineMode = true;
            try {
                conn = DBConnector.getConnection();
                System.out.println("✅ Connected to Local Database (Offline Mode).");
            } catch (SQLException ex) {
                System.err.println("❌ Critical Error: Could not connect to ANY database.");
                ex.printStackTrace();
                return;
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_ACTIVE_BREEDING_PAIRS_TABLE);
            stmt.execute(CREATE_LITTER_HISTORY_TABLE);
            stmt.execute(CREATE_VET_APPOINTMENTS_TABLE);
            stmt.execute(CREATE_USER_ACCOUNTS_TABLE);
            stmt.execute(CREATE_PETS_ACCOUNTS_TABLE);
            stmt.execute(CREATE_ADOPTION_APPS_TABLE);
            stmt.execute(CREATE_INVITE_KEYS_TABLE);

            // --- MIGRATION: ADD pet_id TO ADOPTION_APPLICATIONS ---
            try {
                stmt.execute("ALTER TABLE adoption_applications ADD COLUMN pet_id INT");
                System.out.println("✅ Auto-Added column: pet_id");
            } catch (SQLException e) { } // Ignore if exists

            // --- OTHER MIGRATIONS (KEEPING ORIGINAL) ---
            String[] newColumns = {
                    "middle_name VARCHAR(100)", "dob VARCHAR(20)", "age VARCHAR(10)",
                    "gender VARCHAR(20)", "occupation VARCHAR(100)", "province VARCHAR(100)",
                    "city VARCHAR(100)", "barangay VARCHAR(100)", "residency_type VARCHAR(50)",
                    "has_other_pets VARCHAR(10)"
            };
            for (String colDef : newColumns) {
                try { stmt.execute("ALTER TABLE adoption_applications ADD COLUMN " + colDef); } catch (SQLException e) {}
            }
            try { stmt.execute("ALTER TABLE user_accounts ADD COLUMN trusted_devices TEXT"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE user_accounts ADD COLUMN role VARCHAR(20) DEFAULT 'USER'"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE user_accounts ADD COLUMN last_ip VARCHAR(50)"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE user_accounts ADD COLUMN session_token VARCHAR(100)"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE pets_accounts ADD COLUMN owner_username VARCHAR(100)"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE vet_appointments ADD COLUMN username VARCHAR(100)"); } catch (SQLException e) {}

            try {
                String hashedPassword = PasswordUtils.hash("admin123");
                String adminSql = "INSERT IGNORE INTO user_accounts " +
                        "(last_name, first_name, username, password, email_address, role) " +
                        "VALUES ('System', 'Admin', 'admin', '" + hashedPassword + "', 'admin@pawtrack.com', 'ADMIN')";
                stmt.execute(adminSql);
            } catch (Exception e) {}

            System.out.println("✅ Database Setup & Migration Complete.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}