package org.rainyville.ha03.client.command;

import org.apache.logging.log4j.Level;
import org.rainyville.ha03.client.ClientHandler;

/**
 * PACKAGE: org.rainyville.ha03.client.command
 * DATE: 10/21/2024
 * TIME: 1:49 AM
 * PROJECT: HA03
 */
public class StopCommand implements TerminalCommand {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public void onCommand(ClientHandler client, String label, String[] args) {
        client.addMessage(Level.WARN, "Stopping...");
        client.stop();
    }
}
