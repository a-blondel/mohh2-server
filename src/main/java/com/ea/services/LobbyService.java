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
public class LobbyService {

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
                { "I", "0" }, // Room identifier
                { "N", "MyRoom" }, // Room name
                { "H", "" }, // Room Host
                { "D", "" }, // Room description
                { "F", "CK" }, // Attribute flags
                { "T", "" }, // Current room population
                { "L", "33" }, // Max users allowed in room
                { "P", "0" }, // Room ping
                { "A", props.getTcpHost() }, // Room address
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
                SocketWriter.write(socket, socketData);
                ses(socket, sessionData, lobbyEntity);
                mgm(socket, sessionData, lobbyEntity);
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

    public void mgm(Socket socket, SessionData sessionData, LobbyEntity lobbyEntity) {
        SocketWrapper socketWrapper = socketManager.getHostSocketWrapperOfLobby(lobbyEntity.getId());
        if(socketWrapper != null) {
            SocketWriter.write(socketWrapper.getSocket(), new SocketData("+mgm", null, getLobbyInfo(sessionData, lobbyEntity)));
            SocketWriter.write(socketWrapper.getSocket(), new SocketData("+ses", null, getLobbyInfo(sessionData, lobbyEntity)));
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

        if(!props.isEaServer()) {
            LobbyEntity lobbyEntity = socketMapper.toLobbyEntityForCreation(socketData.getInputMessage(), true);
            lobbyRepository.save(lobbyEntity);

            startLobbyReport(sessionData, lobbyEntity);

            //String room = getValueFromSocket(socketData.getInputMessage(), "ROOM"); // Should room be added to the lobby entity?

            socketManager.setLobbyId(socket.getRemoteSocketAddress().toString(), lobbyEntity.getId());

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
     * If STATUS is "A", then the GPS is avaible to host a game.
     * If STATUS is "G", then the GPS is hosting a game.
     *
     * @param socket
     * @param sessionData
     * @param socketData
     */
    public void gpss(Socket socket, SessionData sessionData, SocketData socketData) {
        SocketWriter.write(socket, socketData);

//        Map<String, String> content = Stream.of(new String[][] {
//                { "PING", "EA60" },
//        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
//        socketData.setOutputData(content);
//        socketData.setIdMessage("$gps");

//        String status = getValueFromSocket(socketData.getInputMessage(), "STATUS");

//        if(("A").equals(status)) {
//            SocketWriter.write(socket, socketData);
//            LobbyEntity lobbyEntity = lobbyRepository.findById(1L).orElse(null);
//            SocketWriter.write(socket, new SocketData("$cre", null, getLobbyInfo(sessionData, lobbyEntity)));
//        }

    }

    /**
     * Start session
     * @param socket
     * @param sessionData
     * @param lobbyEntity
     */
    public void ses(Socket socket, SessionData sessionData, LobbyEntity lobbyEntity) {
        startLobbyReport(sessionData, lobbyEntity);
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
        SocketWrapper socketWrapper = socketManager.getHostSocketWrapperOfLobby(lobbyId);

        Map<String, String> content = Stream.of(new String[][] {
                { "IDENT", String.valueOf(lobbyId) },
                { "NAME", lobbyEntity.getName() },
                { "HOST", socketWrapper != null ? socketWrapper.getPers() : "@brobot1" },
                // { "GPSHOST", socketWrapper != null ? socketWrapper.getPers() : "@brobot1" },
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
                // { "AUTH", "0" }, // ???

                // loop 0x80022058 only if COUNT>=0
                { "OPID0", "0" }, // OPID%d
                { "OPPO0", socketWrapper != null ? socketWrapper.getPers() : "@brobot1" }, // OPPO%d
                { "ADDR0", socketWrapper != null ? socketWrapper.getSocket().getInetAddress().getHostAddress() : "127.0.0.1" },
                { "LADDR0", "127.0.0.1" },
                { "OPFLAG0", "0" },
                { "MADDR0", "" }, // MADDR%d
                { "OPPART0", "0" }, // OPPART%d
                { "OPPARAM0", "chgBAMJQAAAVAAAAUkYAAAUAAAABAAAA" }, // OPPARAM%d
                { "OPFLAGS0", "0" }, // OPFLAGS%d
                { "PRES0", "0" }, // PRES%d ???

                // another loop 0x8002225C only if NUMPART>=0
                { "PARTSIZE0", String.valueOf(lobbyEntity.getMaxsize()) }, // PARTSIZE%d
                { "PARTPARAMS0", "" }, // PARTPARAMS%d
                // { "SELF", sessionData.getCurrentPersonna().getPers() },

                { "SESS", "0" }, // %s-%s-%08x 0--498ea96f

                { "EVID", "0" },
                { "EVGID", "0" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        int[] idx = { 1 };
        lobbyEntity.getLobbyReports().stream().filter(report -> null == report.getEndTime()).forEach(lobbyReportEntity -> {
            PersonaEntity personaEntity = lobbyReportEntity.getPersona();
            Optional<PersonaConnectionEntity> personaConnectionEntityOpt = personaConnectionRepository.findCurrentPersonaConnection(personaEntity);
            if(personaConnectionEntityOpt.isPresent()) {
                PersonaConnectionEntity personaConnectionEntity = personaConnectionEntityOpt.get();
                content.putAll(Stream.of(new String[][] {
                        { "OPID" + idx[0], String.valueOf(idx[0]) },
                        { "OPPO" + idx[0], personaEntity.getPers() },
                        { "ADDR" + idx[0], personaConnectionEntity.getIp() },
                        { "LADDR" + idx[0], personaConnectionEntity.getIp() },
                        { "MADDR" + idx[0], "" },
                        { "OPPART" + idx[0], "0" },
                        { "OPPARAM" + idx[0], "chgBAMJQAAAVAAAAUkYAAAUAAAABAAAA" },
                        { "OPFLAGS" + idx[0], "413082880" },
                        { "PRES" + idx[0], "0" },
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
    private void startLobbyReport(SessionData sessionData, LobbyEntity lobbyEntity) {
        // Close any lobby report that wasn't property ended (e.g. use Dolphin save state to leave)
        endLobbyReport(sessionData);

        LobbyReportEntity lobbyReportEntity = new LobbyReportEntity();
        lobbyReportEntity.setLobby(lobbyEntity);
        lobbyReportEntity.setPersona(sessionData.getCurrentPersonna());
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
