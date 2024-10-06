package com.ea.services;

import com.ea.dto.SessionData;
import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.entities.LobbyEntity;
import com.ea.entities.LobbyReportEntity;
import com.ea.entities.PersonaConnectionEntity;
import com.ea.entities.PersonaEntity;
import com.ea.mappers.SocketMapper;
import com.ea.repositories.LobbyReportRepository;
import com.ea.repositories.LobbyRepository;
import com.ea.repositories.PersonaConnectionRepository;
import com.ea.steps.SocketWriter;
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
    private LobbyRepository lobbyRepository;

    @Autowired
    private LobbyReportRepository lobbyReportRepository;

    @Autowired
    private PersonaConnectionRepository personaConnectionRepository;

    @Autowired
    private SocketMapper socketMapper;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private SocketManager socketManager;

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

    public void gsta(Socket socket, SessionData sessionData, SocketData socketData) {
        SocketWriter.write(socket, socketData);
    }

    /**
     * Game count
     * @param socket
     * @param socketData
     */
    public void gsea(Socket socket, SocketData socketData) {
        List<LobbyEntity> lobbyEntities = lobbyRepository.findByEndTime(null);

        Map<String, String> content = Stream.of(new String[][] {
                { "COUNT", String.valueOf(lobbyEntities.size()) },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);

        gam(socket, lobbyEntities);
    }

    /**
     * List games
     * @param socket
     */
    public void gam(Socket socket, List<LobbyEntity> lobbyEntities) {
        List<Map<String, String>> lobbies = new ArrayList<>();

        for(LobbyEntity lobbyEntity : lobbyEntities) {
            lobbies.add(Stream.of(new String[][] {
                    { "IDENT", String.valueOf(lobbyEntity.getId()) },
                    { "NAME", lobbyEntity.getName() },
                    { "PARAMS", lobbyEntity.getParams() },
                    { "SYSFLAGS", lobbyEntity.getSysflags() },
                    { "COUNT", String.valueOf(lobbyEntity.getLobbyReports().stream().filter(report -> null == report.getEndTime()).count()) },
                    { "MAXSIZE", String.valueOf(lobbyEntity.getMaxsize()) },
            }).collect(Collectors.toMap(data -> data[0], data -> data[1])));
        }

        for (Map<String, String> lobby : lobbies) {
            SocketData socketData = new SocketData("+gam", null, lobby);
            SocketWriter.write(socket, socketData);
        }
    }

    /**
     * Join a game
     * @param socket
     * @param socketData
     */
    public void gjoi(Socket socket, SessionData sessionData, SocketData socketData) {
        String ident = getValueFromSocket(socketData.getInputMessage(), "IDENT");
        Optional<LobbyEntity> lobbyEntityOpt = lobbyRepository.findById(Long.valueOf(ident));
        if(lobbyEntityOpt.isPresent()) {
            LobbyEntity lobbyEntity = lobbyEntityOpt.get();
            if(lobbyEntity.getEndTime() == null) {
                startLobbyReport(sessionData, lobbyEntity, false);

                SocketWriter.write(socket, socketData);

                refreshHostInfo(socket, sessionData, lobbyEntity);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                ses(socket, sessionData, lobbyEntity);
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
     * @param sessionData
     * @param socketData
     */
    public void gpsc(Socket socket, SessionData sessionData, SocketData socketData) {
        SocketWriter.write(socket, socketData);
        LobbyEntity lobbyEntity = socketMapper.toLobbyEntityForCreation(socketData.getInputMessage(), false);
        lobbyRepository.save(lobbyEntity);
        ses(socket, sessionData, lobbyEntity);
    }

    public void refreshHostInfo(Socket socket, SessionData sessionData, LobbyEntity lobbyEntity) {
        SocketWrapper hostSocketWrapper = socketManager.getHostSocketWrapperOfLobby(lobbyEntity.getId());
        if(hostSocketWrapper != null) {
            SocketWriter.write(hostSocketWrapper.getSocket(), new SocketData("+mgm", null, getLobbyInfo(sessionData, lobbyEntity)));
            SocketWriter.write(hostSocketWrapper.getSocket(), new SocketData("+ses", null, getLobbyInfo(sessionData, lobbyEntity)));
        }
    }

    /**
     * Create a new game with the UHS (User Hosted Server)
     * @param socket
     * @param sessionData
     * @param socketData
     */
    public void gcre(Socket socket, SessionData sessionData, SocketData socketData) {
        SocketWriter.write(socket, socketData);

        if(props.isUhsAutoStart()) {
            LobbyEntity lobbyEntity = lobbyRepository.findById(1L).orElse(null);

            if (!props.isUhsEaServerMode()) {
                //String room = getValueFromSocket(socketData.getInputMessage(), "ROOM"); // Should room be added to the lobby entity?
                lobbyEntity = socketMapper.toLobbyEntityForCreation(socketData.getInputMessage(), true);
                lobbyRepository.save(lobbyEntity);
            }

            startLobbyReport(sessionData, lobbyEntity, true);

            socketManager.setGameId(socket.getRemoteSocketAddress().toString(), lobbyEntity.getId());

            personaService.who(socket, sessionData); // Used to set the game id

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            SocketWriter.write(socket, new SocketData("+mgm", null, getLobbyInfo(sessionData, lobbyEntity)));
        }
    }

    /**
     * Leave game
     * @param socket
     * @param sessionData
     * @param socketData
     */
    public void glea(Socket socket, SessionData sessionData, SocketData socketData) {
        endLobbyReport(sessionData);
        SocketWriter.write(socket, socketData);
    }

    /**
     * Update the status of a persistent game spawn service.
     *
     * If STATUS is "A", then the GPS is available to host a game.
     * If STATUS is "G", then the GPS is hosting a game.
     *
     * @param socket
     * @param sessionData
     * @param socketData
     */
    public void gpss(Socket socket, SessionData sessionData, SocketData socketData) {
        SocketWriter.write(socket, socketData);

        String status = getValueFromSocket(socketData.getInputMessage(), "STATUS");

        // Add a flag in database to indicate that the game is hosted
        if(props.isUhsAutoStart() && props.isUhsEaServerMode() && ("A").equals(status)) {
            //gps(socket, sessionData, socketData); // Not needed yet
            LobbyEntity lobbyEntity = lobbyRepository.findById(1L).orElse(null);
            SocketWriter.write(socket, new SocketData("$cre", null, getLobbyInfo(sessionData, lobbyEntity)));
        } else if(props.isUhsAutoStart() && ("G").equals(status)) {
            // We can't send +ses here as we need at least the host + 1 player (COUNT=2) to start a game
        }

    }

    /**
     * Get periodic status from the GPS
     *
     * @param socket
     * @param sessionData
     * @param socketData
     */
    private void gps(Socket socket, SessionData sessionData, SocketData socketData) {
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
     * @param sessionData
     * @param lobbyEntity
     */
    public void ses(Socket socket, SessionData sessionData, LobbyEntity lobbyEntity) {
        SocketWriter.write(socket, new SocketData("+ses", null, getLobbyInfo(sessionData, lobbyEntity)));
    }

    /**
     * Game details (current opponents, ...)
     * @param socket
     */
    public void gget(Socket socket, SessionData sessionData, SocketData socketData) {
        String ident = getValueFromSocket(socketData.getInputMessage(), "IDENT");
        Optional<LobbyEntity> lobbyEntityOpt = lobbyRepository.findById(Long.valueOf(ident));
        if(lobbyEntityOpt.isPresent()) {
            LobbyEntity lobbyEntity = lobbyEntityOpt.get();
            SocketWriter.write(socket, new SocketData("gget", null, getLobbyInfo(sessionData, lobbyEntity)));
        }
    }

    public Map<String, String> getLobbyInfo(SessionData sessionData, LobbyEntity lobbyEntity) {
        String params = lobbyEntity.getParams();

        // MOHH2
//        int serverPortPos = StringUtils.ordinalIndexOf(params, ",", 20);
//        StringBuilder sb = new StringBuilder(params);
//        sb.insert(serverPortPos, Integer.toHexString(props.getUdpPort())); // Set game server port
//        params = sb.toString();

        // MOHH
//        int serverPortPos = StringUtils.ordinalIndexOf(params, ",", 11);
//        StringBuilder sb = new StringBuilder(params);
//        sb.replace(serverPortPos - 1, serverPortPos, Integer.toHexString(props.getUdpPort())); // Set game server port
//        params = sb.toString();
//
//        log.info("params: {}", params);

        Long lobbyId = lobbyEntity.getId();
        SocketWrapper hostSocketWrapperOfLobby = socketManager.getHostSocketWrapperOfLobby(lobbyId);

        Map<String, String> content = Stream.of(new String[][] {
                { "IDENT", String.valueOf(lobbyId) },
                { "NAME", lobbyEntity.getName() },
                { "HOST", hostSocketWrapperOfLobby.getPers() },
                // { "GPSHOST", hostSocketWrapperOfLobby.getPers() },
                { "PARAMS", params },
                // { "PARAMS", ",,,b80,d003f6e0656e47423" },
                { "PLATPARAMS", "0" },  // ???
                { "ROOM", "1" },
                { "CUSTFLAGS", "413082880" },
                { "SYSFLAGS", lobbyEntity.getSysflags() },
                { "COUNT", String.valueOf(lobbyEntity.getLobbyReports().stream().filter(report -> null == report.getEndTime()).count()) },
                // { "GPSREGION", "2" },
                { "PRIV", "0" },
                { "MINSIZE", String.valueOf(lobbyEntity.getMinsize()) },
                { "MAXSIZE", String.valueOf(lobbyEntity.getMaxsize()) },
                { "NUMPART", "1" },
                { "SEED", "3" }, // random seed
                { "WHEN", DateTimeFormatter.ofPattern("yyyy.M.d-H:mm:ss").format(lobbyEntity.getStartTime().toLocalDateTime()) },
                // { "GAMEPORT", String.valueOf(props.getUdpPort())},
                // { "VOIPPORT", "9667" },
                // { "GAMEMODE", "0" }, // ???
                // { "AUTH", "098f6bcd4621d373cade4e832627b4f6" },

                // loop 0x80022058 only if COUNT>=0

                // another loop 0x8002225C only if NUMPART>=0
                // { "SELF", sessionData.getCurrentPersonna().getPers() },

                { "SESS", "0" }, // %s-%s-%08x 0--498ea96f

                { "EVID", "0" },
                { "EVGID", "0" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        int[] idx = { 0 };
        lobbyEntity.getLobbyReports().stream()
                .filter(report -> null == report.getEndTime())
                .sorted(Comparator.comparing(LobbyReportEntity::getId))
                .forEach(lobbyReportEntity -> {
            PersonaEntity personaEntity = lobbyReportEntity.getPersona();
            Optional<PersonaConnectionEntity> personaConnectionEntityOpt = personaConnectionRepository.findCurrentPersonaConnection(personaEntity);
            if(personaConnectionEntityOpt.isPresent()) {
                PersonaConnectionEntity personaConnectionEntity = personaConnectionEntityOpt.get();
                String hostPrefix = lobbyReportEntity.isHost() ? "@" : "";
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
                        { "PARTSIZE" + idx[0], String.valueOf(lobbyEntity.getMaxsize()) },
                        { "PARTPARAMS" + idx[0], "" },
                }).collect(Collectors.toMap(data -> data[0], data -> data[1])));
                idx[0]++;
            }
        });

        return content;
    }

    /**
     * Registers a lobby entry
     * @param lobbyEntity
     */
    private void startLobbyReport(SessionData sessionData, LobbyEntity lobbyEntity, boolean isHost) {
        // Close any lobby report that wasn't property ended (e.g. use Dolphin save state to leave)
        endLobbyReport(sessionData);

        LobbyReportEntity lobbyReportEntity = new LobbyReportEntity();
        lobbyReportEntity.setLobby(lobbyEntity);
        lobbyReportEntity.setPersona(sessionData.getCurrentPersonna());
        lobbyReportEntity.setHost(isHost);
        lobbyReportEntity.setStartTime(Timestamp.from(Instant.now()));
        lobbyReportRepository.save(lobbyReportEntity);

        if(lobbyEntity.getLobbyReports() == null) {
            lobbyEntity.setLobbyReports(new HashSet<>());
        }

        lobbyEntity.getLobbyReports().add(lobbyReportEntity);
        sessionData.setCurrentLobby(lobbyEntity);
        sessionData.setCurrentLobbyReport(lobbyReportEntity);
    }

    /**
     * Ends the lobby report because the player has left the lobby
     */
    public void endLobbyReport(SessionData sessionData) {
        if(sessionData != null) {
            LobbyReportEntity lobbyReportEntity = sessionData.getCurrentLobbyReport();
            if (lobbyReportEntity != null) {
                lobbyReportEntity.setEndTime(Timestamp.from(Instant.now()));
                lobbyReportRepository.save(lobbyReportEntity);
                sessionData.setCurrentLobby(null);
                sessionData.setCurrentLobbyReport(null);
            }
        } else {
            log.error("sessionData is null ?!");
        }
    }

    /**
     * Close expired lobbies
     * If no one is in the lobby after 2 minutes, close it
     */
    public void closeExpiredLobbies() {
        List<LobbyEntity> lobbyEntities = lobbyRepository.findByEndTime(null);
        lobbyEntities.forEach(lobbyEntity -> {
            Set<LobbyReportEntity> lobbyReports = lobbyEntity.getLobbyReports();
            if(lobbyReports.stream().noneMatch(report -> null == report.getEndTime())) {
                if(lobbyReports.stream().allMatch(report -> report.getEndTime().toInstant().plusSeconds(120).isBefore(Instant.now()))) {
                    log.info("Closing expired lobby: {} - {}", lobbyEntity.getId(), lobbyEntity.getName());
                    lobbyEntity.setEndTime(Timestamp.from(Instant.now()));
                    lobbyRepository.save(lobbyEntity);
                }
            }
        });
    }

}
