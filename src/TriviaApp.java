// src/TriviaApp.java
import javax.swing.*;

/**
 * Launcher for the Trivia Module.
 * Delegates UI creation to the modern PetTrivia class.
 */
public class TriviaApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Use the modern styling from PetTrivia
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}

            new PetTrivia().setVisible(true);
        });
    }
}