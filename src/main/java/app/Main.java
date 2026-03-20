package app;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import java.io.IOException;

import controller.SplashScreenController;
import util.ConfirmationDialogs;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Font.loadFont(Main.class.getResourceAsStream("/fonts/Jersey10-Regular.ttf"), 1);

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/SplashScreen.fxml"));
            Parent root = loader.load();

            SplashScreenController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Disaster Detector");
            ConfirmationDialogs.installCloseConfirmation(stage);
            stage.show();
        }

        catch (IOException e) {
            System.err.println("Failed to load main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
