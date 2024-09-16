package com.ea.config;

import com.ea.dto.SessionData;
import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.entities.LobbyEntity;
import com.ea.entities.LobbyReportEntity;
import com.ea.repositories.LobbyReportRepository;
import com.ea.repositories.LobbyRepository;
import com.ea.services.LobbyService;
import com.ea.services.PersonaService;
import com.ea.services.SocketManager;
import com.ea.steps.SocketReader;
import com.ea.steps.SocketWriter;
import com.ea.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
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

    private static SocketManager socketManager = BeanUtil.getBean(SocketManager.class);

    private static LobbyRepository lobbyRepository = BeanUtil.getBean(LobbyRepository.class);

    private static LobbyReportRepository lobbyReportRepository = BeanUtil.getBean(LobbyReportRepository.class);

    private final Socket clientSocket;

    private final SessionData sessionData;

    private ScheduledExecutorService pingExecutor;

    public TcpSocketThread(Socket clientSocket, SessionData sessionData) {
        this.clientSocket = clientSocket;
        this.sessionData = sessionData;
    }

    public void run() {
        log.info("TCP client session started: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        try {
            pingExecutor = Executors.newSingleThreadScheduledExecutor();
            pingExecutor.scheduleAtFixedRate(() -> png(clientSocket), 30, 30, TimeUnit.SECONDS);

            SocketReader.read(clientSocket, sessionData);
        } finally {
            pingExecutor.shutdown();
            lobbyService.endLobbyReport(sessionData); // If the player doesn't leave from the game
            personaService.endPersonaConnection(sessionData);

            SocketWrapper socketWrapper = socketManager.getSocketWrapper(clientSocket.getRemoteSocketAddress().toString());
            if(socketWrapper != null) {
                if(socketWrapper.isHost() && socketWrapper.getLobbyId() != null) {
                    LobbyEntity lobbyEntity = lobbyRepository.findById(socketWrapper.getLobbyId()).orElse(null);
                    if(lobbyEntity != null) {
                        lobbyEntity.setEndTime(Timestamp.from(Instant.now()));
                        for(LobbyReportEntity lobbyReportEntity : lobbyEntity.getLobbyReports()) {
                            if(lobbyReportEntity.getEndTime() == null) {
                                lobbyReportEntity.setEndTime(Timestamp.from(Instant.now()));
                                lobbyReportRepository.save(lobbyReportEntity);
                            }
                        }
                        lobbyRepository.save(lobbyEntity);
                    }
                }
                socketManager.removeSocket(socketWrapper.getIdentifier());
            }
            log.info("TCP client session ended: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        }
    }

    public void png(Socket socket) {
        SocketData socketData = new SocketData("~png", null, null);
        SocketWriter.write(socket, socketData);
    }

}
