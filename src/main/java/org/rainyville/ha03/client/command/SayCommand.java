package org.rainyville.ha03.client.command;

import org.apache.logging.log4j.Level;
import org.rainyville.ha03.MessageType;
import org.rainyville.ha03.client.ClientHandler;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * PACKAGE: org.rainyville.ha03.server.command
 * DATE: 10/20/2024
 * TIME: 6:45 PM
 * PROJECT: HA03
 */
public class SayCommand implements TerminalCommand {
    @Override
    public String getName() {
        return "say";
    }

    @Override
    public void onCommand(ClientHandler client, String label, String[] args) {
        if (args.length == 0) {
            client.addMessage(Level.ERROR, "Invalid command arguments.");
        }

        String message = String.join(" ", args);
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));

        client.sendToServer(MessageType.TEXT, buffer.array(), "");
    }
}
