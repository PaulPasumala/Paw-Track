// src/DatabaseDiscovery.java
import java.net.*;
import javax.swing.SwingUtilities;

public class DatabaseDiscovery {

    private static final int DISCOVERY_PORT = 9999;
    private static final String REQUEST_MSG = "WHO_HAS_DATABASE";
    private static final String RESPONSE_MSG = "I_HAVE_DATABASE";

    // --- 1. UPDATED INTERFACE (Now has 2 methods) ---
    public interface DiscoveryListener {
        void onServerFound(String ipAddress);
        void onDiscoveryFailed(); // New method for when scanning fails
    }

    // --- MODE 1: SERVER (Device 1) ---
    public static void startServerMode() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
                socket.setBroadcast(true);
                System.out.println("📡 Server Mode Started on Port " + DISCOVERY_PORT);

                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (message.equals(REQUEST_MSG)) {
                        String clientIP = packet.getAddress().getHostAddress();
                        System.out.println("🔎 Client ping from " + clientIP + ". Replying...");

                        byte[] responseBytes = RESPONSE_MSG.getBytes();
                        DatagramPacket response = new DatagramPacket(
                                responseBytes, responseBytes.length, packet.getAddress(), packet.getPort()
                        );
                        socket.send(response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- MODE 2: CLIENT (Device 2) ---
    public static void findServer(DiscoveryListener listener) { // Changed interface type
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setSoTimeout(5000); // 5 Second Timeout

                // 1. Send Request
                System.out.println("📡 Client: Broadcasting search...");
                byte[] requestBytes = REQUEST_MSG.getBytes();
                DatagramPacket request = new DatagramPacket(
                        requestBytes, requestBytes.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT
                );
                socket.send(request);

                // 2. Wait for Reply
                byte[] buffer = new byte[1024];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);

                try {
                    socket.receive(response);
                    String message = new String(response.getData(), 0, response.getLength());

                    if (message.equals(RESPONSE_MSG)) {
                        String serverIP = response.getAddress().getHostAddress();
                        // SUCCESS: Tell UI we found it
                        SwingUtilities.invokeLater(() -> listener.onServerFound(serverIP));
                    }
                } catch (SocketTimeoutException e) {
                    // FAIL: Tell UI we timed out (This was missing before!)
                    SwingUtilities.invokeLater(() -> listener.onDiscoveryFailed());
                }

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> listener.onDiscoveryFailed());
            }
        }).start();
    }
}