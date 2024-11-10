package com.ea.config;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.services.GameService;
import com.ea.services.PersonaService;
import com.ea.services.SocketManager;
import com.ea.steps.SocketReader;
import com.ea.steps.SocketWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread to handle a unique tcp socket
 */
@Slf4j
@RequiredArgsConstructor
public class TcpSocketThread implements Runnable {

    private ScheduledExecutorService pingExecutor;

    private final Socket clientSocket;
    private final SocketManager socketManager;
    private final SocketReader socketReader;
    private final SocketWriter socketWriter;
    private final PersonaService personaService;
    private final GameService gameService;

    @Override
    public void run() {
        log.info("TCP client session started: {}", clientSocket.getRemoteSocketAddress());
        try {
            pingExecutor = Executors.newSingleThreadScheduledExecutor();
            pingExecutor.scheduleAtFixedRate(() -> png(clientSocket), 30, 30, TimeUnit.SECONDS);
            socketReader.read(clientSocket);
        } catch (Exception e) {
            log.error("Exception in TcpSocketThread: ", e);
        } finally {
            if (pingExecutor != null && !pingExecutor.isShutdown()) {
                pingExecutor.shutdownNow();
            }
            SocketWrapper socketWrapper = socketManager.getSocketWrapper(clientSocket);
            if (socketWrapper != null && socketWrapper.getPersonaEntity() != null) {
                gameService.endGameReport(socketWrapper);
                personaService.endPersonaConnection(socketWrapper);
                socketManager.removeSocket(socketWrapper.getIdentifier());
            }
            log.info("TCP client session ended: {}", clientSocket.getRemoteSocketAddress());
        }
    }

    private void png(Socket socket) {
        SocketWrapper socketWrapper = socketManager.getSocketWrapper(socket);
        if (socketWrapper != null) {
            AtomicInteger pingSendCounter = socketWrapper.getPingSendCounter();
            AtomicInteger pingReceiveCounter = socketWrapper.getPingReceiveCounter();
            if (!socketWrapper.getIsHost().get() && pingReceiveCounter.get() != pingSendCounter.get()) {
                log.warn("Client did not respond to last ping, closing socket");
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Error closing socket", e);
                }
                return;
            }
            Map<String, String> content = Collections.singletonMap("TIME", String.valueOf(pingSendCounter.incrementAndGet()));
            SocketData socketData = new SocketData("~png", null, content);
            socketWriter.write(socket, socketData);
        }
    }
}