package com.ea.config;

import com.ea.steps.SocketReader;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSocket;

/**
 * Thread to handle a unique SSL socket
 */
@Slf4j
public class SslSocketThread implements Runnable {

    private final SSLSocket clientSocket;

    public SslSocketThread(SSLSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        log.info("SSL client session started: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        try {
            SocketReader socketReader = new SocketReader();
            socketReader.read(clientSocket, null);
        } finally {
            log.info("SSL client session ended: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        }
    }

}
