import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    private static final SecureRandom RAND = new SecureRandom();
    private static final int SALT_LEN = 16;

    public static String hash(String password) throws Exception {
        if (password == null) password = "";
        byte[] salt = new byte[SALT_LEN];
        RAND.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[salt.length + hashed.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hashed, 0, combined, salt.length, hashed.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static boolean verify(String password, String stored) throws Exception {
        if (stored == null || stored.isEmpty()) return false;
        byte[] combined = Base64.getDecoder().decode(stored);
        if (combined.length <= SALT_LEN) return false;
        byte[] salt = new byte[SALT_LEN];
        System.arraycopy(combined, 0, salt, 0, SALT_LEN);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashed = md.digest((password == null ? "" : password).getBytes(StandardCharsets.UTF_8));
        int hashedLen = combined.length - SALT_LEN;
        if (hashed.length != hashedLen) return false;
        for (int i = 0; i < hashedLen; i++) {
            if (hashed[i] != combined[SALT_LEN + i]) return false;
        }
        return true;
    }
}