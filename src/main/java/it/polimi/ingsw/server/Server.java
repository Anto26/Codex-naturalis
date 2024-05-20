package it.polimi.ingsw.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import it.polimi.ingsw.controllers.PlayerControllerRMI;
import it.polimi.ingsw.exceptions.AlreadyUsedUsernameException;
import it.polimi.ingsw.exceptions.ChosenMatchException;
import it.polimi.ingsw.exceptions.WrongStateException;
import it.polimi.ingsw.gamemodel.InitialCard;
import it.polimi.ingsw.gamemodel.Match;
import it.polimi.ingsw.gamemodel.Objective;
import it.polimi.ingsw.gamemodel.PlayableCard;
import it.polimi.ingsw.network.tcp.TCPServer;
import it.polimi.ingsw.utils.DeckCreator;

public class Server extends UnicastRemoteObject implements ServerRMIInterface {
    private final Map<String, Match> matches;

    private final int portRMI;
    private final int portTCP;

    // Cards to be used in matches
    private static final Map<Integer, Objective> objectives = new HashMap<>();
    private static final Map<Integer, PlayableCard> playableCards = new HashMap<>();
    private static final Map<Integer, InitialCard> initialCards = new HashMap<>();

    public static Objective getObjective(Integer id) {
        return Server.objectives.get(id);
    }

    public static PlayableCard getPlayableCard(Integer id) {
        return Server.playableCards.get(id);
    }

    public static InitialCard getiInitialCard(Integer id) {
        return Server.initialCards.get(id);
    }

    public Server(int portRMI, int portTCP) throws RemoteException {
        super();

        this.portRMI = portRMI;
        this.portTCP = portTCP;

        matches = new HashMap<>();
    }

    @Override
    public List<String> getJoinableMatches() {
        return matches.keySet().stream().filter(name -> !matches.get(name).isFull()).toList();
    }

    @Override
    public PlayerControllerRMI joinMatch(String matchName, String username) throws RemoteException, ChosenMatchException, WrongStateException, AlreadyUsedUsernameException {
        if (!matches.containsKey(matchName))
            throw new ChosenMatchException("The chosen match doesn't exist");
        if (matches.get(matchName).isFull())
            throw new ChosenMatchException("The chosen match is already full");

        Match chosenMatch = matches.get(matchName);

        return new PlayerControllerRMI(username, chosenMatch);
    }

    @Override
    // TODO: Implement this method, synchronize it
    public void createMatch(String matchName, int maxPlayers) throws RemoteException, ChosenMatchException {
        if (matches.containsKey(matchName))
            throw new ChosenMatchException("A match with the chosen name already exists");

        Match newMatch = getNewMatch(maxPlayers);

        matches.put(matchName, newMatch);
    }

    public Map<String, Match> getJoinableMatchesMap() {
        HashMap<String, Match> result = new HashMap<>();
        for (String name : matches.keySet()) {
            result.put(name, matches.get(name));
        }
        return result;
    }

    public Match getMatch(String name) {
        return matches.get(name);
    }

    public static Match getNewMatch(int maxPlayers) {
        DeckCreator creator = new DeckCreator();
        return new Match(maxPlayers, creator.createInitialDeck(), creator.createResourceDeck(), creator.createGoldDeck(),
                creator.createObjectiveDeck());
    }

    public void startRMIServer() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(portRMI);
        registry.rebind("CodexNaturalisRMIServer", this);
    }

    public void startTCPServer() {
        TCPServer tcpServer = new TCPServer(portTCP, this);
        new Thread(tcpServer::listen).start();
    }

    public static String promptAndInput(String message, Scanner scanner) {
        System.out.print(message);
        return scanner.nextLine();
    }

    public static void main(String[] args) throws RemoteException {
        int portRMI = Integer.parseInt(args[0]);
        int portTCP = Integer.parseInt(args[1]);

        Server server = new Server(portRMI, portTCP);

        server.startRMIServer();
        server.startTCPServer();

        /* Scanner scanner = new Scanner(System.in);
        String choice;

        do {
            choice = promptAndInput("What do you want to do?\n\t0: exit\n\t1: create match\n\t2: show matches\n", scanner);

            switch (choice) {
                case "1" -> {
                    String matchName = promptAndInput("Match name: ", scanner);
                    int maxPlayers = Integer.parseInt(promptAndInput("Maximum number of players: ", scanner));

                    try {
                        server.createMatch(matchName, maxPlayers);
                    } catch (ChosenMatchException e) {
                        System.out.println(e.getMessage());
                    }
                }

                case "2" -> server.matches.keySet().forEach(System.out::println);
            }
        } while (!choice.equals("0")); */
    }

}
