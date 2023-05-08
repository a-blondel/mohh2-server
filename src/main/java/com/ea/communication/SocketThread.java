package com.ea.communication;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Thread to handle a unique socket
 */
@Slf4j
public class SocketThread implements Runnable {

    Socket clientSocket;

    public void setClientSocket(Socket clientSocket) throws SocketException {
        this.clientSocket = clientSocket;
        // Set timeout for reading from client socket
        // this.clientSocket.setSoTimeout(30000);
    }

    public void run() {

        log.info("Client session started: {} | {} | {}", clientSocket.hashCode(),
                clientSocket.getLocalSocketAddress().toString(),
                clientSocket.getRemoteSocketAddress().toString());

        try {
            exchangeWithSocket(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log.info("Client session ended: " + clientSocket.hashCode());
        }
    }

    /**
     * Read and write on the socket
     * @param socket
     * @throws IOException
     */
    private static void exchangeWithSocket(Socket socket) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            String line;
            StringBuffer response = new StringBuffer();
            // TODO : parse requests (between '@' and last '\0'), create objects (id, content)
            while ((line = reader.readLine()) != null) {
                log.info("Received: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}
