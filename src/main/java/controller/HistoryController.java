package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import util.ConfirmationDialogs;
import util.SceneNavigator;

import java.util.List;

public class HistoryController {
    @FXML
    private Label titleLabel;
    @FXML
    private ListView<String> historyList;

    private Integer gameId;
    private Stage stage;

    public void initialize(Integer gameId, Stage stage) {
        this.gameId = gameId;
        this.stage = stage;
        loadHistory();
    }

    @FXML
    private void handleBack() {
        if (!ConfirmationDialogs.confirm("Back to Menu", "Do you want to return to the main menu?")) {
            return;
        }
        if (stage != null) {
            SceneNavigator.showMainMenu(stage);
        }
    }

    private void loadHistory() {
        if (gameId == null) {
            titleLabel.setText("Game History");
            historyList.getItems().setAll(List.of("No game id provided."));
            return;
        }
        titleLabel.setText("Game History (Game " + gameId + ")");
        try {
            SaveLoadController saveLoadController = new SaveLoadController(DatabaseController.fromEnv());
            List<String> history = saveLoadController.loadTurnHistory(gameId);
            if (history.isEmpty()) {
                historyList.getItems().setAll(List.of("No turns recorded yet."));
            } else {
                historyList.getItems().setAll(history);
            }
        } catch (Exception ex) {
            historyList.getItems().setAll(List.of("Failed to load history: " + ex.getMessage()));
        }
    }
}
