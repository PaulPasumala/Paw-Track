// src/HardwareUtils.java
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class HardwareUtils {

    public static String getDeviceFingerprint() {
        try {
            // 1. Try to get MAC Address
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null && mac.length > 0 && !network.isLoopback() && !network.isVirtual() && network.isUp()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    String macStr = sb.toString();
                    System.out.println("DEBUG: Found MAC Address: " + macStr);
                    return macStr;
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Failed to get MAC Address: " + e.getMessage());
        }

        // 2. Fallback: If MAC fails, use Computer Name + OS User (e.g., "DESKTOP-123/Julian")
        // This is less secure than MAC but works 100% of the time as a "Fingerprint"
        try {
            String computerName = InetAddress.getLocalHost().getHostName();
            String userName = System.getProperty("user.name");
            String fallbackID = "ID:" + computerName + "/" + userName;
            System.out.println("DEBUG: Using Fallback ID: " + fallbackID);
            return fallbackID;
        } catch (Exception e) {
            return "UNKNOWN-DEVICE";
        }
    }
}