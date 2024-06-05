package it.polimi.ingsw.client.frontend;

import it.polimi.ingsw.client.network.NetworkView;
import it.polimi.ingsw.gamemodel.*;
import it.polimi.ingsw.utils.AvailableMatch;
import it.polimi.ingsw.utils.LeaderboardEntry;
import it.polimi.ingsw.utils.Pair;
import it.polimi.ingsw.utils.RequestStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GraphicalView {
    protected NetworkView networkView;
    protected Map<String, ClientBoard> clientBoards;
    protected List<String> players; // ordered by turn
    protected String currentPlayer;
    protected Pair<Objective, Objective> visibleObjectives;
    protected Map<DrawSource, PlayableCard> visiblePlayableCards;
    protected Pair<Symbol, Symbol> decksTopReign;
    protected boolean lastTurn = false;
    protected List<AvailableMatch> availableMatches;
    protected String username;
    protected final LastRequest lastRequest;

    public GraphicalView() {
        this.lastRequest = new LastRequest();
        this.lastRequest.setStatus(RequestStatus.PENDING);
    }

    protected void setUsername(String username) {
        this.username = username;
        this.networkView.setUsername(username);
    }

    public boolean isLastTurn() {
        return this.lastTurn;
    }

    public void setLastRequestStatus(RequestStatus status) {
        this.lastRequest.setStatus(status);
    }

    /**
     * Sets the internal state according to the occurance of an error.
     *
     * @param exception The thrown exception
     */
    public void notifyError(Exception exception) {
        this.setLastRequestStatus(RequestStatus.FAILED);
    }

    /**
     * Sets the network interface in order to communicate.
     *
     * @param networkView the interface to communicate
     */
    public void setNetworkInterface(NetworkView networkView) {
        this.networkView = networkView;
    }

    /**
     * Tries to create a match
     *
     * @param matchName The match's name
     */
    public void createMatch(String matchName, Integer maxPlayers) {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.networkView.createMatch(matchName, maxPlayers);
    }

    /**
     * Tries to join a match
     *
     * @param matchName the match's name
     */
    public void joinMatch(String matchName) {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.networkView.joinMatch(matchName);
    }

    public void sendBroadcastText(String text) {
        this.networkView.sendBroadcastText(text);
    }

    public void sendPrivateText(String recipient, String text) {
        this.networkView.sendPrivateText(recipient, text);
    }

    /**
     * Draws an initial card for the player.
     */
    public void drawInitialCard() {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.networkView.drawInitialCard();
    }

    /**
     * Communicates the chosen initial card side.
     *
     * @param side The side on which play the initial card drawn using {@link #drawInitialCard()}
     */
    public void chooseInitialCardSide(Side side) {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.networkView.chooseInitialCardSide(side);
    }

    /**
     * Draws two secret objectives.
     */
    public void drawSecretObjectives() {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.networkView.drawSecretObjectives();
    }

    /**
     * Communicates the chosen secret objective.
     *
     * @param objective The chosen objective
     */
    public void chooseSecretObjective(Objective objective) {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.clientBoards.get(this.username).setSecretObjective(objective);
        this.networkView.chooseSecretObjective(objective);
    }

    /**
     * Plays a card.
     *
     * @param coords The coordinates on which to place the card
     * @param card   The PlayableCard to play
     * @param side   The side on which to play the chosen card
     */
    public void playCard(Pair<Integer, Integer> coords, PlayableCard card, Side side) {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.networkView.playCard(coords, card, side);
    }

    /**
     * Draws a card.
     *
     * @param source The drawing source to draw the card from
     */
    public void drawCard(DrawSource source) {
        this.setLastRequestStatus(RequestStatus.PENDING);
        this.networkView.drawCard(source);
    }

    /**
     * Method used to show the turn has changed
     */
    public abstract void changePlayer();

    /**
     * Goes to the next turn, making sure that the current player is set and that he plays the right
     * turn (choose initial card/objective, or make a move).
     */
    private void nextPlayer() {
        if (this.currentPlayer == null)
            this.currentPlayer = this.players.get(0);
        else
            this.currentPlayer = this.players.get((this.players.indexOf(currentPlayer) + 1) % this.players.size());


        if (this.currentPlayer.equals(this.username)) {
            if (this.clientBoards.get(this.username).getPlaced().isEmpty())
                this.drawInitialCard();
            else if (this.clientBoards.get(this.username).getObjective() == null)
                this.drawSecretObjectives();
            else
                this.makeMove();
        } else {
            this.changePlayer();
        }
    }


    /**
     * Ask the user to make a play. Must call {@link GraphicalView#playCard(Pair, PlayableCard, Side)}
     */
    public abstract void makeMove();


    /**
     * Starts match on the client side, setting all variables to their initial values
     *
     * @param playersUsernamesAndPawns Map containing all players' pawns, indexed by their username
     * @param playersHands             Map containing all the players' hands, indexed by their username
     * @param visibleObjectives        The two objectives common to every player
     * @param visiblePlayableCards     The four cards that can be drawn, visible to everyone
     * @param decksTopReign            the reigns of the two decks' top
     */
    public void matchStarted(Map<String, Color> playersUsernamesAndPawns, Map<String, List<PlayableCard>> playersHands,
                             Pair<Objective, Objective> visibleObjectives, Map<DrawSource, PlayableCard> visiblePlayableCards,
                             Pair<Symbol, Symbol> decksTopReign) {
        this.players = new ArrayList<>();
        this.clientBoards = new HashMap<>();
        Color curr;
        playersUsernamesAndPawns.forEach((player, pawn) -> this.players.add(player));

        for (String username : playersUsernamesAndPawns.keySet()) {
            curr = playersUsernamesAndPawns.get(username);
            switch (curr) {
                case Color.RED:
                    this.players.set(0, username);
                    break;
                case Color.BLUE:
                    this.players.set(1, username);
                    break;
                case Color.GREEN:
                    this.players.set(2, username);
                    break;
                case Color.YELLOW:
                    this.players.set(3, username);
                    break;
                default:
                    break;
            }
        }

        this.currentPlayer = null;

        playersHands.forEach((username, hand) -> {
            this.clientBoards.put(username, new ClientBoard(playersUsernamesAndPawns.get(username), hand));
        });

        this.visiblePlayableCards = visiblePlayableCards;
        this.visibleObjectives = visibleObjectives;
        this.decksTopReign = decksTopReign;

        this.notifyMatchStarted();
        this.nextPlayer();
    }


    /**
     * Method that shows the user that the match has started
     */
    protected abstract void notifyMatchStarted();


    public void receiveAvailableMatches(List<AvailableMatch> availableMatches) {
        this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        this.availableMatches = availableMatches;
    }

    /**
     * Give the user its initial card
     *
     * @param initialCard the player's initial card
     */
    public void giveInitialCard(InitialCard initialCard) {
        this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        this.clientBoards.get(this.username).setInitial(initialCard);
    }


    /**
     * Gives the player two secret objectives to choose from
     *
     * @param secretObjectives the two objectives to choose from
     */
    public void giveSecretObjectives(Pair<Objective, Objective> secretObjectives) {
        this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
    }

    /**
     * Notifies other players that someone drew the initial card
     *
     * @param someoneUsername Player who drew the initial
     * @param card            The card he drew
     */
    public void someoneDrewInitialCard(String someoneUsername, InitialCard card) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
        this.clientBoards.get(someoneUsername).setInitial(card);
    }


    /**
     * Effectively place the initial card on the player's board, on the right side. Note that the card
     * must have already been set
     *
     * @param someoneUsername Player who chose the initial card's side
     * @param side            Chosen side
     */
    public void someoneSetInitialSide(String someoneUsername, Side side, Map<Symbol, Integer> availableResources) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
        this.clientBoards.get(someoneUsername).placeInitial(side, availableResources);
        this.nextPlayer();
    }


    /**
     * Notifies other players that someone is choosing the secret objective. They should not know from
     * which objective he is choosing, so they are not passed
     *
     * @param someoneUsername Player who is choosing
     */
    public void someoneDrewSecretObjective(String someoneUsername) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
    }

    public void someoneChoseSecretObjective(String someoneUsername) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
        this.nextPlayer();
    }


    /**
     * Actually places a card on the player's board (so the Player tried to place a card and it was a
     * valid move)
     *
     * @param someoneUsername    The player who made the move
     * @param coords             where he placed the card
     * @param card               the placed card
     * @param side               the side the card was placed on
     * @param points             the total points of the player after he placed the card
     * @param availableResources the available resources of the player after he placed the card
     */
    public void someonePlayedCard(String someoneUsername, Pair<Integer, Integer> coords, PlayableCard card, Side side, int points,
                                  Map<Symbol, Integer> availableResources) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
        if (points >= 20 && !this.lastTurn) {
            this.lastTurn = true;
            this.notifyLastTurn();
        }
        this.clientBoards.get(someoneUsername).placeCard(coords, card, side, points, availableResources);
    }


    /**
     * Handles the replacement of the last card drawn, and changes turn
     *
     * @param someoneUsername      Player who drew the card
     * @param source               From where he drew the card
     * @param card                 The card he drew
     * @param replacementCard      The replacement card, which will be null if the {@link DrawSource} is a
     *                             deck
     * @param replacementCardReign The replacement card's reign, which will be null if the
     *                             {@link DrawSource} is not a deck
     */
    public void someoneDrewCard(String someoneUsername, DrawSource source, PlayableCard card, PlayableCard replacementCard,
                                Symbol replacementCardReign) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
        if (source.equals(DrawSource.GOLDS_DECK)) {
            this.decksTopReign = new Pair<Symbol, Symbol>(replacementCardReign, this.decksTopReign.second());
        } else if (source.equals(DrawSource.RESOURCES_DECK)) {
            this.decksTopReign = new Pair<Symbol, Symbol>(this.decksTopReign.first(), replacementCardReign);
        } else {
            visiblePlayableCards.put(source, replacementCard);
        }

        if (decksTopReign.first() == null && decksTopReign.second() == null && !this.lastTurn) {
            this.lastTurn = true;
            this.notifyLastTurn();
        }
        this.clientBoards.get(someoneUsername).drawCard(card);

        this.nextPlayer();
    }

    /**
     * Notifies the player that this is the last turn he can play
     */
    public void notifyLastTurn() {
        this.lastTurn = true;
    }

    /**
     * Notifies the player that someone joined the lobby
     *
     * @param someoneUsername Player who joined
     */
    public void someoneJoined(String someoneUsername, List<String> joinedPlayers) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
    }

    /**
     * Notifies the player that someone quit the lobby
     *
     * @param someoneUsername Player who quit
     */
    public abstract void someoneQuit(String someoneUsername);


    /**
     * Shows the player the match's leaderboard after the game ended
     *
     * @param ranking Ranking of players
     */
    public abstract void matchFinished(List<LeaderboardEntry> ranking);

    /**
     * Notifies that someone sent a broadcast text
     *
     * @param someoneUsername Player who sent the text
     * @param text            Text he sent
     */
    public void someoneSentBroadcastText(String someoneUsername, String text) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
    }

    /**
     * Notifies the player that someone sent him a private text
     *
     * @param someoneUsername Player who sent the private text
     * @param text            Text he sent
     */
    public void someoneSentPrivateText(String someoneUsername, String text) {
        if (this.username.equals(someoneUsername)) {
            this.setLastRequestStatus(RequestStatus.SUCCESSFUL);
        }
    }
}