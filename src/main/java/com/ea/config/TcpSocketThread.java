package com.ea.config;

import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
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
import java.time.LocalDateTime;
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
        log.info("TCP client session started: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        try {
            pingExecutor = Executors.newSingleThreadScheduledExecutor();
            pingExecutor.scheduleAtFixedRate(() -> png(clientSocket), 30, 30, TimeUnit.SECONDS);

            SocketReader.read(clientSocket);
        } finally {
            if (pingExecutor != null) {
                pingExecutor.shutdown();
            }
            SocketWrapper socketWrapper = SocketManager.getSocketWrapper(clientSocket);
            if(socketWrapper != null) {
                GameEntity gameEntity = socketWrapper.getGameEntity();
                if(socketWrapper.isHost() && gameEntity != null) {
                    for(GameReportEntity gameReportEntity : gameReportRepository.findByGameIdAndEndTimeIsNull(gameEntity.getId())) {
                        gameReportEntity.setEndTime(LocalDateTime.now());
                        gameReportRepository.save(gameReportEntity);
                    }
                    gameEntity.setEndTime(LocalDateTime.now());
                    gameRepository.save(gameEntity);
                } else {
                    gameService.endGameReport(socketWrapper); // If the player doesn't leave from the game
                    if(gameEntity != null) {
                        gameService.updatePlayerList(gameEntity, socketWrapper);
                    }
                }
                personaService.endPersonaConnection(socketWrapper);
                SocketManager.removeSocket(socketWrapper.getIdentifier());
            }
            log.info("TCP client session ended: {}:{}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        }
    }

    public void png(Socket socket) {
        SocketData socketData = new SocketData("~png", null, null);
        SocketWriter.write(socket, socketData);
    }

}