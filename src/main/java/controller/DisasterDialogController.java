package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.DisasterQuestion;

import java.util.List;

public class DisasterDialogController {
    @FXML
    private Label questionLabel;
    @FXML
    private Button optionA;
    @FXML
    private Button optionB;
    @FXML
    private Button optionC;
    @FXML
    private Button optionD;

    private int selectedIndex = 0;

    public void setQuestion(DisasterQuestion question) {
        questionLabel.setText(question.getPrompt());
        List<String> options = question.getOptions();
        configureButton(optionA, options, 0);
        configureButton(optionB, options, 1);
        configureButton(optionC, options, 2);
        configureButton(optionD, options, 3);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @FXML
    private void handleOptionA() {
        select(0);
    }

    @FXML
    private void handleOptionB() {
        select(1);
    }

    @FXML
    private void handleOptionC() {
        select(2);
    }

    @FXML
    private void handleOptionD() {
        select(3);
    }

    private void configureButton(Button button, List<String> options, int index) {
        if (index < options.size()) {
            char letter = (char) ('A' + index);
            button.setText(letter + ". " + options.get(index));
            button.setDisable(false);
            button.setVisible(true);
            button.setManaged(true);
        } else {
            button.setDisable(true);
            button.setVisible(false);
            button.setManaged(false);
        }
    }

    private void select(int index) {
        selectedIndex = index;
        Stage stage = (Stage) optionA.getScene().getWindow();
        stage.close();
    }
}
