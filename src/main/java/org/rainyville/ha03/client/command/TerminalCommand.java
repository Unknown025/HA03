package org.rainyville.ha03.client.command;

import org.rainyville.ha03.client.ClientHandler;

/**
 * PACKAGE: org.rainyville.ha03.server.command
 * DATE: 10/20/2024
 * TIME: 6:32 PM
 * PROJECT: HA03
 */
public interface TerminalCommand {
    /**
     * Name of the command.
     *
     * @return Name used to call this command.
     */
    String getName();

    /**
     * Called when this command is used.
     *
     * @param client Client executing this command.
     * @param label  Label this command was used under (always matches {@link #getName()}).
     * @param args   Arguments of this command, not including the label.
     */
    void onCommand(ClientHandler client, String label, String[] args);
}
