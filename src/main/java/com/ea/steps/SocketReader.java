package com.ea.steps;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

@Slf4j
public class SocketReader {

    /**
     * Waits for data to come from the client
     * Calls a parser to handle input messages from the stream
     * @param socket the socket to read
     * @throws IOException
     */
    public static void read(Socket socket) {
        try {
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[4096];
            int readLength;
            while((readLength = is.read(buffer)) != -1) {
                SocketParser.parse(socket, buffer, readLength);
            }
        } catch (SocketException e) {
            log.warn("Socket closed, stopping reading");
        } catch (IOException e) {
            log.error("Error reading from socket", e);
        }
    }

}
