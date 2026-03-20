package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public final class ConfirmationDialogs {
    private ConfirmationDialogs() {
    }

    public static boolean confirm(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner != null) {
            alert.initOwner(owner);
        }
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static boolean confirm(String title, String message) {
        return confirm((Stage) null, title, message);
    }

    public static boolean confirmQuit() {
        return confirm("Confirm Quit", "Do you want to quit?");
    }

    public static void installCloseConfirmation(Stage stage) {
        if (stage == null) {
            return;
        }
        stage.setOnCloseRequest(event -> {
            if (!confirmQuit()) {
                event.consume();
            }
        });
    }
}
