import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PawTrackAIService {

    // --- SECURITY FIX: Load from Environment or Config ---
    private static String getApiKey() {
        String key = System.getenv("PAWTRACK_API_KEY");
        if (key == null || key.isEmpty()) {
            // Fallback for development (Do not commit real keys to git)
            return "AIzaSyDqM12jm3eU0_3xC_S1AQJeUso7GOWsmgw";
        }
        return key;
    }

    private static final String ONLINE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + getApiKey();
    private static final String OFFLINE_URL = "http://localhost:11434/v1/chat/completions";
    private static final String OFFLINE_MODEL = "llama3.2:1b";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)) // Reduced timeout for snappier fallback
            .build();

    // --- MEMORY FIX: Increased Context Window ---
    private static final List<String> conversationHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 50;

    public static CompletableFuture<String> askAI(String userMessage) {
        // 1. Check Local Brain first (Fastest)
        String localAnswer = AIKnowledgeBase.searchMemory(userMessage);
        if (localAnswer != null) {
            return CompletableFuture.completedFuture(localAnswer);
        }

        // 2. Build Dynamic Context (Real-time DB Data)
        String dynamicContext = fetchRealTimeDatabaseContext();
        String systemPrompt = buildSystemPrompt(dynamicContext);

        // 3. Add to History
        addToHistory("user", userMessage);

        // 4. Try Online API
        return askOnlineAI(systemPrompt)
                .handle((result, ex) -> {
                    if (result != null && !result.startsWith("ERROR") && !result.startsWith("I cannot")) {
                        String clean = cleanResponse(result);
                        addToHistory("model", clean);
                        // Save good answers to local brain for next time
                        AIKnowledgeBase.learn(userMessage, clean);
                        return CompletableFuture.completedFuture(clean);
                    } else {
                        // 5. Robust Offline Fallback
                        return askOfflineAI(systemPrompt);
                    }
                }).thenCompose(f -> f);
    }

    // --- DB INTEGRATION: Fetch Live Data ---
    private static String fetchRealTimeDatabaseContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[REAL-TIME DATABASE INFO]\n");

        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            // Fetch Available Pets
            sb.append("Available Pets: ");
            ResultSet rs = stmt.executeQuery("SELECT name, breed, age FROM pets_accounts WHERE status = 'Available' LIMIT 5");
            List<String> pets = new ArrayList<>();
            while (rs.next()) {
                pets.add(rs.getString("name") + " (" + rs.getString("breed") + ", " + rs.getString("age") + ")");
            }
            if (pets.isEmpty()) sb.append("None currently.");
            else sb.append(String.join(", ", pets));
            sb.append(".\n");

            // Fetch Busy Vet Slots (To avoid double booking)
            sb.append("Busy Appointment Slots: ");
            rs = stmt.executeQuery("SELECT appt_date, appt_time FROM vet_appointments WHERE status = 'BOOKED' ORDER BY appt_id DESC LIMIT 5");
            List<String> busy = new ArrayList<>();
            while (rs.next()) {
                busy.add(rs.getString("appt_date") + " at " + rs.getString("appt_time"));
            }
            if (busy.isEmpty()) sb.append("Schedule is clear.");
            else sb.append(String.join(", ", busy));
            sb.append(".\n");

        } catch (Exception e) {
            sb.append("Database unavailable currently.");
        }
        return sb.toString();
    }

    private static String buildSystemPrompt(String dynamicContext) {
        return """
            ROLE: You are 'PawTrack Core', a helpful AI Assistant for a Pet Adoption & Vet Clinic app.
            
            RULES:
            1. Keep answers SHORT (max 2-3 sentences unless asked for details).
            2. No Markdown formatting (plain text only).
            3. If asked about pets or appointments, use the REAL-TIME INFO below.
            
            """ + dynamicContext + """
            
            [STATIC INFO]
            Clinic Hours: 8 AM - 8 PM Daily.
            Services: Adoption, Vet Checkups, Grooming, Surgery.
            """;
    }

    // --- ONLINE REQUEST ---
    private static CompletableFuture<String> askOnlineAI(String systemPrompt) {
        if (getApiKey().equals("YOUR_API_KEY_HERE")) return CompletableFuture.completedFuture("ERROR: No API Key");

        String jsonPayload = String.format(
                "{ \"system_instruction\": { \"parts\": { \"text\": \"%s\" } }, \"contents\": %s }",
                escapeJson(systemPrompt), buildGeminiHistory());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ONLINE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> res.statusCode() == 200 ? extractGeminiResponse(res.body()) : "ERROR");
    }

    // --- OFFLINE REQUEST (Robust) ---
    private static CompletableFuture<String> askOfflineAI(String systemPrompt) {
        // Check if Ollama is actually reachable before waiting for timeout
        return checkOllamaHealth().thenCompose(isUp -> {
            if (!isUp) return CompletableFuture.completedFuture("I'm currently offline and cannot reach the server.");

            String jsonPayload = String.format("""
                {
                    "model": "%s",
                    "messages": [ { "role": "system", "content": "%s" }, %s ],
                    "stream": false
                }
                """, OFFLINE_MODEL, escapeJson(systemPrompt), buildOllamaHistory());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OFFLINE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> res.statusCode() == 200 ? cleanResponse(extractOllamaResponse(res.body())) : "Offline Service Error.");
        });
    }

    private static CompletableFuture<Boolean> checkOllamaHealth() {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:11434/")).GET().build();
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> r.statusCode() == 200)
                .exceptionally(e -> false);
    }

    // --- UTILS ---
    private static void addToHistory(String role, String text) {
        if (conversationHistory.size() > MAX_HISTORY) conversationHistory.remove(0);
        conversationHistory.add(role + "|" + text);
    }

    private static String buildGeminiHistory() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < conversationHistory.size(); i++) {
            String[] parts = conversationHistory.get(i).split("\\|", 2);
            String role = parts[0].equals("assistant") ? "model" : parts[0];
            sb.append(String.format("{ \"role\": \"%s\", \"parts\": [{ \"text\": \"%s\" }] }", role, escapeJson(parts[1])));
            if (i < conversationHistory.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String buildOllamaHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < conversationHistory.size(); i++) {
            String[] parts = conversationHistory.get(i).split("\\|", 2);
            String role = parts[0].equals("model") ? "assistant" : parts[0];
            sb.append(String.format("{ \"role\": \"%s\", \"content\": \"%s\" }", role, escapeJson(parts[1])));
            if (i < conversationHistory.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private static String extractGeminiResponse(String json) { return manualParse(json, "\"text\":"); }
    private static String extractOllamaResponse(String json) { return manualParse(json, "\"content\":"); }

    private static String manualParse(String json, String keySearch) {
        try {
            int startIndex = json.indexOf(keySearch);
            if (startIndex == -1) return "No response.";
            int quoteStart = json.indexOf("\"", startIndex + keySearch.length());
            StringBuilder sb = new StringBuilder();
            boolean esc = false;
            for (int i = quoteStart + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (esc) {
                    // Handle escapes
                    if(c=='n') sb.append('\n'); else if(c=='"') sb.append('"'); else sb.append(c);
                    esc = false;
                } else {
                    if (c == '\\') esc = true; else if (c == '"') break; else sb.append(c);
                }
            }
            return sb.toString();
        } catch (Exception e) { return "Error parsing."; }
    }

    private static String cleanResponse(String input) { return input == null ? "" : input.replace("**", "").trim(); }
    private static String escapeJson(String input) { return input == null ? "" : input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "); }
}