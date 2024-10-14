package com.ea.services;

import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
import com.ea.entities.PersonaConnectionEntity;
import com.ea.entities.PersonaEntity;
import com.ea.mappers.SocketMapper;
import com.ea.repositories.GameReportRepository;
import com.ea.repositories.GameRepository;
import com.ea.repositories.PersonaConnectionRepository;
import com.ea.steps.SocketWriter;
import com.ea.utils.GameVersUtils;
import com.ea.utils.Props;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ea.utils.SocketUtils.getValueFromSocket;

@Component
@Slf4j
public class GameService {

    @Autowired
    private Props props;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameReportRepository gameReportRepository;

    @Autowired
    private PersonaConnectionRepository personaConnectionRepository;

    @Autowired
    private SocketMapper socketMapper;

    @Autowired
    private PersonaService personaService;

    /**
     * Distribute room change updates
     * @param socket
     * @param socketData
     */
    public void rom(Socket socket, SocketData socketData) {
        Map<String, String> content = Stream.of(new String[][] {
                { "I", "1" }, // Room identifier
                { "N", "room" }, // Room name
//                { "H", socketManager.getSocketWrapper(socket.getRemoteSocketAddress().toString()).getPers() }, // Room Host
//                { "D", "" }, // Room description
//                { "F", "CK" }, // Attribute flags
//                { "T", "1" }, // Current room population
//                { "L", "33" }, // Max users allowed in room
//                { "P", "0" }, // Room ping
//                { "A", props.getTcpHost() }, // Room address
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        socketData.setIdMessage("+rom");
        SocketWriter.write(socket, socketData);
    }

    public void gsta(Socket socket, SocketData socketData) {
        SocketWriter.write(socket, socketData);
    }

    /**
     * Game count
     * @param socket
     * @param socketData
     */
    public void gsea(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        List<String> relatedVers = GameVersUtils.getRelatedVers(socketWrapper.getPersonaConnectionEntity().getVers());
        List<GameEntity> gameEntities = gameRepository.findByVersInAndEndTimeIsNull(relatedVers);

        Map<String, String> content = Stream.of(new String[][] {
                { "COUNT", String.valueOf(gameEntities.size()) },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);

        gam(socket, gameEntities);
    }

    /**
     * List games
     * @param socket
     * @param gameEntities
     */
    public void gam(Socket socket, List<GameEntity> gameEntities) {
        List<Map<String, String>> games = new ArrayList<>();

        for(GameEntity gameEntity : gameEntities) {
            games.add(Stream.of(new String[][] {
                    { "IDENT", String.valueOf(gameEntity.getId()) },
                    { "NAME", gameEntity.getName() },
                    { "PARAMS", gameEntity.getParams() },
                    { "SYSFLAGS", gameEntity.getSysflags() },
                    { "COUNT", String.valueOf(gameEntity.getGameReports().stream().filter(report -> null == report.getEndTime()).count()) },
                    { "MAXSIZE", String.valueOf(gameEntity.getMaxsize()) },
            }).collect(Collectors.toMap(data -> data[0], data -> data[1])));
        }

        for (Map<String, String> game : games) {
            SocketData socketData = new SocketData("+gam", null, game);
            SocketWriter.write(socket, socketData);
        }
    }

    /**
     * Join a game
     * @param socket
     * @param socketData
     * @param socketWrapper
     */
    public void gjoi(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        String ident = getValueFromSocket(socketData.getInputMessage(), "IDENT");
        Optional<GameEntity> gameEntityOpt = gameRepository.findById(Long.valueOf(ident));
        if(gameEntityOpt.isPresent()) {
            GameEntity gameEntity = gameEntityOpt.get();
            if(gameEntity.getEndTime() == null) {
                startGameReport(socketWrapper, gameEntity, false);

                SocketWriter.write(socket, socketData);

                updatePlayerListOfHost(gameEntity, socketWrapper);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                ses(socket, gameEntity, socketWrapper);
            } else {
                SocketWriter.write(socket, new SocketData("gjoiugam", null, null)); // Game closed
            }
        } else {
            SocketWriter.write(socket, new SocketData("gjoiugam", null, null)); // Game unknown
        }
    }

    /**
     * Create a game on a persistent game spawn service for a user
     * @param socket
     * @param socketData
     */
    public void gpsc(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        SocketWriter.write(socket, socketData);
        GameEntity gameEntity = socketMapper.toGameEntityForCreation(socketData.getInputMessage(), false);
        gameEntity.setVers(socketWrapper.getPersonaConnectionEntity().getVers());
        gameEntity.setSlus(socketWrapper.getPersonaConnectionEntity().getSlus());
        gameRepository.save(gameEntity);
        startGameReport(socketWrapper, gameEntity, false);
        ses(socket, gameEntity, socketWrapper);
    }

    public void updatePlayerListOfHost(GameEntity gameEntity, SocketWrapper socketWrapper) {
        SocketWrapper hostSocketWrapper = SocketManager.getHostSocketWrapperOfGame(gameEntity.getId());
        if(hostSocketWrapper != null) {
            SocketWriter.write(hostSocketWrapper.getSocket(), new SocketData("+mgm", null, getGameInfo(gameEntity, socketWrapper)));
        }
    }

    /**
     * Create a new game with the UHS (User Hosted Server)
     * @param socket
     * @param socketData
     * @param socketWrapper
     */
    public void gcre(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        SocketWriter.write(socket, socketData);

        if(props.isUhsAutoStart()) {
            GameEntity gameEntity = gameRepository.findById(1L).orElse(null);

            if (!props.isUhsEaServerMode()) {
                gameEntity = socketMapper.toGameEntityForCreation(socketData.getInputMessage(), true);
                gameEntity.setVers(socketWrapper.getPersonaConnectionEntity().getVers());
                gameEntity.setSlus(socketWrapper.getPersonaConnectionEntity().getSlus());
                gameRepository.save(gameEntity);
            }

            startGameReport(socketWrapper, gameEntity, true);

            SocketManager.setGameEntity(socket.getRemoteSocketAddress().toString(), gameEntity);

            personaService.who(socket, socketWrapper); // Used to set the game id

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            SocketWriter.write(socket, new SocketData("+mgm", null, getGameInfo(gameEntity, socketWrapper)));
        }
    }

    /**
     * Leave game
     * @param socket
     * @param socketData
     * @param socketWrapper
     */
    public void glea(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        endGameReport(socketWrapper);
        SocketWriter.write(socket, socketData);
    }

    /**
     * Update the status of a persistent game spawn service.
     *
     * If STATUS is "A", then the GPS is available to host a game.
     * If STATUS is "G", then the GPS is hosting a game.
     *
     * @param socket
     * @param socketData
     */
    public void gpss(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        SocketWriter.write(socket, socketData);

        String status = getValueFromSocket(socketData.getInputMessage(), "STATUS");

        // Add a flag in database to indicate that the game is hosted
        if(props.isUhsAutoStart() && props.isUhsEaServerMode() && ("A").equals(status)) {
            //gps(socket, socketData); // Not needed yet
            GameEntity gameEntity = gameRepository.findById(1L).orElse(null);
            SocketWriter.write(socket, new SocketData("$cre", null, getGameInfo(gameEntity, socketWrapper)));
        } else if(props.isUhsAutoStart() && ("G").equals(status)) {
            // We can't send +ses here as we need at least the host + 1 player (COUNT=2) to start a game
        }

    }

    /**
     * Get periodic status from the GPS
     *
     * @param socket
     * @param socketData
     */
    private void gps(Socket socket, SocketData socketData) {
            Map<String, String> content = Stream.of(new String[][] {
                { "PING", "EA60" },
            }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
            socketData.setOutputData(content);
            socketData.setIdMessage("$gps");
            SocketWriter.write(socket, socketData);
    }

    /**
     * Start session
     * @param socket
     * @param gameEntity
     */
    public void ses(Socket socket, GameEntity gameEntity, SocketWrapper socketWrapper) {
        SocketWriter.write(socket, new SocketData("+ses", null, getGameInfo(gameEntity, socketWrapper)));
    }

    /**
     * Game details (current opponents, ...)
     * @param socket
     * @param socketData
     */
    public void gget(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        String ident = getValueFromSocket(socketData.getInputMessage(), "IDENT");
        Optional<GameEntity> gameEntityOpt = gameRepository.findById(Long.valueOf(ident));
        if(gameEntityOpt.isPresent()) {
            GameEntity gameEntity = gameEntityOpt.get();
            SocketWriter.write(socket, new SocketData("gget", null, getGameInfo(gameEntity, socketWrapper)));
        }
    }

    public Map<String, String> getGameInfo(GameEntity gameEntity, SocketWrapper socketWrapper) {

        Long gameId = gameEntity.getId();
        SocketWrapper hostSocketWrapperOfGame = SocketManager.getHostSocketWrapperOfGame(gameId);

        // Workaround when there is no host (serverless patch)
        boolean hasHost = hostSocketWrapperOfGame != null;
        String host = hasHost ? "@" + hostSocketWrapperOfGame.getPersonaEntity().getPers() : "@brobot1";
        int count = gameEntity.getGameReports().stream().filter(report -> null == report.getEndTime()).collect(Collectors.toList()).size();
        count = hasHost ? count : ++count;

        Map<String, String> content = Stream.of(new String[][] {
                { "IDENT", String.valueOf(gameId) },
                { "NAME", gameEntity.getName() },
                { "HOST", host },
                // { "GPSHOST", hostSocketWrapperOfGame.getPers() },
                { "PARAMS", gameEntity.getParams() },
                // { "PARAMS", ",,,b80,d003f6e0656e47423" },
                { "PLATPARAMS", "0" },  // ???
                { "ROOM", "1" },
                { "CUSTFLAGS", "413082880" },
                { "SYSFLAGS", gameEntity.getSysflags() },
                { "COUNT", String.valueOf(count) },
                // { "GPSREGION", "2" },
                { "PRIV", "0" },
                { "MINSIZE", String.valueOf(gameEntity.getMinsize()) },
                { "MAXSIZE", String.valueOf(gameEntity.getMaxsize()) },
                { "NUMPART", "1" },
                { "SEED", "3" }, // random seed
                { "WHEN", DateTimeFormatter.ofPattern("yyyy.M.d-H:mm:ss").format(gameEntity.getStartTime().toLocalDateTime()) },
                // { "GAMEPORT", String.valueOf(props.getUdpPort())},
                // { "VOIPPORT", "9667" },
                // { "GAMEMODE", "0" }, // ???
                { "AUTH", gameEntity.getSysflags().equals("262656") ? "098f6bcd4621d373cade4e832627b4f6" : "" }, // Required for ranked

                // loop 0x80022058 only if COUNT>=0

                // another loop 0x8002225C only if NUMPART>=0
                // { "SELF", "" },

                { "SESS", "0" }, // %s-%s-%08x 0--498ea96f

                { "EVID", "0" },
                { "EVGID", "0" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        int[] idx = { 0 };

        if(!hasHost) {
            content.putAll(Stream.of(new String[][] {
                    { "OPID" + idx[0], "0" },
                    { "OPPO" + idx[0], "@brobot1" },
                    { "ADDR" + idx[0], "127.0.0.1" },
                    { "LADDR" + idx[0], "127.0.0.1" },
                    { "MADDR" + idx[0], "" },
            }).collect(Collectors.toMap(data -> data[0], data -> data[1])));
            idx[0]++;
        }

        gameEntity.getGameReports().stream()
                .filter(report -> null == report.getEndTime())
                .sorted(Comparator.comparing(GameReportEntity::getId))
                .forEach(gameReportEntity -> {
            PersonaEntity personaEntity = gameReportEntity.getPersona();

            List<String> relatedSlus = GameVersUtils.getRelatedSlus(
                    socketWrapper.getPersonaConnectionEntity().getVers(),
                    socketWrapper.getPersonaConnectionEntity().getSlus());

            Optional<PersonaConnectionEntity> personaConnectionEntityOpt =
                    personaConnectionRepository.findByPersonaAndSlusInAndEndTimeIsNull(personaEntity, relatedSlus);
            if(personaConnectionEntityOpt.isPresent()) {
                PersonaConnectionEntity personaConnectionEntity = personaConnectionEntityOpt.get();
                String hostPrefix = gameReportEntity.isHost() ? "@" : "";
                content.putAll(Stream.of(new String[][] {
                        { "OPID" + idx[0], String.valueOf(personaEntity.getId()) },
                        { "OPPO" + idx[0], hostPrefix + personaEntity.getPers() },
                        { "ADDR" + idx[0], personaConnectionEntity.getIp() },
                        { "LADDR" + idx[0], personaConnectionEntity.getIp() },
                        { "MADDR" + idx[0], "" },
                        { "OPPART" + idx[0], "0" },
                        { "OPPARAM" + idx[0], "chgBAMJQAAAVAAAAUkYAAAUAAAABAAAA" },
                        { "OPFLAG" + idx[0], "413082880" },
                        { "OPFLAGS" + idx[0], "413082880" },
                        { "PRES" + idx[0], "0" },
                        { "PARTSIZE" + idx[0], String.valueOf(gameEntity.getMaxsize()) },
                        { "PARTPARAMS" + idx[0], "" },
                }).collect(Collectors.toMap(data -> data[0], data -> data[1])));
                idx[0]++;
            }
        });

        return content;
    }

    /**
     * Registers a game entry
     * @param socketWrapper
     * @param gameEntity
     * @param isHost
     */
    private void startGameReport(SocketWrapper socketWrapper, GameEntity gameEntity, boolean isHost) {
        // Close any game report that wasn't property ended (e.g. use Dolphin save state to leave)
        endGameReport(socketWrapper);

        GameReportEntity gameReportEntity = new GameReportEntity();
        gameReportEntity.setGame(gameEntity);
        gameReportEntity.setPersona(socketWrapper.getPersonaEntity());
        gameReportEntity.setHost(isHost);
        gameReportEntity.setStartTime(Timestamp.from(Instant.now()));
        gameReportRepository.save(gameReportEntity);

        if(gameEntity.getGameReports() == null) {
            gameEntity.setGameReports(new HashSet<>());
        }

        gameEntity.getGameReports().add(gameReportEntity);
        socketWrapper.setGameEntity(gameEntity);
        socketWrapper.setGameReportEntity(gameReportEntity);
    }

    /**
     * Ends the game report because the player has left the game
     */
    public void endGameReport(SocketWrapper socketWrapper) {
        GameReportEntity gameReportEntity = socketWrapper.getGameReportEntity();
        if(gameReportEntity != null) {
            Optional<GameReportEntity> gameReportEntityOptional = gameReportRepository.findById(gameReportEntity.getId());
            if (gameReportEntityOptional.isPresent()) {
                gameReportEntity = gameReportEntityOptional.get();
                if (gameReportEntity.getEndTime() == null) {
                    gameReportEntity.setEndTime(Timestamp.from(Instant.now()));
                    gameReportRepository.save(gameReportEntity);
                }
            }
            socketWrapper.setGameEntity(null);
            socketWrapper.setGameReportEntity(null);
        }
    }

    /**
     * Close expired lobbies
     * If no one is in the game after 2 minutes, close it
     */
    public void closeExpiredLobbies() {
        List<GameEntity> gameEntities = gameRepository.findByEndTimeIsNull();
        gameEntities.forEach(gameEntity -> {
            Set<GameReportEntity> gameReports = gameEntity.getGameReports();
            if(gameReports.stream().noneMatch(report -> null == report.getEndTime())) {
                if(gameReports.stream().allMatch(report -> report.getEndTime().toInstant().plusSeconds(120).isBefore(Instant.now()))) {
                    log.info("Closing expired game: {} - {}", gameEntity.getId(), gameEntity.getName());
                    gameEntity.setEndTime(Timestamp.from(Instant.now()));
                    gameRepository.save(gameEntity);
                }
            }
        });
    }

}
