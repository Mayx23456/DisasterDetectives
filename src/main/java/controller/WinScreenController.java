package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Player;
import util.ConfirmationDialogs;
import util.SceneNavigator;

public class WinScreenController {
    @FXML
    private Label winnerLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setWinner(Player winner) {
        if (winner == null) {
            winnerLabel.setText("Winner: TBD");
        } else {
            winnerLabel.setText("Winner: " + winner.getName());
        }
    }

    @FXML
    private void handleMainMenu() {
        if (!ConfirmationDialogs.confirm("Main Menu", "Do you want to go back to the main menu?")) {
            return;
        }
        if (stage != null) {
            SceneNavigator.showMainMenu(stage);
        }
    }

    @FXML
    private void handleExit() {
        if (!ConfirmationDialogs.confirmQuit()) {
            return;
        }
        if (stage != null) {
            stage.close();
        }
    }
}
