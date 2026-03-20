package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import model.Board;
import model.GameState;
import model.Player;
import util.ConfirmationDialogs;
import util.SceneNavigator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainMenuController {
    @FXML
    private StackPane rootPane;
    @FXML
    private ImageView backgroundView;
    @FXML
    private Label countLabel;
    @FXML
    private VBox playerListBox;
    @FXML
    private Label statusLabel;

    private Stage stage;
    private int playerCount = 2;
    private final List<PlayerRow> playerRows = new ArrayList<>();
    private final Image humanSprite = loadImage("/images/human_sprite.png");
    private final Image colourWheelImage = loadImage("/images/colour_wheel.png");

    private static final Color[] PALETTE = {
            Color.DODGERBLUE,
            Color.CRIMSON,
            Color.DARKGREEN,
            Color.GOLDENROD,
            Color.DARKORANGE,
            Color.MEDIUMPURPLE,
            Color.DARKCYAN,
            Color.HOTPINK,
            Color.LIMEGREEN,
            Color.SIENNA
    };

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        backgroundView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundView.fitHeightProperty().bind(rootPane.heightProperty());
        countLabel.setText(String.valueOf(playerCount));
        rebuildPlayerRows(playerCount);
        statusLabel.setText("");
    }

    @FXML
    private void handleDecrement() {
        if (playerCount > 1) {
            playerCount--;
            countLabel.setText(String.valueOf(playerCount));
            rebuildPlayerRows(playerCount);
        }
    }

    @FXML
    private void handleIncrement() {
        if (playerCount < GameState.MAX_PLAYERS) {
            playerCount++;
            countLabel.setText(String.valueOf(playerCount));
            rebuildPlayerRows(playerCount);
        }
    }

    @FXML
    private void handleNewGame() {
        if (!ConfirmationDialogs.confirm("New Game", "Do you want to start a new game?")) {
            return;
        }
        List<Player> players = buildPlayers();
        if (players.isEmpty()) {
            return;
        }

        GameState gameState = new GameState(players, new Board());
        GameController gameController = new GameController(gameState, new TurnController(), new DisasterController());
        SaveLoadController saveLoadController = null;
        Integer gameId = null;
        try {
            saveLoadController = new SaveLoadController(DatabaseController.fromEnv());
            gameId = saveLoadController.saveGame(gameState, null);
            gameController.attachPersistence(saveLoadController, gameId);
        } catch (Exception ex) {
            statusLabel.setText("Persistence unavailable: " + ex.getMessage());
        }
        SceneNavigator.showGameBoard(resolveStage(), gameController, saveLoadController, gameId);
    }

    @FXML
    private void handleLoadGame() {
        if (!ConfirmationDialogs.confirm("Load Game", "Do you want to load a saved game?")) {
            return;
        }
        try {
            SaveLoadController saveLoadController = new SaveLoadController(DatabaseController.fromEnv());
            Integer gameId = saveLoadController.findLatestGameId();
            if (gameId == null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Load Game");
                dialog.setHeaderText("Enter the saved game id");
                Optional<String> input = dialog.showAndWait();
                if (input.isEmpty()) {
                    return;
                }
                gameId = Integer.parseInt(input.get().trim());
            }
            GameState loadedState = saveLoadController.loadGame(gameId);
            GameController gameController = new GameController(loadedState, new TurnController(), new DisasterController());
            gameController.attachPersistence(saveLoadController, gameId);
            SceneNavigator.showGameBoard(resolveStage(), gameController, saveLoadController, gameId);
        } catch (Exception ex) {
            statusLabel.setText("Failed to load: " + ex.getMessage());
        }
    }

    @FXML
    private void handleHistory() {
        if (!ConfirmationDialogs.confirm("View History", "Do you want to view game history?")) {
            return;
        }
        try {
            SaveLoadController saveLoadController = new SaveLoadController(DatabaseController.fromEnv());
            Integer gameId = saveLoadController.findLatestGameId();
            if (gameId == null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("View History");
                dialog.setHeaderText("Enter a game id to view history");
                Optional<String> input = dialog.showAndWait();
                if (input.isEmpty()) {
                    return;
                }
                gameId = Integer.parseInt(input.get().trim());
            }
            SceneNavigator.showHistory(resolveStage(), gameId);
        } catch (Exception ex) {
            statusLabel.setText("History unavailable: " + ex.getMessage());
        }
    }

    @FXML
    private void handleSettings() {
        if (!ConfirmationDialogs.confirm("Settings", "Do you want to open settings?")) {
            return;
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Settings");
        info.setHeaderText("Settings");
        info.setContentText("No additional settings available.");
        Stage owner = resolveStage();
        if (owner != null) {
            info.initOwner(owner);
        }
        info.showAndWait();
    }

    private List<Player> buildPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerRows.size(); i++) {
            PlayerRow row = playerRows.get(i);
            String name = row.nameField.getText() == null ? "" : row.nameField.getText().trim();
            if (name.isEmpty()) {
                statusLabel.setText("Player " + (i + 1) + " name is required.");
                return List.of();
            }

            Player player = new Player(name);
            player.setColor(row.selectedColor);
            players.add(player);
        }
        return players;
    }

    private Stage resolveStage() {
        if (stage != null) {
            return stage;
        }
        if (!playerRows.isEmpty() && playerRows.get(0).nameField.getScene() != null) {
            return (Stage) playerRows.get(0).nameField.getScene().getWindow();
        }
        return null;
    }

    private void rebuildPlayerRows(int count) {
        List<String> existingNames = new ArrayList<>();
        List<Color> existingColors = new ArrayList<>();
        for (PlayerRow row : playerRows) {
            existingNames.add(row.nameField.getText());
            existingColors.add(row.selectedColor);
        }

        playerRows.clear();
        playerListBox.getChildren().clear();

        for (int i = 0; i < count; i++) {
            String name = i < existingNames.size() && existingNames.get(i) != null
                    ? existingNames.get(i)
                    : "Player " + (i + 1);
            Color color = i < existingColors.size() && existingColors.get(i) != null
                    ? existingColors.get(i)
                    : PALETTE[i % PALETTE.length];

            Label label = new Label("P" + (i + 1));
            label.getStyleClass().add("player-label");

            TextField field = new TextField(name);
            field.setPrefWidth(180);
            field.setPromptText("Player " + (i + 1));
            field.getStyleClass().add("player-name-field");

            ImageView spriteView = new ImageView(humanSprite);
            spriteView.setFitHeight(32);
            spriteView.setPreserveRatio(true);

            Circle colorIndicator = new Circle(9, color);
            colorIndicator.setStroke(Color.gray(0.6));
            colorIndicator.setStrokeWidth(1.5);

            ImageView wheelView = new ImageView(colourWheelImage);
            wheelView.setFitWidth(26);
            wheelView.setFitHeight(26);
            wheelView.setPreserveRatio(true);
            Circle clip = new Circle(13);
            clip.setCenterX(13);
            clip.setCenterY(13);
            wheelView.setClip(clip);

            Button colorButton = new Button();
            colorButton.setGraphic(wheelView);
            colorButton.setStyle("-fx-background-color: transparent; -fx-padding: 2; -fx-cursor: hand;");

            PlayerRow row = new PlayerRow(field, color);

            colorButton.setOnAction(e -> {
                Popup popup = new Popup();
                popup.setAutoHide(true);

                TilePane grid = new TilePane(6, 6);
                grid.setPrefColumns(5);
                grid.setPadding(new Insets(8));
                grid.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #555555; -fx-border-radius: 6; -fx-background-radius: 6;");

                for (Color option : PALETTE) {
                    Circle swatch = new Circle(13, option);
                    swatch.setStroke(Color.gray(0.4));
                    swatch.setStrokeWidth(1.5);
                    swatch.setStyle("-fx-cursor: hand;");
                    swatch.setOnMouseClicked(ev -> {
                        row.selectedColor = option;
                        colorIndicator.setFill(option);
                        popup.hide();
                    });
                    grid.getChildren().add(swatch);
                }

                popup.getContent().add(grid);
                popup.show(colorButton,
                        colorButton.localToScreen(colorButton.getBoundsInLocal()).getMinX(),
                        colorButton.localToScreen(colorButton.getBoundsInLocal()).getMaxY() + 4);
            });

            HBox rowBox = new HBox(8, label, field, spriteView, colorButton, colorIndicator);
            rowBox.setAlignment(Pos.CENTER);

            playerListBox.getChildren().add(rowBox);
            playerRows.add(row);
        }
    }

    private Image loadImage(String resourcePath) {
        InputStream input = getClass().getResourceAsStream(resourcePath);
        if (input == null) {
            statusLabelTextSafe("Missing image: " + resourcePath);
            return null;
        }
        return new Image(input);
    }

    private void statusLabelTextSafe(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        } else {
            System.err.println(message);
        }
    }

    private static final class PlayerRow {
        private final TextField nameField;
        private Color selectedColor;

        private PlayerRow(TextField nameField, Color selectedColor) {
            this.nameField = nameField;
            this.selectedColor = selectedColor;
        }
    }
}
