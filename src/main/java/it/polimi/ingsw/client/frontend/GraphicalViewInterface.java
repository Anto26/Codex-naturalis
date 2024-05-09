package it.polimi.ingsw.client.frontend;

import it.polimi.ingsw.client.network.NetworkView;
import it.polimi.ingsw.gamemodel.PlayableCard;
import it.polimi.ingsw.gamemodel.Side;
import it.polimi.ingsw.utils.Pair;

public interface GraphicalViewInterface {

    /**
     * Sets the network interface to communicate
     *
     * @param networkView the interface to communicate
     */
    void setNetworkInterface(NetworkView networkView);

    /**
     * (Temporary demo method)
     * Shows an error in the GUI
     *
     * @param text explaination of the error in natural language
     */
    void sendError(String text);

    /**
     * (Temporary demo method)
     * Notifies the view that someone played a card
     *
     */
    void someonePlayedCard(String someoneUsername, Pair<Integer, Integer> coords, PlayableCard card, Side side);

    /**
     * (Temporary demo method)
     * Cancel last action by player
     */
    void cancelLastAction();

}
