package org.rainyville.ha03.client;

import joptsimple.internal.Strings;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.rainyville.ha03.MessageType;
import org.rainyville.ha03.client.command.SayCommand;
import org.rainyville.ha03.client.command.StopCommand;
import org.rainyville.ha03.client.command.TerminalCommand;
import org.rainyville.ha03.client.command.UploadCommand;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

/**
 * PACKAGE: org.rainyville.ha03.client
 * DATE: 10/20/2024
 * TIME: 5:20 PM
 * PROJECT: HA03
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ClientHandler {
    private final Logger logger = LogManager.getLogger("Chat");
    private final LineReader reader;
    private final HashMap<String, TerminalCommand> commands = new HashMap<>();

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    private boolean running;

    public ClientHandler() throws IOException {
        this.socket = new Socket(InetAddress.getByName("localhost"), 50010);

        final Terminal terminal = TerminalConsoleAppender.getTerminal();
        if (terminal == null)
            throw new NullPointerException("Terminal is null!");
        reader = LineReaderBuilder.builder().appName("HA03").terminal(terminal).build();
        TerminalConsoleAppender.setReader(reader);

        commands.put("say", new SayCommand());
        commands.put("upload", new UploadCommand());
        commands.put("stop", new StopCommand());

        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        running = true;
    }

    public void start() throws IOException {
        logger.info("Connected to {}", socket.getRemoteSocketAddress());

        new Thread(() -> {
            // Receive files
            try {
                while (!Thread.interrupted() && running) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    MessageType type = MessageType.values()[in.readInt()];
                    String filename = in.readUTF();
                    int bytes;
                    long size = in.readLong();
                    byte[] buffer = new byte[4 * 1024];
                    while (size > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                        stream.write(buffer, 0, bytes);
                        size -= bytes;
                    }

                    if (type == MessageType.TEXT) {
                        byte[] byteArray = stream.toByteArray();
                        String message = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(byteArray)).toString();
                        logger.info(message);
                    } else if (type == MessageType.FILE) {
                        int index = 0;
                        File receivedFile = new File("received", filename);
                        while (receivedFile.exists())
                            receivedFile = new File(receivedFile.getParentFile(), filename + "." + ++index);
                        logger.info("Received file {}", receivedFile);
                        if (!receivedFile.getParentFile().exists()) {
                            receivedFile.getParentFile().mkdirs();
                        }
                        Files.write(receivedFile.toPath(), stream.toByteArray());
                    }
                }
            } catch (SocketException e) {
                // Log only if we're still supposed to be running.
                if (running)
                    logger.error("Error while reading incoming packets", e);
            } catch (IOException e) {
                logger.error("Error while reading incoming packets", e);
            }
        }).start();

        String input;

        while (!Thread.interrupted() && running) {
            input = reader.readLine("> ");
            if (Strings.isNullOrEmpty(input)) {
                // terminal dead, exit.
                break;
            }

            // Handle commands
            String[] raw = input.split(" ");
            if (raw.length == 0) continue;

            TerminalCommand tc = commands.get(raw[0]);
            if (tc == null) {
                logger.error("Unknown command");
                continue;
            }
            String[] args = new String[raw.length - 1];
            System.arraycopy(raw, 1, args, 0, raw.length - 1);
            tc.onCommand(this, raw[0], args);
        }

        in.close();
        out.close();
    }

    public void sendToServer(MessageType type, byte[] bytes, String filename) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            out.writeInt(type.ordinal());
            out.writeUTF(filename);

            int size;

            out.writeLong(bytes.length);
            byte[] buffer = new byte[4 * 1024];
            while ((size = stream.read(buffer)) != -1) {
                out.write(buffer, 0, size);
                out.flush();
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    public void addMessage(Level level, Object message) {
        logger.log(level, message.toString());
    }

    public void stop() {
        running = false;
        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            // ignored
        }
    }
}
