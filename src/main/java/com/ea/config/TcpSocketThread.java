package com.ea.config;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.services.GameService;
import com.ea.services.PersonaService;
import com.ea.services.SocketManager;
import com.ea.steps.SocketReader;
import com.ea.steps.SocketWriter;

import com.ea.utils.SocketUtils;
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
            String playerInfo = SocketUtils.getPlayerInfo(socketWrapper);
            if (socketWrapper != null && socketWrapper.getPersonaEntity() != null) {
                gameService.endGameReport(socketWrapper);
                personaService.endPersonaConnection(socketWrapper);
                socketManager.removeSocket(socketWrapper.getIdentifier());
            }
            log.info("TCP client session ended: {} {}", clientSocket.getRemoteSocketAddress(), playerInfo);
        }
    }

    private void png(Socket socket) {
        SocketWrapper socketWrapper = socketManager.getSocketWrapper(socket);
        if (socketWrapper != null) {
            SocketData socketData = new SocketData("~png", null, null);
            socketWriter.write(socket, socketData);
            synchronized (this) {
                socketWrapper.setLastPingSent(LocalDateTime.now());
            }

            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            LocalDateTime lastPingSent = socketWrapper.getLastPingSent();
            LocalDateTime lastPingReceived = socketWrapper.getLastPingReceived();
            boolean isExpired = lastPingReceived != null && lastPingReceived.isBefore(lastPingSent);
            if (isExpired) {
                String playerInfo = SocketUtils.getPlayerInfo(socketWrapper);
                log.warn("{} {} - Last ping sent {} is after last ping received {}",
                        socket.getRemoteSocketAddress().toString(), playerInfo, lastPingSent, lastPingReceived);
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    log.error("Error closing socket", e);
//                }
            }
        }
    }
}