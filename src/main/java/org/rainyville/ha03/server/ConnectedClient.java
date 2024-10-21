package org.rainyville.ha03.server;

import org.apache.logging.log4j.Level;
import org.rainyville.ha03.MessageType;
import org.rainyville.ha03.ServerDispatcher;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * PACKAGE: org.rainyville.ha03.server
 * DATE: 10/20/2024
 * TIME: 4:59 PM
 * PROJECT: HA03
 */
@SuppressWarnings("DuplicatedCode")
public class ConnectedClient extends Thread {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    private String username;

    public ConnectedClient(Socket socket) throws IOException {
        this.socket = socket;

        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public void sendData(MessageType type, byte[] bytes, String filename) throws IOException {
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
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                // Reconstruct on the server.
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
                    message = String.format("<%s>: %s", getUsername(), message).trim();
                    ServerDispatcher.addMessage(Level.INFO, message);

                    ServerDispatcher.sendDataAll(type, ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)).array(), filename);
                } else {
                    ServerDispatcher.sendDataAll(type, stream.toByteArray(), filename);
                }

                stream.close();
            }
        } catch (IOException ex) {
            // ignored
        }

        ServerDispatcher.disconnect(this);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
