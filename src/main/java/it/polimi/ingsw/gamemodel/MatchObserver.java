package it.polimi.ingsw.gamemodel;
import it.polimi.ingsw.utils.*;

public interface MatchObserver {

    void matchStarted();

    void someoneDrewInitialCard(Player someone, InitialCard card);

    void someoneSetInitialSide(Player someone, Side side);

    void someoneDrewSecretObjective(Player p, Pair<Objective, Objective> objectives);

    void someoneChoseSecretObjective(Player p, Objective objective);

    void someonePlayedCard(Player p, Pair<Integer, Integer> coords, PlayableCard card, Side side);

    void someoneDrewCard(Player p, DrawSource source, Card card, Card replacementCard);

    void matchFinished();

}
