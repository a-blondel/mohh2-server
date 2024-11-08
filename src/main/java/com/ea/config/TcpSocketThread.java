package com.ea.config;

import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.repositories.GameReportRepository;
import com.ea.repositories.GameRepository;
import com.ea.services.GameService;
import com.ea.services.PersonaService;
import com.ea.services.SocketManager;
import com.ea.steps.SocketReader;
import com.ea.steps.SocketWriter;
import com.ea.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;

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

    private static GameService gameService = BeanUtil.getBean(GameService.class);

    private static GameRepository gameRepository = BeanUtil.getBean(GameRepository.class);

    private static GameReportRepository gameReportRepository = BeanUtil.getBean(GameReportRepository.class);

    private final Socket clientSocket;

    private ScheduledExecutorService pingExecutor;

    public TcpSocketThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        log.info("TCP client session started: {}", clientSocket.getRemoteSocketAddress().toString());
        try {
            pingExecutor = Executors.newSingleThreadScheduledExecutor();
            pingExecutor.scheduleAtFixedRate(() -> png(clientSocket), 30, 30, TimeUnit.SECONDS);

            SocketReader.read(clientSocket);
        } finally {
            if (pingExecutor != null) {
                pingExecutor.shutdown();
            }
            SocketWrapper socketWrapper = SocketManager.getSocketWrapper(clientSocket);
            if(socketWrapper != null && socketWrapper.getPersonaEntity() != null) {
                gameService.endGameReport(socketWrapper);
                personaService.endPersonaConnection(socketWrapper);
                SocketManager.removeSocket(socketWrapper.getIdentifier());
            }
            log.info("TCP client session ended: {}", clientSocket.getRemoteSocketAddress().toString());
        }
    }

    public void png(Socket socket) {
        SocketData socketData = new SocketData("~png", null, null);
        SocketWriter.write(socket, socketData);
    }

}