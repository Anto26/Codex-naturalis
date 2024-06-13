package it.polimi.ingsw.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import it.polimi.ingsw.exceptions.HandException;
import it.polimi.ingsw.exceptions.WrongChoiceException;
import it.polimi.ingsw.exceptions.WrongStateException;
import it.polimi.ingsw.exceptions.WrongTurnException;
import it.polimi.ingsw.gamemodel.*;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.errors.ErrorMessage;
import it.polimi.ingsw.network.messages.responses.*;
import it.polimi.ingsw.network.tcp.IOHandler;
import it.polimi.ingsw.utils.Pair;
import it.polimi.ingsw.utils.PlacedCardRecord;

public final class PlayerControllerTCP extends PlayerController {
    private IOHandler io;

    public PlayerControllerTCP(String username, Match match, IOHandler io) {
        super(username, match);
        try {
            this.io = io;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Message msg) {
        try {
            this.io.writeMsg(msg);
        } catch (Exception e) {
            this.connectionError();
        }
    }

    private void connectionError() {
        match.removePlayer(player);
        match.unsubscribeObserver(this);
    }

    private ErrorMessage createErrorMessage(Exception e) {
        return new ErrorMessage(e.getMessage(), e.getClass().getName());
    }

    @Override
    public void matchStarted() {
        this.sendMessage(new MatchStartedMessage(match.getVisibleObjectives(),
                match.getVisiblePlayableCards(), match.getDecksTopReigns(), match.getPlayers()));
    }

    @Override
    public void someoneJoined(Player someone) {
        this.sendMessage(new SomeoneJoinedMessage(someone.getUsername(), match.getPlayers(),
                match.getMaxPlayers()));
    }

    @Override
    public void someoneQuit(Player someone) {
        this.sendMessage(new SomeoneQuitMessage(someone.getUsername(), match.getPlayers().size(),
                match.isFinished()));
    }

    @Override
    public void someoneDrewInitialCard(Player someone, InitialCard card) {
        this.sendMessage(new SomeoneDrewInitialCardMessage(someone.getUsername(), card.getId()));
    }

    @Override
    public void someoneSetInitialSide(Player someone, Side side,
            Map<Symbol, Integer> availableResources) {
        this.sendMessage(
                new SomeoneSetInitialSideMessage(someone.getUsername(), side, availableResources));
    }

    @Override
    public void someoneDrewSecretObjective(Player someone, Pair<Objective, Objective> objectives) {
        Pair<Integer, Integer> IDs =
                new Pair<Integer, Integer>(objectives.first().getID(), objectives.second().getID());
        this.sendMessage(new SomeoneDrewSecretObjectivesMessage(someone.getUsername(), IDs));
    }

    @Override
    public void someoneChoseSecretObjective(Player someone, Objective objective) {
        Integer objectiveID = null;
        if (someone.equals(player))
            objectiveID = objective.getID();
        this.sendMessage(
                new SomeoneChoseSecretObjectiveMessage(someone.getUsername(), objectiveID));
    }

    @Override
    public void someonePlayedCard(Player someone, Pair<Integer, Integer> coords, PlayableCard card,
            Side side) {
        this.sendMessage(new SomeonePlayedCardMessage(someone.getUsername(), coords, card.getId(),
                side, someone.getPoints(), someone.getBoard().getAvailableResources()));
    }

    @Override
    public void someoneDrewCard(Player someone, DrawSource source, PlayableCard card,
            PlayableCard replacementCard) {
        Integer repId = null;
        if (replacementCard != null) {
            repId = replacementCard.getId();
        }
        this.sendMessage(new SomeoneDrewCardMessage(someone.getUsername(), source, card.getId(),
                repId, match.getDecksTopReigns()));
    }

    @Override
    public void matchFinished() {
        this.sendMessage(new MatchFinishedMessage(match.getPlayersFinalRanking()));
    }


    public void drawInitialCard() {
        try {
            this.player.drawInitialCard();
        } catch (WrongTurnException | WrongStateException e) {
            this.sendMessage(this.createErrorMessage(e));
        }
    }


    public void chooseInitialCardSide(Side side) {
        try {
            this.player.chooseInitialCardSide(side);
        } catch (WrongTurnException | WrongStateException e) {
            this.sendMessage(this.createErrorMessage(e));
        }
    }

    public void drawSecretObjectives() {
        try {
            this.player.drawSecretObjectives();
        } catch (WrongTurnException | WrongStateException e) {
            this.sendMessage(this.createErrorMessage(e));
        }
    }

    public void chooseSecretObjective(Objective objective) {
        try {
            this.player.chooseSecretObjective(objective);
        } catch (WrongChoiceException | WrongStateException | WrongTurnException e) {
            this.sendMessage(this.createErrorMessage(e));
        }
    }

    public void playCard(Pair<Integer, Integer> coords, PlayableCard card, Side side) {
        try {
            this.player.playCard(coords, card, side);
        } catch (WrongChoiceException | WrongStateException | WrongTurnException e) {
            this.sendMessage(this.createErrorMessage(e));
        }
    }

    public void drawCard(DrawSource source) {
        try {
            this.player.drawCard(source);
        } catch (HandException | WrongTurnException | WrongStateException
                | WrongChoiceException e) {
            this.sendMessage(this.createErrorMessage(e));
        }
    }

    @Override
    public void someoneSentBroadcastText(Player someone, String text) {
        Message msg = new SomeoneSentBroadcastTextMessage(someone.getUsername(), text);
        this.sendMessage(msg);
    }

    @Override
    public void someoneSentPrivateText(Player someone, Player recipient, String text) {
        if (recipient.getUsername().equals(this.player.getUsername())
                || someone.getUsername().equals(this.player.getUsername())) {
            Message msg = new SomeoneSentPrivateTextMessage(someone.getUsername(),
                    recipient.getUsername(), text);
            this.sendMessage(msg);
        }
    }

    public void sendBroadcastText(String text) {
        this.player.sendBroadcastText(text);
    }

    public void sendPrivateText(String recipientUsername, String text) {
        Player recipient = null;
        for (Player player : this.match.getPlayers()) {
            if (player.getUsername().equals(recipientUsername)) {
                recipient = player;
                break;
            }
        }

        // if you want to send error if recipient does not exist, change here
        if (recipient != null) {
            this.player.sendPrivateText(recipient, text);
        }
    }

    @Override
    public void matchResumed() {
        Map<String, Color> playersUsernamesAndPawns = new HashMap<>();
        Map<String, List<Integer>> playersHands = new HashMap<>();
        Pair<Integer, Integer> visibleObjectives;
        Map<DrawSource, Integer> visiblePlayableCards = new HashMap<>();
        Pair<Symbol, Symbol> decksTopReigns;
        Integer secretObjective;
        Map<String, Map<Symbol, Integer>> availableResources = new HashMap<>();
        Map<String, Map<Integer, PlacedCardRecord>> placedCards = new HashMap<>();
        Map<String, Integer> playerPoints = new HashMap<>();
        String currentPlayer;
        boolean drawPhase;

        this.match.getPlayers().forEach(player -> {
            String username = player.getUsername();
            Board board = player.getBoard();
            playersUsernamesAndPawns.put(username, player.getPawnColor());
            playersHands.put(username, board.getCurrentHand().stream().map(card -> card.getId())
                    .collect(Collectors.toList()));
            availableResources.put(username, board.getAvailableResources());

            Map<Integer, PlacedCardRecord> placed = new HashMap<>();
            board.getPlacedCards()
                    .forEach((coords, placedCard) -> placed.put(placedCard.getTurn(),
                            new PlacedCardRecord(placedCard.getCard().getId(), coords.first(),
                                    coords.second(), placedCard.getPlayedSide())));

            placedCards.put(username, placed);
            playerPoints.put(username, player.getPoints());
        });

        Pair<Objective, Objective> visibleObjectivesValue = this.match.getVisibleObjectives();
        // get a Set of Entry, which contains key and value, and create a new Hashmap with key and
        // value.ID
        visibleObjectives = new Pair<Integer, Integer>(visibleObjectivesValue.first().getID(),
                visibleObjectivesValue.second().getID());
        visiblePlayableCards = this.match.getVisiblePlayableCards().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getId()));
        decksTopReigns = this.match.getDecksTopReigns();
        secretObjective = this.player.getSecretObjective().getID();
        currentPlayer = this.match.getCurrentPlayer().getUsername();
        drawPhase = this.match.getCurrentState().getClass().equals(AfterMoveState.class);


        Message msg = new MatchResumedMessage(playersUsernamesAndPawns, playersHands,
        visibleObjectives, visiblePlayableCards, decksTopReigns, secretObjective,
        availableResources, placedCards, playerPoints, currentPlayer, drawPhase);

        this.sendMessage(msg);
    }
}
