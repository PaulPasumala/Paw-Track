import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AIKnowledgeBase {

    private static final String MEMORY_FILE = "pawtrack_brain.dat";
    private static final Map<String, String> knowledgeMap = new ConcurrentHashMap<>();

    static {
        loadMemory();
    }

    public static String searchMemory(String userQuestion) {
        String target = cleanText(userQuestion);
        if (target.length() < 5) return null;

        String bestMatchAnswer = null;
        double highestScore = 0.0;

        for (Map.Entry<String, String> entry : knowledgeMap.entrySet()) {
            String storedQuestion = entry.getKey();
            // IMPROVEMENT: Use Cosine Similarity instead of Jaccard
            double score = calculateCosineSimilarity(target, storedQuestion);

            // Threshold lowered slightly as Cosine is more strict but accurate
            if (score > 0.80) {
                if (score > highestScore) {
                    highestScore = score;
                    bestMatchAnswer = entry.getValue();
                }
            }
        }

        if (bestMatchAnswer != null) {
            System.out.println("⚡ Memory Hit! (Score: " + String.format("%.2f", highestScore) + ")");
            return bestMatchAnswer;
        }

        return null;
    }

    public static void learn(String question, String answer) {
        if (question == null || answer == null) return;
        String cleanQ = cleanText(question);
        if (knowledgeMap.containsKey(cleanQ)) return;
        knowledgeMap.put(cleanQ, answer);
        saveMemory();
    }

    // --- IMPROVED ALGORITHM: COSINE SIMILARITY ---
    private static double calculateCosineSimilarity(String s1, String s2) {
        Map<String, Integer> vector1 = getTermFrequencyMap(s1);
        Map<String, Integer> vector2 = getTermFrequencyMap(s2);

        Set<String> allWords = new HashSet<>(vector1.keySet());
        allWords.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String word : allWords) {
            int v1 = vector1.getOrDefault(word, 0);
            int v2 = vector2.getOrDefault(word, 0);
            dotProduct += v1 * v2;
            normA += Math.pow(v1, 2);
            normB += Math.pow(v2, 2);
        }

        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static Map<String, Integer> getTermFrequencyMap(String text) {
        Map<String, Integer> map = new HashMap<>();
        for (String word : text.split("\\s+")) {
            map.put(word, map.getOrDefault(word, 0) + 1);
        }
        return map;
    }

    private static String cleanText(String input) {
        return input.toLowerCase().trim().replaceAll("[^a-z0-9 ]", "");
    }

    private static void saveMemory() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(MEMORY_FILE))) {
            for (Map.Entry<String, String> entry : knowledgeMap.entrySet()) {
                writer.write(entry.getKey() + "|||" + entry.getValue().replace("\n", "\\n"));
                writer.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadMemory() {
        Path path = Paths.get(MEMORY_FILE);
        if (!Files.exists(path)) return;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|\\|\\|");
                if (parts.length == 2) knowledgeMap.put(parts[0], parts[1].replace("\\n", "\n"));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}