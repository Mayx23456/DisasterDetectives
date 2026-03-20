package util;

import app.Main;
import controller.GameBoardController;
import controller.HistoryController;
import controller.MainMenuController;
import controller.WinScreenController;
import controller.GameController;
import controller.SaveLoadController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Player;

import java.io.IOException;

public final class SceneNavigator {
    private SceneNavigator() {
    }

    public static void showMainMenu(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainMenu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setStage(stage);
            stage.setTitle("Disaster Game");
            stage.setScene(new Scene(root, 1000, 700));
            ConfirmationDialogs.installCloseConfirmation(stage);
            stage.show();
        } catch (IOException ex) {
            System.err.println("Failed to load main menu: " + ex.getMessage());
        }
    }

    public static void showGameBoard(Stage stage, GameController gameController, SaveLoadController saveLoadController, Integer gameId) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/GameBoard.fxml"));
            Parent root = loader.load();
            GameBoardController controller = loader.getController();
            controller.initializeGame(gameController, saveLoadController, stage, gameId);
            Stage targetStage = stage != null ? stage : new Stage();
            targetStage.setTitle("Disaster Game");
            targetStage.setScene(new Scene(root, 1000, 720));
            ConfirmationDialogs.installCloseConfirmation(targetStage);
            targetStage.show();
        } catch (IOException ex) {
            System.err.println("Failed to load game board: " + ex.getMessage());
        }
    }

    public static void showWinScreen(Stage stage, Player winner) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/WinScreen.fxml"));
            Parent root = loader.load();
            WinScreenController controller = loader.getController();
            controller.setStage(stage);
            controller.setWinner(winner);
            stage.setTitle("Disaster Game");
            stage.setScene(new Scene(root, 1000, 700));
            ConfirmationDialogs.installCloseConfirmation(stage);
            stage.show();
        } catch (IOException ex) {
            System.err.println("Failed to load win screen: " + ex.getMessage());
        }
    }

    public static void showHistory(Stage stage, Integer gameId) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/History.fxml"));
            Parent root = loader.load();
            HistoryController controller = loader.getController();
            controller.initialize(gameId, stage);
            Stage targetStage = stage != null ? stage : new Stage();
            targetStage.setTitle("Disaster Game - History");
            targetStage.setScene(new Scene(root, 1000, 700));
            ConfirmationDialogs.installCloseConfirmation(targetStage);
            targetStage.show();
        } catch (IOException ex) {
            System.err.println("Failed to load history: " + ex.getMessage());
        }
    }
}
