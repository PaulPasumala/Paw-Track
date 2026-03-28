import java.sql.*;

public class DataSync {

    // Pulls all User Data from Online and saves it to Local DB
    public static void syncUserData(String username) {
        System.out.println("🔄 [Sync] Starting Data Synchronization for: " + username);

        try (Connection online = DBConnector.getOnlineConnection();
             Connection local = DBConnector.getLocalConnection()) {

            if (online == null || local == null) {
                System.out.println("⚠️ [Sync] Cannot sync - one connection is missing.");
                return;
            }

            // 1. Sync User Profile
            // FIXED: Explicitly selecting columns to match INSERT order
            syncTable(online, local,
                    "SELECT user_id, last_name, first_name, middle_name, username, password, contact_number, email_address, role, last_ip, session_token, trusted_devices FROM user_accounts WHERE username = ?",
                    "REPLACE INTO user_accounts (user_id, last_name, first_name, middle_name, username, password, contact_number, email_address, role, last_ip, session_token, trusted_devices) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    username, 12);

            // 2. Sync Pets (Owned & Registered)
            // FIXED: Explicitly selecting columns. 'image' matches 'image', 'age' matches 'age'.
            syncTable(online, local,
                    "SELECT pet_id, name, gender, age, breed, health_status, contact_number, personal_traits, reason_for_adoption, image, status, owner_username FROM pets_accounts WHERE owner_username = ?",
                    "REPLACE INTO pets_accounts (pet_id, name, gender, age, breed, health_status, contact_number, personal_traits, reason_for_adoption, image, status, owner_username) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    username, 12);

            // 3. Sync Adoption Applications
            // FIXED: Explicitly selecting columns
            syncTable(online, local,
                    "SELECT app_id, applicant_name, pet_name, status, application_date, email, contact_number, address, middle_name, dob, age, gender, occupation, province, city, barangay, residency_type, has_other_pets FROM adoption_applications WHERE applicant_name = ?",
                    "REPLACE INTO adoption_applications (app_id, applicant_name, pet_name, status, application_date, email, contact_number, address, middle_name, dob, age, gender, occupation, province, city, barangay, residency_type, has_other_pets) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    username, 18);

            // 4. Sync Transactions
            // FIXED: Explicitly selecting columns
            syncTable(online, local,
                    "SELECT id, username, date, description, type, amount, method, status, reference_no FROM transactions WHERE username = ?",
                    "REPLACE INTO transactions (id, username, date, description, type, amount, method, status, reference_no) VALUES (?,?,?,?,?,?,?,?,?)",
                    username, 9);

            System.out.println("✅ [Sync] Synchronization Complete. Local DB is up to date.");

        } catch (Exception e) {
            System.err.println("❌ [Sync] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void syncTable(Connection source, Connection dest, String selectSql, String insertSql, String param, int columnCount) throws SQLException {
        try (PreparedStatement fetch = source.prepareStatement(selectSql)) {
            fetch.setString(1, param);
            ResultSet rs = fetch.executeQuery();

            try (PreparedStatement push = dest.prepareStatement(insertSql)) {
                int count = 0;
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        push.setObject(i, rs.getObject(i));
                    }
                    push.addBatch();
                    count++;
                }
                push.executeBatch();
                if (count > 0) {
                    System.out.println("   -> Synced " + count + " records.");
                }
            }
        }
    }
}