package it.polimi.ingsw.gamemodel;

import it.polimi.ingsw.utils.Pair;

/**
 * 
 */
public interface MatchObserver {
    void matchStarted();

    void someoneJoined(Player someone);

    void someoneQuit(Player someone);

    void someoneDrewInitialCard(Player someone, InitialCard card);

    void someoneSetInitialSide(Player someone, Side side);

    void someoneDrewSecretObjective(Player someone, Pair<Objective, Objective> objectives);

    void someoneChoseSecretObjective(Player someone, Objective objective);

    void someonePlayedCard(Player someone, Pair<Integer, Integer> coords, PlayableCard card, Side side);

    void someoneDrewCard(Player someone, DrawSource source, PlayableCard card, PlayableCard replacementCard);

    void someoneSentBroadcastText(Player someone, String text);

    void someoneSentPrivateText(Player someone, Player recipient, String text);

    /**
     * Notifies that the match has just finished.
     */
    void matchFinished();

}
