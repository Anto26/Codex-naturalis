package it.polimi.ingsw.utils;

import it.polimi.ingsw.client.frontend.gui.GraphicalApplication;
import it.polimi.ingsw.gamemodel.*;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class GuiUtil {
    public static String playableCardsPath = "/images/playable_cards";
    public static String objectivesPath = "/images/objectives";
    public static String initialsPath = "/images/initial_cards";

    public static <T>T getFromFXML(String path) throws IOException {
        FXMLLoader loader = GuiUtil.getLoader(path);
        return loader.load();
    }

    /**
     * Get the loader from the specified path
     * @param path file path of fxml
     * @return loader
     */
    public static FXMLLoader getLoader(String path) {
        return new FXMLLoader(GraphicalApplication.class.getResource(path));
    }

    /**
     * Applies the specified CSS to a javafx scene parent
     * @param w The parent to apply the css to
     * @param path Path of the css file
     */
    public static void applyCSS(javafx.scene.Parent w, String path) {
        w.getStylesheets().addAll(GraphicalApplication.class.getResource(path).toExternalForm());
    }

    public static String getImagePath(PlayableCard card, Side side) {
        if (side.equals(Side.FRONT)) {
            return playableCardsPath + "/" + card.getId() + ".png";
        } else {
            return switch (card){
                case GoldCard ignored -> getGoldsBack(card.getReign());
                case ResourceCard ignored -> getResourcesBack(card.getReign());
            };
        }
    }

    public static String getResourcesBack(Symbol symbol) {
        String reign = symbol.toString().toUpperCase();
        return playableCardsPath + "/" + reign + "-resources-back.png";
    }

    public static String getGoldsBack(Symbol symbol) {
        String reign = symbol.toString().toUpperCase();
        return playableCardsPath + "/" + reign + "-golds-back.png";
    }

    public static String getImagePath(InitialCard card, Side side) {
        return initialsPath + "/" + side.toString() + "/" + card.getId() + ".png";
    }

    public static String getImagePath(Objective obj, Side side) {
        if (side.equals(Side.FRONT))
            return objectivesPath + "/" + obj.getID() + ".png";
        else
            return objectivesPath + "/objectives-back.png";
    }

}

