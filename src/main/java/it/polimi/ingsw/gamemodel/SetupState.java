package it.polimi.ingsw.gamemodel;

<<<<<<< HEAD
public class SetupState {
=======
public class SetupState extends MatchState{

    SetupState(Match match) {
        super(match);

        match.setupDecks();
        match.setupPlayers();
        match.setupBoards();

        this.transition();
    }

    @Override
    public void transition() {
        MatchState nextState = new NextTurnState(match);
        match.setState(nextState);
    }
>>>>>>> 1-match-states-player-first-implementation
}
