package com.ea.services;

import com.ea.dto.SessionData;
import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.entities.AccountEntity;
import com.ea.entities.PersonaConnectionEntity;
import com.ea.entities.PersonaEntity;
import com.ea.entities.PersonaStatsEntity;
import com.ea.repositories.PersonaConnectionRepository;
import com.ea.repositories.PersonaRepository;
import com.ea.repositories.PersonaStatsRepository;
import com.ea.steps.SocketWriter;
import com.ea.utils.AccountUtils;
import com.ea.utils.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ea.utils.SocketUtils.getValueFromSocket;

@Slf4j
@Component
public class PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PersonaConnectionRepository personaConnectionRepository;

    @Autowired
    private PersonaStatsRepository personaStatsRepository;

    @Autowired
    private SocketManager socketManager;

    /**
     * Persona creation
     * @param socket
     * @param sessionData
     * @param socketData
     */
    public void cper(Socket socket, SessionData sessionData, SocketData socketData) {
        String pers = getValueFromSocket(socketData.getInputMessage(), "PERS");

        Optional<PersonaEntity> personaEntityOpt = personaRepository.findByPers(pers);
        if (personaEntityOpt.isPresent()) {
            socketData.setIdMessage("cperdupl"); // Duplicate persona error (EC_DUPLICATE)
            int alts = Integer.parseInt(getValueFromSocket(socketData.getInputMessage(), "ALTS"));
            if (alts > 0) {
                String opts = AccountUtils.suggestNames(alts, pers);
                Map<String, String> content = Stream.of(new String[][]{
                        { "OPTS", opts }
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
                socketData.setOutputData(content);
            }
        } else {
            PersonaEntity personaEntity = new PersonaEntity();
            personaEntity.setAccount(sessionData.getCurrentAccount());
            personaEntity.setPers(pers);
            personaEntity.setCreatedOn(Timestamp.from(Instant.now()));

            PersonaStatsEntity personaStatsEntity = new PersonaStatsEntity();
            personaStatsEntity.setPersona(personaEntity);
            personaEntity.setPersonaStats(personaStatsEntity);

            personaRepository.save(personaEntity);
        }

        SocketWriter.write(socket, socketData);
    }

    /**
     * Get persona
     * @param socket
     * @param socketData
     */
    public void pers(Socket socket, SessionData sessionData, SocketData socketData) {
        String pers = getValueFromSocket(socketData.getInputMessage(), "PERS");
        socketManager.setPers(socket.getRemoteSocketAddress().toString(), pers);

        if(pers.contains("@")) { // Remove @ from persona name (UHS naming convention)
            pers = pers.split("@")[0] + pers.split("@")[1];
        }

        Optional<PersonaEntity> personaEntityOpt = personaRepository.findByPers(pers);
        if (personaEntityOpt.isPresent()) {
            PersonaEntity personaEntity = personaEntityOpt.get();
            sessionData.setCurrentPersonna(personaEntity);
            Map<String, String> content = Stream.of(new String[][] {
                    { "PERS", personaEntity.getPers() },
                    { "LKEY", "" },
                    { "EX-ticker", "" },
                    { "LOC", personaEntity.getAccount().getLoc() },
                    { "A", socket.getInetAddress().getHostAddress() },
                    { "LA", socket.getInetAddress().getHostAddress() },
                    { "IDLE", "35000" },
            }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

            socketData.setOutputData(content);
            SocketWriter.write(socket, socketData);

            startPersonaConnection(socket, sessionData, personaEntity);
        }
    }

    /**
     * Registers a connection of the persona
     * @param socket
     * @param sessionData
     * @param personaEntity
     */
    private void startPersonaConnection(Socket socket, SessionData sessionData, PersonaEntity personaEntity) {
        // Close current connection if the user got a "soft" disconnection (TCP connection is still active)
        if(null != sessionData.getCurrentPersonaConnection()) {
            log.error("User wasn't properly disconnected");
            endPersonaConnection(sessionData);
        }
        Optional<PersonaConnectionEntity> personaConnectionEntityOpt = personaConnectionRepository.findCurrentPersonaConnection(personaEntity);
        if(personaConnectionEntityOpt.isPresent()) {
            PersonaConnectionEntity personaConnectionEntity = personaConnectionEntityOpt.get();
            personaConnectionEntity.setEndTime(Timestamp.from(Instant.now()));
            personaConnectionRepository.save(personaConnectionEntity);
        }
        PersonaConnectionEntity personaConnectionEntity = new PersonaConnectionEntity();
        personaConnectionEntity.setIp(SocketUtils.handleLocalhostIp(socket.getInetAddress().getHostAddress()));
        personaConnectionEntity.setPersona(personaEntity);
        personaConnectionEntity.setStartTime(Timestamp.from(Instant.now()));
        personaConnectionRepository.save(personaConnectionEntity);
        sessionData.setCurrentPersonaConnection(personaConnectionEntity);
    }

    /**
     * Ends the current connection of the persona
     */
    public void endPersonaConnection(SessionData sessionData) {
        PersonaConnectionEntity personaConnectionEntity = sessionData.getCurrentPersonaConnection();
        if(null != personaConnectionEntity) {
            personaConnectionEntity.setEndTime(Timestamp.from(Instant.now()));
            personaConnectionRepository.save(personaConnectionEntity);
        }
    }

    /**
     * Delete persona
     * @param socket
     * @param socketData
     */
    public void dper(Socket socket, SocketData socketData) {
        String pers = getValueFromSocket(socketData.getInputMessage(), "PERS");
        Optional<PersonaEntity> personaEntityOpt = personaRepository.findByPers(pers);
        if (personaEntityOpt.isPresent()) {
            PersonaEntity personaEntity = personaEntityOpt.get();
            personaEntity.setDeletedOn(Timestamp.from(Instant.now()));
            personaRepository.save(personaEntity);
        }
        SocketWriter.write(socket, socketData);
    }

    public void llvl(Socket socket, SessionData sessionData, SocketData socketData) {
        Map<String, String> content = Stream.of(new String[][] {
                { "SKILL_PTS", "211" },
                { "SKILL_LVL", "1049601" },
                { "SKILL", "" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);

        who(socket, sessionData);
    }

    public void who(Socket socket, SessionData sessionData) {
        PersonaEntity personaEntity = sessionData.getCurrentPersonna();
        AccountEntity accountEntity = sessionData.getCurrentAccount();

        Optional<PersonaStatsEntity> personaStatsEntityOpt = personaStatsRepository.findByPersonaId(personaEntity.getId());
        if (personaStatsEntityOpt.isPresent()) {
            PersonaStatsEntity personaStatsEntity = personaStatsEntityOpt.get();

            SocketWrapper socketWrapper = socketManager.getSocketWrapper(socket.getRemoteSocketAddress().toString());
            Long gameId = socketWrapper.getGameId();
            String hostPrefix = socketWrapper.isHost() ? "@" : "";

            Map<String, String> content = Stream.of(new String[][] {
                    { "I", String.valueOf(accountEntity.getId()) },
                    { "M", hostPrefix + accountEntity.getName() },
                    { "N", hostPrefix + personaEntity.getPers() },
                    { "F", "U" },
                    { "P", "40" },
                    // Stats : kills (in hex) at 8th position, deaths (in hex) at 9th
                    { "S", ",,,,,,," + Long.toHexString(personaStatsEntity.getTotalKills()) + "," + Long.toHexString(personaStatsEntity.getTotalDeaths()) },
                    { "X", "0" },
                    { "G", gameId != null ? String.valueOf(gameId) : "0" },
                    { "AT", "" },
                    { "CL", "511" },
                    { "LV", "1049601" },
                    { "MD", "0" },
                    { "R", String.valueOf(personaStatsRepository.getRankByPersonaId(personaStatsEntity.getPersona().getId())) }, // Rank (in decimal)
                    { "US", "0" },
                    { "HW", "0" },
                    { "RP", String.valueOf(personaEntity.getRp()) }, // Reputation (0 to 5 stars)
                    { "LO", sessionData.getCurrentAccount().getLoc() }, // Locale (used to display country flag)
                    { "CI", "0" },
                    { "CT", "0" },
                    // 0x800225E0
                    { "A", socket.getInetAddress().getHostAddress() },
                    { "LA", socket.getInetAddress().getHostAddress() },
                    // 0x80021384
                    { "C", "4000,,7,1,1,,1,1,5553" },
                    { "RI", "1" },
                    { "RT", "1" },
                    { "RG", "0" },
                    { "RGC", "0" },
                    // 0x80021468 if RI != ?? then read RM and RF
                    { "RM", "room" },
                    { "RF", "C" },
            }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
            SocketWriter.write(socket, new SocketData("+who", null, content));
        }
    }

}
