package org.rainyville.ha03.client.command;

import org.apache.logging.log4j.Level;
import org.rainyville.ha03.MessageType;
import org.rainyville.ha03.client.ClientHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * PACKAGE: org.rainyville.ha03.server.command
 * DATE: 10/20/2024
 * TIME: 6:45 PM
 * PROJECT: HA03
 */
public class UploadCommand implements TerminalCommand {
    @Override
    public String getName() {
        return "upload";
    }

    // upload C:\\Users\\Account\\Downloads\\ANDAI Launcher.exe

    @Override
    public void onCommand(ClientHandler client, String label, String[] args) {
        String path = String.join(" ", args);
        File file = new File(path);
        if (!file.exists()) {
            client.addMessage(Level.ERROR, "File not found: " + path);
        }
        client.addMessage(Level.INFO, String.format("Uploading file at: %s", path));
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            client.sendToServer(MessageType.FILE, bytes, file.getName());
        } catch (IOException e) {
            client.addMessage(Level.ERROR, "Could not open file: " + e.getMessage());
        }
    }
}
