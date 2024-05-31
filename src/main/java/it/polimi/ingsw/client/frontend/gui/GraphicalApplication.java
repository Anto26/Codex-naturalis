package it.polimi.ingsw.client.frontend.gui;

import it.polimi.ingsw.client.frontend.gui.scenes.PlayerTabController;
import it.polimi.ingsw.client.frontend.gui.scenes.SceneController;
import it.polimi.ingsw.gamemodel.PlayableCard;
import it.polimi.ingsw.gamemodel.Side;
import it.polimi.ingsw.utils.GuiUtil;
import it.polimi.ingsw.utils.Pair;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class GraphicalApplication extends Application {
    private GraphicalViewGUI view;
    private Stage primaryStage;
    public static double screenWidth = 1920;
    public static double screenHeight = 1020;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        this.view = new GraphicalViewGUI(primaryStage);

        // Load initial screen
        primaryStage.setTitle("Codex Naturalis");
        // Load FXML layout
        StackPane root = this.loadScene("/fxml/connection.fxml");
        // Add stylesheet
        GuiUtil.applyCSS(root, "/css/style.css");
        // Create the connection scene
        Scene connectionScene = new Scene(root, screenWidth, screenHeight);
        // Show the window

        /*
        // Fullscreen
        primaryStage.setFullScreen(true);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitHint("");
        */

        primaryStage.setScene(connectionScene);
        primaryStage.show();
        root.requestFocus();

    }


    /**
     * Get a node from the specified FXML path and set the values of a SceneController
     * @param path file path of FXML
     * @return The first node
     * @param <T> Type of the node
     * @throws IOException
     */
    private <T>T loadScene(String path) throws IOException {
        FXMLLoader loader = GuiUtil.getLoader(path);
        T result = loader.load();
        SceneController controller = loader.getController();
        controller.setGraphicalView(view);
        controller.setStage(primaryStage);
        return result;
    }
}
