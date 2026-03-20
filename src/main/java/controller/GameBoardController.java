package controller;

import app.Main;
import javafx.animation.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;
import model.*;
import util.*;
import java.io.*;
import java.util.*;

public class GameBoardController {
    private static final int BOARD_SIZE = 10;
    private static final int CELL_SIZE = 60;

    @FXML
    private BorderPane root;
    @FXML
    private GridPane boardGrid;
    @FXML
    private Label statusLabel;
    @FXML
    private StackPane boardWrapper;

    private TextFlow statusFlow;
    private Text statusPrefix;
    private Text statusPlayerName;

    private final StackPane[][] cells = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private final Map<Player, Circle> tokens = new HashMap<>();

    private VBox leaderboardBox;
    private Button rollButton;

    private GameController gameController;
    private SaveLoadController saveLoadController;
    private Stage stage;
    private Integer activeGameId;
    private boolean winScreenShown;

    @FXML
    private VBox diceContainer;
    @FXML
    private ImageView diceImageView;
    @FXML
    private Button rollDiceButton;

    private boolean animationRunning = false;
    private int currentDiceRoll = 0;
    private Image[] diceImages;

    public void initializeGame(GameController gameController, SaveLoadController saveLoadController, Stage stage, Integer activeGameId) {
        this.gameController = gameController;
        this.saveLoadController = saveLoadController;
        this.stage = stage;
        this.activeGameId = activeGameId;

        loadFont();
        setupBackground();
        addOverlay();
        setupStatusFlow();
        hideTopBar();

        buildBoard();
        buildPanels();
        initTokens(gameController.getGameState().getPlayers());
        loadDiceImages();
        updateStatus();
        updateLeaderboard();

        gameController.getDisasterController().setHumanAnswerProvider(this::askQuestionWithDialog);
        gameController.setClueProvider(this::showClueDialog);
        setUiDisabled(false);
    }

    private void loadFont() {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Jersey10-Regular.ttf"), 14);
    }

    private void setupBackground() {
        root.setStyle("""
                -fx-background-image: url('/images/gameBG.png');
                -fx-background-size: cover;
                -fx-background-position: center;
                -fx-background-repeat: no-repeat;
                """);
    }

    private void addOverlay() {
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setMouseTransparent(true);
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(overlay);
    }

    private void setupStatusFlow() {
        statusPrefix = new Text("Current player: ");
        statusPlayerName = new Text("");
        statusPrefix.setStyle("-fx-fill: white; -fx-font-size: 28px; -fx-font-family: 'Jersey 10';");
        statusPlayerName.setStyle("-fx-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Jersey 10';");
        statusFlow = new TextFlow(statusPrefix, statusPlayerName);
        statusFlow.setTextAlignment(TextAlignment.CENTER);
    }

    private void hideTopBar() {
        Node top = root.getTop();
        if (top != null) {
            top.setVisible(false);
            top.setManaged(false);
        }
    }

    private void buildPanels() {
        VBox leftSection = buildLeftSection();
        VBox centreColumn = buildCenterSection();
        VBox rightSection = buildRightSection();

        root.setTop(null);
        root.setCenter(null);
        root.setLeft(null);
        root.setRight(null);

        HBox mainRow = new HBox(leftSection, centreColumn, rightSection);
        mainRow.setAlignment(Pos.CENTER);
        root.setCenter(mainRow);
    }

    private VBox buildLeftSection() {
        diceImageView = createDiceImageView();
        StackPane diceFrame = createDiceFrame(diceImageView);
        rollDiceButton = createRollButton();
        Button saveButton = createSaveButton();

        VBox leftSection = new VBox(20, diceFrame, rollDiceButton, saveButton);
        leftSection.setAlignment(Pos.CENTER);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        return leftSection;
    }

    private VBox buildCenterSection() {
        VBox centreColumn = new VBox(12, statusFlow, boardWrapper);
        centreColumn.setAlignment(Pos.CENTER);
        return centreColumn;
    }

    private VBox buildRightSection() {
        Label title = createLeaderboardTitle();

        leaderboardBox = new VBox(8);
        leaderboardBox.setAlignment(Pos.TOP_LEFT);
        leaderboardBox.getChildren().add(title);
        leaderboardBox.setMaxWidth(250);

        StackPane rightWrapper = new StackPane(leaderboardBox);
        StackPane.setAlignment(leaderboardBox, Pos.CENTER);

        VBox rightSection = new VBox(rightWrapper);
        rightSection.setAlignment(Pos.CENTER);
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        return rightSection;
    }

