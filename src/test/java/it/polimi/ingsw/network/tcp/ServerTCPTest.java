package it.polimi.ingsw.network.tcp;

import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.actions.CreateMatchMessage;
import it.polimi.ingsw.network.messages.actions.GetAvailableMatchesMessage;
import it.polimi.ingsw.network.messages.actions.JoinMatchMessage;
import it.polimi.ingsw.network.messages.actions.SendBroadcastTextMessage;
import it.polimi.ingsw.network.messages.actions.SendPrivateTextMessage;
import it.polimi.ingsw.server.Server;
import it.polimi.ingsw.utils.MessageJsonParser;

public class ServerTCPTest {
    MessageJsonParser messageParser;
    Integer port;
    Server server;
    List<Socket> sockets;

    public ServerTCPTest() throws RemoteException {
        messageParser = new MessageJsonParser();
        port = 9999;
        server = new Server(12, port);
        sockets = new ArrayList<>();

        TCPServer tcpServer = new TCPServer(port, server);
        new Thread(() -> {
            tcpServer.listen();
        }).start();
    }

    private IOHandler matchCreator(String username, String matchName, Integer maxPlayers) {
        IOHandler io;
        try {
            Socket socket = new Socket("localhost", this.port);
            io = new IOHandler(socket);

            Message create = new CreateMatchMessage(username, matchName, maxPlayers);
            io.writeMsg(create);
            io.readMsg();
        } catch (Exception e) {
            return null;
        }

        return io;
    }

    private IOHandler matchJoin(String username, String matchName) {
        IOHandler io;
        try {
            Socket socket = new Socket("localhost", this.port);
            io = new IOHandler(socket);

            Message getAvailable = new GetAvailableMatchesMessage(username);
            io.writeMsg(getAvailable);
            io.readMsg();
            Message join = new JoinMatchMessage(username, matchName);
            io.writeMsg(join);
            io.readMsg();
        } catch (Exception e) {
            return null;
        }

        return io;
    }

    @Test
    public void testChat() throws Exception {
        String matchCreatorUsername = "Davide";
        String matchJoineeUsername = "Luca";
        String matchName = "pippo";

        IOHandler matchCreator = this.matchCreator(matchCreatorUsername, matchName, 4);
        IOHandler matchJoinee = this.matchJoin(matchJoineeUsername,matchName);
        IOHandler matchJoineev2 = this.matchJoin(matchJoineeUsername+"V2",matchName);

        Message publicText = new SendBroadcastTextMessage(matchCreatorUsername, "ciao bimbi");
        matchCreator.writeMsg(publicText);
        Message privateText = new SendPrivateTextMessage(matchCreatorUsername, matchJoineeUsername, "ciao bimbo");
        matchCreator.writeMsg(privateText);
        matchCreator.writeMsg(publicText); // avoid infinite loop and check if the other user receives the message

        matchJoinee.readMsg(); // remove join
        System.out.println(matchJoinee.readMsg());
        System.out.println(matchJoineev2.readMsg());

        System.out.println(matchJoinee.readMsg());
        System.out.println(matchJoineev2.readMsg());
    }

}
