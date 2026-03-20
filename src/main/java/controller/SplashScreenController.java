package controller;
import javafx.scene.text.Font;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.ConfirmationDialogs;
import util.SceneNavigator;

public class SplashScreenController {

    @FXML
    private Button startButton;

    @FXML
    private Button exitButton;

    @FXML
    private StackPane rootPane;

    private Stage stage;

    @FXML
    public void initialize() {
        Font.loadFont(
        getClass().getResourceAsStream("/fonts/Jersey10-Regular.ttf"),20);

        backgroundImage();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void backgroundImage(){
        ImageView bg = new ImageView(new Image(getClass().getResource("/images/Cataclysmic chaos strikes the city.png").toExternalForm()));
        bg.setPreserveRatio(false);
        bg.setSmooth(true);
        bg.fitWidthProperty().bind(rootPane.widthProperty());
        bg.fitHeightProperty().bind(rootPane.heightProperty());
        bg.setMouseTransparent(true);

        rootPane.getChildren().add(0, bg);
    }

    @FXML
    private void handleStart() {
        if (!ConfirmationDialogs.confirm("Start Game", "Do you want to start a new session?")) {
            return;
        }
        clickAnimation(startButton, () -> {
            SceneNavigator.showMainMenu(stage);
        });
    }

    @FXML
    private void handleExit() {
        if (!ConfirmationDialogs.confirmQuit()) {
            return;
        }
        clickAnimation(exitButton, () -> {
            if (stage != null) {
                stage.close();
            } else {
                System.exit(0);
            }
        });
    }

    private void clickAnimation(Button button, Runnable onComplete) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button.getParent());
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button.getParent());
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        FadeTransition flash = new FadeTransition(Duration.millis(150), button);
        flash.setFromValue(1.0);
        flash.setToValue(0.5);
        flash.setCycleCount(2);
        flash.setAutoReverse(true);

        SequentialTransition sequence = new SequentialTransition(scaleDown, scaleUp);
        ParallelTransition parallel = new ParallelTransition(sequence, flash);

        parallel.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });

        parallel.play();
    }
}
