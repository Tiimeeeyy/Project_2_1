package ui_controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirstScreenController {

    private static final Logger logger = Logger.getLogger(FirstScreenController.class.getName());
    @FXML
    private Button startGameButton;

    @FXML
    private void handleStartGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/design/second_screen.fxml"));
            Parent secondScreenParent = loader.load();

            Stage current = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(secondScreenParent);
            current.setScene(scene);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load second screen!", e);
        }
    }

}