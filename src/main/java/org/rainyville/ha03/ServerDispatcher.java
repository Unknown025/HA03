package org.rainyville.ha03;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rainyville.ha03.server.ConnectedClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * PACKAGE: org.rainyville.ha03
 * DATE: 10/20/2024
 * TIME: 4:45 PM
 * PROJECT: HA03
 */
public class ServerDispatcher {
    private static final Logger logger = LogManager.getLogger("Server");
    private static ServerSocket serverSocket;
    private static List<ConnectedClient> connectedClients;
    private int userIndex;

    public ServerDispatcher() throws IOException {
        serverSocket = new ServerSocket(50010, 10);
        connectedClients = new LinkedList<>();
    }

    public static void addMessage(Level info, String message) {
        logger.log(info, message);
    }

    public void start() throws IOException {
        logger.info("Server started on port {}", serverSocket.getLocalPort());

        while (!Thread.interrupted()) {
            Socket clientSocket = serverSocket.accept();
            logger.info("Connecting to {}", clientSocket.getRemoteSocketAddress());

            ConnectedClient thread = new ConnectedClient(clientSocket);
            connectedClients.add(thread);
            thread.start();
            thread.setUsername(String.format("User %d", userIndex++));
        }
    }

    public static synchronized void sendDataAll(MessageType type, byte[] byteArray, String filename) {
        try {
            for (ConnectedClient client : connectedClients) {
                client.sendData(type, byteArray, filename);
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    public static synchronized void disconnect(ConnectedClient client) {
        logger.info("{}[{}] disconnected", client.getUsername(), client.getSocket().getRemoteSocketAddress());
        connectedClients.remove(client);
    }
}
