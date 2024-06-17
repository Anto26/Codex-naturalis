package it.polimi.ingsw.controllers;

import it.polimi.ingsw.exceptions.AlreadyUsedUsernameException;
import it.polimi.ingsw.exceptions.ChosenMatchException;
import it.polimi.ingsw.exceptions.WrongNameException;
import it.polimi.ingsw.exceptions.WrongStateException;
import it.polimi.ingsw.gamemodel.Match;
import it.polimi.ingsw.gamemodel.MatchObserver;
import it.polimi.ingsw.gamemodel.Player;
import it.polimi.ingsw.utils.GuiUtil;

import java.util.Optional;

/**
 * Controller for a match player, the only agent needing a view and so a controller in this
 * application. This class subclasses instances are given (in RMI case) / reachable (in TCP case) on
 * the network and collected by a corresponding view (RMI view or TCP view); then this class commits
 * its two subclasses {@link PlayerControllerRMI} and {@link PlayerControllerTCP} to implement all
 * the methods needed by a generic view to play in a match. This class implements
 * {@link MatchObserver} since its instances subscribe themselves to a Match, as mentioned in
 * {@link #PlayerController(String, Match)}; this is needed to allow this class to behave as a
 * bridge between a view and a match.
 */
public abstract sealed class PlayerController implements MatchObserver permits PlayerControllerRMI, PlayerControllerTCP {
    protected Player player;
    protected Match match;

    /**
     * Instantiates the internal Player with the given username and sets the internal Match reference to
     * the given one, furthermore add the new Player instance to the match and subscribe this class
     * instance to the match observers.
     *
     * @param username The username of the new player of the Match
     * @param match    The match to which this PlayerClass must pertain
     */
    public PlayerController(String username, Match match) {
        this.player = new Player(username, match);
        this.match = match;
    }

    /**
     * Gets the player linked to this PlayerController instance.
     *
     * @return The player linked to this instance
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Tries to effectively join a match, adding himself to the list of observers and the corresponding
     * player to the match, if the username is valid
     *
     * @throws AlreadyUsedUsernameException if the username is already taken
     * @throws WrongStateException          if the match currently does not accept new players
     */
    public void sendJoined() throws IllegalArgumentException, AlreadyUsedUsernameException, WrongStateException, ChosenMatchException, WrongNameException {
        if (!GuiUtil.isValidName(this.player.getUsername())) {
            throw new WrongNameException("The match name must be alphanumeric with maximum 32 characters");
        }
        if (match == null) {
            throw new ChosenMatchException("The specified match does not exist");
        }

        try {
            synchronized (match) {
                if (!match.isRejoinable()) {
                    match.subscribeObserver(this);
                    match.addPlayer(this.player);
                } else {
                    // Rejoin a match
                    // Get the player with the same username and not already connected
                    Optional<Player> playerOptional = match.getPlayers().stream()
                            .filter((p) -> p.getUsername().equals(player.getUsername()))
                            .filter((p) -> !p.isConnected())
                            .findFirst();
                    if (playerOptional.isPresent()) {
                        player = playerOptional.get();
                        player.setConnected(true);
                        match.subscribeObserver(this);
                        this.matchResumed();
                    } else {
                        throw new WrongStateException("There is no disconnected player with this username");
                    }
                }
            }
        } catch (AlreadyUsedUsernameException | IllegalArgumentException e) {
            match.unsubscribeObserver(this);
            throw e;
        }
    }

    public abstract void matchResumed();
}
