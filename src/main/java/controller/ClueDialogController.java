package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ClueDialogController {

    @FXML
    private Label clueLabel;

    public void setClue(String clue) {
        String display = clue
                .replace("Bad news! ", "")
                .replace("Good news! ", "");
        clueLabel.setText(display);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) clueLabel.getScene().getWindow();
        stage.close();
    }
}
