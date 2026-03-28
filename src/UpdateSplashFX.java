import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;

public class UpdateSplashFX {

    private static Stage stage;
    private static final int W = 800;
    private static final int H = 500;

    public static void show(Runnable onComplete) {
        // Init JavaFX if not already started
        try { Platform.startup(() -> {}); } catch (IllegalStateException e) {}

        Platform.runLater(() -> {
            stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(true);

            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: transparent;");

            // 1. Background Image with Blur
            ImageView bgView = new ImageView();
            try {
                // Try loading local background, fallback to web if missing
                String bgPath = "image/background.png";
                File f = new File(bgPath);
                Image bgImg = f.exists() ? new Image(f.toURI().toString()) : new Image("https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=800&q=80");
                bgView.setImage(bgImg);
                bgView.setFitWidth(W);
                bgView.setFitHeight(H);
                bgView.setPreserveRatio(false);

                // Apply heavy blur for that "frosted glass" look in your image
                BoxBlur blur = new BoxBlur(15, 15, 3);
                bgView.setEffect(blur);
            } catch (Exception e) {}

            // Dark overlay to make text pop
            Rectangle overlay = new Rectangle(W, H, Color.rgb(0, 0, 0, 0.2));

            // 2. Central Content Container
            VBox centerBox = new VBox(25); // 25px spacing
            centerBox.setAlignment(Pos.CENTER);

            // -- Logo Circle --
            ImageView logoView = new ImageView();
            try {
                URL url = UpdateSplashFX.class.getResource("/image/Logo.png");
                if (url != null) {
                    Image logo = new Image(url.toExternalForm());
                    logoView.setImage(logo);
                    logoView.setFitWidth(120);
                    logoView.setFitHeight(120);

                    // Circular Clip
                    Circle clip = new Circle(60, 60, 60);
                    logoView.setClip(clip);

                    // White halo/shadow behind logo
                    logoView.setEffect(new DropShadow(20, Color.WHITE));
                }
            } catch (Exception e) {}

            // -- Custom Paw Spinner & Progress Bar Canvas --
            Canvas canvas = new Canvas(300, 150);
            AnimationLoop loop = new AnimationLoop(canvas.getGraphicsContext2D());
            loop.start();

            centerBox.getChildren().addAll(logoView, canvas);

            root.getChildren().addAll(bgView, overlay, centerBox);

            // Clip the entire window to rounded corners
            Rectangle windowClip = new Rectangle(W, H);
            windowClip.setArcWidth(40);
            windowClip.setArcHeight(40);
            root.setClip(windowClip);

            Scene scene = new Scene(root, W, H);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            // Simulate Update Process
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Fake download time
                } catch (InterruptedException e) {}

                Platform.runLater(() -> {
                    loop.stop();
                    stage.close();
                    if (onComplete != null) onComplete.run();
                });
            }).start();
        });
    }

    private static class AnimationLoop extends AnimationTimer {
        private final GraphicsContext gc;
        private double angle = 0;
        private double progress = 0;

        // Gradient for the spinner/bar (Purple to Blue)
        private final Color C1 = Color.web("#8B5CF6"); // Purple
        private final Color C2 = Color.web("#3B82F6"); // Blue

        public AnimationLoop(GraphicsContext gc) {
            this.gc = gc;
        }

        @Override
        public void handle(long now) {
            angle += 3; // Spin speed
            if (progress < 1.0) progress += 0.003; // Progress fill speed

            double w = gc.getCanvas().getWidth();
            double h = gc.getCanvas().getHeight();
            double cx = w / 2;

            gc.clearRect(0, 0, w, h);

            // --- 1. DRAW SPINNER RING (Center) ---
            double r = 40; // Radius
            double cy = 50;

            gc.setLineWidth(6);
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

            // Background track
            gc.setStroke(Color.rgb(255, 255, 255, 0.3));
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

            // Spinning colored arc
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, C1), new Stop(1, C2));
            gc.setStroke(gradient);
            // Draw 2 arcs for the "spinning" look
            gc.strokeArc(cx - r, cy - r, r * 2, r * 2, angle, 100, ArcType.OPEN);
            gc.strokeArc(cx - r, cy - r, r * 2, r * 2, angle + 180, 100, ArcType.OPEN);

            // --- 2. DRAW PAW ICON (Inside Spinner) ---
            // A simple geometric paw
            gc.setFill(Color.WHITE);
            double pawScale = 0.5;
            double px = cx;
            double py = cy + 5;

            // Main pad
            gc.fillOval(px - 15*pawScale, py - 10*pawScale, 30*pawScale, 25*pawScale);
            // Toes
            gc.fillOval(px - 20*pawScale, py - 25*pawScale, 10*pawScale, 10*pawScale);
            gc.fillOval(px - 5*pawScale, py - 35*pawScale, 10*pawScale, 10*pawScale);
            gc.fillOval(px + 10*pawScale, py - 25*pawScale, 10*pawScale, 10*pawScale);

            // --- 3. PROGRESS BAR (Bottom) ---
            double barW = 220;
            double barH = 10;
            double barX = (w - barW) / 2;
            double barY = 110;

            // Track
            gc.setFill(Color.rgb(255, 255, 255, 0.3));
            gc.fillRoundRect(barX, barY, barW, barH, 10, 10);

            // Fill
            gc.setFill(gradient);
            gc.fillRoundRect(barX, barY, barW * progress, barH, 10, 10);

            // --- 4. TEXT ---
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText("Updating your experience...", cx, barY + 30);
        }
    }
}