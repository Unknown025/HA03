package org.rainyville.ha03;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.rainyville.ha03.client.ClientHandler;

import java.io.IOException;

/**
 * PACKAGE: org.rainyville.ha03
 * DATE: 10/20/2024
 * TIME: 4:41 PM
 * PROJECT: HA03
 */
public class MainClient {
    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser() {
            {
                accepts("server", "Runs the server.");
            }
        };

        OptionSet options = parser.parse(args);
        if (options.has("server")) {
            ServerDispatcher server = new ServerDispatcher();
            server.start();
        } else {
            ClientHandler clientHandler = new ClientHandler();
            clientHandler.start();
        }
    }
}