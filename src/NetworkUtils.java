import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

public class NetworkUtils {

    public static String getSmartIP() {
        // 1. Check the flag from your existing DBConnector
        // This prevents the app from hanging if we already know the internet is down
        if (DBConnector.isOfflineMode) {
            return "Offline Mode (" + getHotspotIP() + ")";
        }

        // 2. If Online, try to fetch Public IP with a short timeout
        // This gets the REAL internet IP, which is great for security logs
        try {
            URL url = new URL("https://checkip.amazonaws.com");
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(2000); // Give up after 2 seconds to keep app snappy
            conn.setReadTimeout(2000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return br.readLine().trim();
        } catch (Exception e) {
            // 3. Fallback: If AWS fails or times out, just use local IP
            return getHotspotIP();
        }
    }

    // --- [UPDATED] Robust IP Finder for Hotspots/LAN ---
    // Instead of just asking for "LocalHost" (which often says 127.0.0.1),
    // this iterates through actual network cards to find the real IP (e.g., 192.168.x.x)
    private static String getHotspotIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // Skip Loopback (127.0.0.1), Down interfaces, and Virtual adapters (optional)
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // We check for IPv4 addresses (no colons) and ensure it's not a loopback
                    if (addr.getHostAddress().indexOf(':') == -1 && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            // Fallback if advanced search fails
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}