package com.ea.config;

import com.ea.dto.SessionData;
import com.ea.dto.SocketData;
import com.ea.services.LobbyService;
import com.ea.services.PersonaService;
import com.ea.services.SocketManager;
import com.ea.steps.SocketReader;
import com.ea.steps.SocketWriter;
import com.ea.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread to handle a unique tcp socket
 */
@Slf4j
public class TcpSocketThread implements Runnable {

    private static PersonaService personaService = BeanUtil.getBean(PersonaService.class);

    private static LobbyService lobbyService = BeanUtil.getBean(LobbyService.class);

    private final Socket clientSocket;

    private final SessionData sessionData;

    private ScheduledExecutorService pingExecutor;

    @Autowired
    private SocketManager socketManager;

    public TcpSocketThread(Socket clientSocket, SessionData sessionData) {
        this.clientSocket = clientSocket;
        this.sessionData = sessionData;
    }

    public void run() {
        log.info("TCP client session started: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        String socketIdentifier = clientSocket.getRemoteSocketAddress().toString(); // TODO : check if necessary
        try {
            pingExecutor = Executors.newSingleThreadScheduledExecutor();
            pingExecutor.scheduleAtFixedRate(() -> png(clientSocket), 30, 30, TimeUnit.SECONDS);

            SocketReader.read(clientSocket, sessionData);
        } finally {
            pingExecutor.shutdown();
            lobbyService.endLobbyReport(sessionData); // If the player doesn't leave from the game
            personaService.endPersonaConnection(sessionData);
//            socketManager.removeSocket(socketIdentifier); // TODO : check NullPointerException
            log.info("TCP client session ended: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        }
    }

    public void png(Socket socket) {
        SocketData socketData = new SocketData("~png", null, null);
        SocketWriter.write(socket, socketData);
    }

}