    private ImageView createDiceImageView() {
        ImageView view = new ImageView();
        view.setFitWidth(100);
        view.setFitHeight(100);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }

    private StackPane createDiceFrame(ImageView view) {
        StackPane frame = new StackPane(view);
        frame.setStyle("""
                -fx-background-color: rgba(255,255,255,0.15);
                -fx-background-radius: 8;
                -fx-padding: 15;
                """);
        return frame;
    }

    private Button createRollButton() {
        Button button = new Button("Roll");
        button.setPrefWidth(135);
        button.setPrefHeight(45);
        button.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b);
                -fx-text-fill: white;
                -fx-font-size: 22px;
                -fx-font-weight: bold;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                -fx-border-width: 0;
                """);
        button.setOnAction(e -> handleRoll());
        return button;
    }

    private Button createSaveButton() {
        Button button = new Button("Save");
        button.setPrefWidth(135);
        button.setPrefHeight(40);
        button.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                -fx-border-width: 0;
                """);
        button.setOnAction(e -> handleSave());
        return button;
    }

    private Label createLeaderboardTitle() {
        Label title = new Label("LEADERBOARD");
        title.setStyle("""
                -fx-font-size: 32px;
                -fx-font-weight: bold;
                -fx-font-family: 'Jersey 10';
                -fx-text-fill: white;
                -fx-padding: 0 0 12 0;
                """);
        return title;
    }

    private void updateLeaderboard() {
        leaderboardBox.getChildren().subList(1, leaderboardBox.getChildren().size()).clear();

        List<Player> ranked = new ArrayList<>(gameController.getGameState().getPlayers());
        ranked.sort(Comparator.comparingInt(Player::getPosition).reversed());

        String[] ranks = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th"};

        for (int i = 0; i < ranked.size(); i++) {
            Player player = ranked.get(i);
            String prefix = i < ranks.length ? ranks[i] : (i + 1) + "th";

            Color tokenColor = getTokenColor(player);
            String hex = colorToHex(tokenColor);

            Label entry = new Label(prefix + "  " + player.getName() + "  [" + player.getPosition() + "]");
            entry.setStyle("-fx-font-size: 22px; -fx-text-fill: " + hex + "; -fx-font-weight: bold; -fx-font-family: 'Jersey 10';");
            entry.setMaxWidth(Double.MAX_VALUE);
            entry.setAlignment(Pos.CENTER_LEFT);

            leaderboardBox.getChildren().add(entry);
        }
    }

    private Color getTokenColor(Player player) {
        Circle token = tokens.get(player);
        if (token != null && token.getFill() instanceof Color) {
            return (Color) token.getFill();
        }
        return Color.WHITE;
    }

    private String colorToHex(Color color) {
        if (color == null) {
            return "#FFFFFF";
        }
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void updateStatus() {
        GameState state = gameController.getGameState();
        if (state.isFinished()) {
            showWinnerStatus();
        } else {
            showCurrentPlayerStatus(state.getCurrentPlayer());
        }
    }

    private void showWinnerStatus() {
        Player winner = gameController.getWinner();
        if (winner == null) {
            setStatusText("Game finished.", "", Color.WHITE, true);
            return;
        }
        Color tokenColor = getTokenColor(winner);
        setStatusText("Winner: ", winner.getName(), tokenColor, true);
    }

    private void showCurrentPlayerStatus(Player current) {
        Color tokenColor = getTokenColor(current);
        setStatusText("Current player: ", current.getName(), tokenColor, false);
    }

    private void setStatusText(String prefix, String name, Color nameColor, boolean boldPrefix) {
        statusPrefix.setText(prefix);
        if (boldPrefix) {
            statusPrefix.setStyle("-fx-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Jersey 10';");
        } else {
            statusPrefix.setStyle("-fx-fill: white; -fx-font-size: 28px; -fx-font-family: 'Jersey 10';");
        }

        statusPlayerName.setText(name);
        String hex = colorToHex(nameColor);
        statusPlayerName.setStyle("-fx-fill: " + hex + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Jersey 10';");
    }

    @FXML
    private void handleRoll() {
        if (gameController == null || animationRunning) {
            return;
        }

        setButtonEnabled(false);
        animationRunning = true;

        currentDiceRoll = util.Dice.roll();

        animateDiceRoll(currentDiceRoll, () -> {
            runTurnSequence();
            animationRunning = false;
            setButtonEnabled(true);
        });
    }

    @FXML
    private void handleSave() {
        if (gameController == null) {
            return;
        }
        if (!ConfirmationDialogs.confirm("Save Game", "Do you want to save this game?")) {
            return;
        }
        try {
            if (activeGameId == null) {
                saveNewGame();
            } else {
                updateExistingGame();
            }
        } catch (Exception ex) {
            showStatusMessage("Save failed: " + ex.getMessage());
        }
    }

    private SaveLoadController getPersistence() {
        if (saveLoadController == null) {
            saveLoadController = new SaveLoadController(DatabaseController.fromEnv());
        }
        return saveLoadController;
    }

    private void saveNewGame() throws java.sql.SQLException {
        SaveLoadController persistence = getPersistence();
        activeGameId = persistence.saveGame(gameController.getGameState(), gameController.getWinner());
        gameController.attachPersistence(persistence, activeGameId);
        showStatusMessage("Saved game with id: " + activeGameId);
    }

    private void updateExistingGame() throws java.sql.SQLException {
        SaveLoadController persistence = getPersistence();
        persistence.updateGame(activeGameId, gameController.getGameState(), gameController.getWinner());
        showStatusMessage("Game saved.");
    }

    private void showStatusMessage(String message) {
        statusPrefix.setText(message);
        statusPlayerName.setText("");
    }

    private void buildBoard() {
        boardGrid.getChildren().clear();

        double boardPx = BOARD_SIZE * CELL_SIZE;
        configureBoardGrid(boardPx);
        addBoardCells();
        applyBoardBackground(boardPx);
    }

    private void configureBoardGrid(double boardPx) {
        boardGrid.setMinSize(boardPx, boardPx);
        boardGrid.setMaxSize(boardPx, boardPx);
        boardGrid.setPrefSize(boardPx, boardPx);
    }

    private void addBoardCells() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-background-color: transparent;");

                int position = positionForRowCol(row, col);
                Label label = new Label(String.valueOf(position));
                label.setStyle("-fx-font-size: 10px; -fx-text-fill: transparent;");
                StackPane.setAlignment(label, Pos.TOP_LEFT);
                cell.getChildren().add(label);

                cells[row][col] = cell;
                boardGrid.add(cell, col, row);
            }
        }
    }

    private void applyBoardBackground(double boardPx) {
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(getClass().getResource("/images/Board03.png").toExternalForm()),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(boardPx, boardPx, false, false, false, false)
        );
        boardGrid.setBackground(new Background(backgroundImage));
    }

    private void initTokens(List<Player> players) {
        Color[] fallbackPalette = new Color[]{
                Color.DODGERBLUE,
                Color.CRIMSON,
                Color.DARKGREEN,
                Color.GOLDENROD,
                Color.DARKORANGE,
                Color.MEDIUMPURPLE,
                Color.DARKCYAN
        };
        int index = 0;
        for (Player player : players) {
            Color c = player.getColor() != null ? player.getColor() : fallbackPalette[index % fallbackPalette.length];
            Circle token = new Circle(10, c);
            tokens.put(player, token);
            index++;
        }
        refreshTokens();
    }

    private void refreshTokens() {
        for (StackPane[] row : cells) {
            for (StackPane cell : row) {
                cell.getChildren().removeIf(node -> node instanceof Circle);
            }
        }

        for (Map.Entry<Player, Circle> entry : tokens.entrySet()) {
            Player player = entry.getKey();
            Circle token = entry.getValue();
            StackPane cell = cellForPosition(player.getPosition() == 0 ? 1 : player.getPosition());
            token.setTranslateX(0);
            token.setTranslateY(0);
            cell.getChildren().add(token);
        }
    }

    private void showClueDialog(String clue) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/ClueDialog.fxml"));
            Parent dialogRoot = loader.load();
            ClueDialogController dialogController = loader.getController();
            dialogController.setClue(clue);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(stage);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(clue.startsWith("Bad news") ? "Bad News..." : "Good News!");
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setResizable(false);

            root.setDisable(true);
            dialogStage.showAndWait();
            root.setDisable(false);
        } catch (IOException ex) {
            statusPrefix.setText("Clue error: " + ex.getMessage());
            statusPlayerName.setText("");
            root.setDisable(false);
        }
    }

    private int askQuestionWithDialog(DisasterQuestion question) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/DisasterDialog.fxml"));
            Parent dialogRoot = loader.load();
            DisasterDialogController dialogController = loader.getController();
            dialogController.setQuestion(question);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(stage);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Disaster Quiz");
            dialogStage.setScene(new Scene(dialogRoot));

            root.setDisable(true);
            dialogStage.showAndWait();
            root.setDisable(false);

            return dialogController.getSelectedIndex();
        } catch (IOException ex) {
            statusPrefix.setText("Quiz error: " + ex.getMessage());
            statusPlayerName.setText("");
            root.setDisable(false);
            return 0;
        }
    }

    private void runTurnSequence() {
        if (gameController.getGameState().isFinished()) {
            showWinScreen();
            return;
        }

        Player currentPlayer = gameController.getGameState().getCurrentPlayer();
        int oldPosition = currentPlayer.getPosition();

        Platform.runLater(() -> playTurn(currentPlayer, oldPosition));
    }

    private void playTurn(Player currentPlayer, int oldPosition) {
        gameController.playTurnWithRoll(currentDiceRoll);
        int newPosition = currentPlayer.getPosition();
        animatePlayerMovement(currentPlayer, oldPosition, newPosition, this::finishTurnAfterMove);
    }

    private void finishTurnAfterMove() {
        updateStatus();
        updateLeaderboard();

        if (gameController.getGameState().isFinished()) {
            showWinScreen();
            return;
        }

        setUiDisabled(false);
        setButtonEnabled(true);
    }

    private void setUiDisabled(boolean disabled) {
        if (root != null) {
            root.setDisable(disabled);
        }
    }

    private void showWinScreen() {
        if (winScreenShown) return;
        winScreenShown = true;
        setUiDisabled(false);
        SceneNavigator.showWinScreen(stage, gameController.getWinner());
    }

    private void loadDiceImages() {
        diceImages = new Image[6];
        try {
            for (int i = 0; i < 6; i++) {
                diceImages[i] = new Image(
                        getClass().getResourceAsStream("/images/dice-" + (i + 1) + ".png")
                );
            }
            if (diceImageView != null && diceImages[0] != null) {
                diceImageView.setImage(diceImages[0]);
            }
        } catch (Exception e) {
            System.err.println("Failed to load dice images: " + e.getMessage());
        }
    }

    private void animateDiceRoll(int finalValue, Runnable onComplete) {
        if (diceImageView == null || diceImages == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        Timeline flickerTimeline = new Timeline();
        int flickerCount = 15;
        int flickerDuration = 60;

        for (int i = 0; i < flickerCount; i++) {
            final int randomFace = (int) (Math.random() * 6);
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(i * flickerDuration),
                    e -> {
                        if (diceImages[randomFace] != null) {
                            diceImageView.setImage(diceImages[randomFace]);
                        }
                    }
            );
            flickerTimeline.getKeyFrames().add(keyFrame);
        }

        KeyFrame finalFrame = new KeyFrame(
                Duration.millis(flickerCount * flickerDuration),
                e -> {
                    if (diceImages[finalValue - 1] != null) {
                        diceImageView.setImage(diceImages[finalValue - 1]);
                    }
                }
        );
        flickerTimeline.getKeyFrames().add(finalFrame);

        RotateTransition rotate = new RotateTransition(
                Duration.millis(flickerCount * flickerDuration), diceImageView);
        rotate.setByAngle(360 * 3);
        rotate.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scale = new ScaleTransition(
                Duration.millis(flickerCount * flickerDuration), diceImageView);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);

        ParallelTransition parallel = new ParallelTransition(flickerTimeline, rotate, scale);
        parallel.setOnFinished(e -> {
            if (onComplete != null) onComplete.run();
        });
        parallel.play();
    }

    private void setButtonEnabled(boolean enabled) {
        if (rollDiceButton != null) rollDiceButton.setDisable(!enabled);
        if (rollButton != null)     rollButton.setDisable(!enabled);
    }

    private void animatePlayerMovement(Player player, int startPos, int endPos, Runnable onComplete) {
        Circle token = tokens.get(player);
        if (token == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        token.setVisible(true);
        token.setOpacity(1.0);

        if (startPos == endPos) {
            refreshTokens();
            if (onComplete != null) onComplete.run();
            return;
        }

        if (startPos < 1) startPos = 1;
        if (endPos   < 1) endPos   = 1;

        boolean isSpecialTile = Math.abs(endPos - startPos) > 10;

        if (isSpecialTile) {
            StackPane startCell = cellForPosition(startPos);
            StackPane endCell   = cellForPosition(endPos);

            double startX = startCell.localToScene(startCell.getWidth() / 2, startCell.getHeight() / 2).getX();
            double startY = startCell.localToScene(startCell.getWidth() / 2, startCell.getHeight() / 2).getY();
            double endX   = endCell.localToScene(endCell.getWidth() / 2, endCell.getHeight() / 2).getX();
            double endY   = endCell.localToScene(endCell.getWidth() / 2, endCell.getHeight() / 2).getY();

            animateDiagonalPath(token, startX, startY, endX, endY, onComplete);
        } else {
            animateStepByStep(player, startPos, endPos, onComplete);
        }
    }

    private void animateStepByStep(Player player, int startPos, int endPos, Runnable onComplete) {
        Circle token = tokens.get(player);
        if (token == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        StackPane currentCell = cellForPosition(startPos);
        if (token.getParent() != currentCell) {
            currentCell.getChildren().add(token);
        }
        token.setTranslateX(0);
        token.setTranslateY(0);
        token.setVisible(true);
        token.setOpacity(1.0);

        SequentialTransition sequence = new SequentialTransition();
        int step = startPos < endPos ? 1 : -1;

        for (int pos = startPos; pos != endPos; pos += step) {
            final int nextPos = pos + step;
            addStepTransition(sequence, token, pos, nextPos);
        }

        sequence.setOnFinished(e -> {
            token.setTranslateX(0);
            token.setTranslateY(0);
            token.setScaleX(1.0);
            token.setScaleY(1.0);
            refreshTokens();
            if (onComplete != null) onComplete.run();
        });

        sequence.play();
    }

    private void addStepTransition(SequentialTransition sequence, Circle token, int fromPos, int toPos) {
        StackPane fromCell = cellForPosition(fromPos);
        StackPane toCell = cellForPosition(toPos);

        double fromX = fromCell.localToScene(fromCell.getWidth() / 2, fromCell.getHeight() / 2).getX();
        double fromY = fromCell.localToScene(fromCell.getWidth() / 2, fromCell.getHeight() / 2).getY();
        double toX = toCell.localToScene(toCell.getWidth() / 2, toCell.getHeight() / 2).getX();
        double toY = toCell.localToScene(toCell.getWidth() / 2, toCell.getHeight() / 2).getY();

        TranslateTransition move = new TranslateTransition(Duration.millis(150), token);
        move.setByX(toX - fromX);
        move.setByY(toY - fromY);
        move.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition bounce = new ScaleTransition(Duration.millis(75), token);
        bounce.setToX(1.3);
        bounce.setToY(1.3);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);

        PauseTransition reparent = new PauseTransition(Duration.millis(1));
        reparent.setOnFinished(e -> {
            token.setTranslateX(0);
            token.setTranslateY(0);
            StackPane next = cellForPosition(toPos);
            next.getChildren().remove(token);
            next.getChildren().add(token);
        });

        sequence.getChildren().addAll(new ParallelTransition(move, bounce), reparent);
    }

    private void animateDiagonalPath(Circle token, double startX, double startY,
                                     double endX, double endY, Runnable onComplete) {
        token.setVisible(true);
        token.setOpacity(1.0);

        TranslateTransition move = new TranslateTransition(Duration.millis(800), token);
        move.setByX(endX - startX);
        move.setByY(endY - startY);
        move.setInterpolator(Interpolator.EASE_BOTH);

        RotateTransition rotate = new RotateTransition(Duration.millis(800), token);
        rotate.setByAngle(360);

        ScaleTransition scale = new ScaleTransition(Duration.millis(400), token);
        scale.setToX(1.5);
        scale.setToY(1.5);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        ParallelTransition parallel = new ParallelTransition(move, rotate, scale);
        parallel.setOnFinished(e -> {
            token.setTranslateX(0);
            token.setTranslateY(0);
            token.setScaleX(1.0);
            token.setScaleY(1.0);
            token.setRotate(0);
            refreshTokens();
            if (onComplete != null) onComplete.run();
        });

        parallel.play();
    }

    private StackPane cellForPosition(int position) {
        int[] rc = rowColForPosition(position);
        return cells[rc[0]][rc[1]];
    }

    private int[] rowColForPosition(int position) {
        int zeroBased = position - 1;
        int rowFromBottom = zeroBased / BOARD_SIZE;
        int colInRow = zeroBased % BOARD_SIZE;
        boolean leftToRight = rowFromBottom % 2 == 0;
        int row = BOARD_SIZE - 1 - rowFromBottom;
        int col = leftToRight ? colInRow : (BOARD_SIZE - 1 - colInRow);
        return new int[]{row, col};
    }

    private int positionForRowCol(int row, int col) {
        int rowFromBottom = BOARD_SIZE - 1 - row;
        boolean leftToRight = rowFromBottom % 2 == 0;
        int offset = leftToRight ? col : (BOARD_SIZE - 1 - col);
        return rowFromBottom * BOARD_SIZE + offset + 1;
    }
}
