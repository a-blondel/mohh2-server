package com.ea.services;

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

    /**
     * Persona creation
     * @param socket
     * @param socketData
     * @param socketWrapper
     */
    public void cper(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
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
            personaEntity.setAccount(socketWrapper.getAccountEntity());
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
     * @param socketWrapper
     */
    public void pers(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        String pers = getValueFromSocket(socketData.getInputMessage(), "PERS");
        SocketManager.setHost(socket.getRemoteSocketAddress().toString(), pers.contains("@"));

        if(pers.contains("@")) { // Remove @ from persona name (UHS naming convention)
            pers = pers.split("@")[0] + pers.split("@")[1];
        }

        Optional<PersonaEntity> personaEntityOpt = personaRepository.findByPers(pers);
        if (personaEntityOpt.isPresent()) {
            PersonaEntity personaEntity = personaEntityOpt.get();
            socketWrapper.setPersonaEntity(personaEntity);
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

            startPersonaConnection(socket, socketWrapper, personaEntity);
        }
    }

    /**
     * Registers a connection of the persona
     * @param socket
     * @param socketWrapper
     */
    private void startPersonaConnection(Socket socket, SocketWrapper socketWrapper, PersonaEntity personaEntity) {
        // Close current connection if the user got a "soft" disconnection (TCP connection is still active)
        Optional<PersonaConnectionEntity> personaConnectionEntityOpt = personaConnectionRepository.findCurrentPersonaConnection(personaEntity);
        if(personaConnectionEntityOpt.isPresent()) {
            PersonaConnectionEntity personaConnectionEntity = personaConnectionEntityOpt.get();
            personaConnectionEntity.setEndTime(Timestamp.from(Instant.now()));
            personaConnectionRepository.save(personaConnectionEntity);
        }
        PersonaConnectionEntity personaConnectionEntity = socketWrapper.getPersonaConnectionEntity();
        personaConnectionEntity.setPersona(personaEntity);
        personaConnectionRepository.save(personaConnectionEntity);
    }

    /**
     * Ends the current connection of the persona
     */
    public void endPersonaConnection(SocketWrapper socketWrapper) {
        PersonaConnectionEntity personaConnectionEntity = socketWrapper.getPersonaConnectionEntity();
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

    public void llvl(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        Map<String, String> content = Stream.of(new String[][] {
                { "SKILL_PTS", "211" },
                { "SKILL_LVL", "1049601" },
                { "SKILL", "" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);

        who(socket, socketWrapper);
    }

    /**
     * Send a user update record for the current logged in user.
     * @param socket
     * @param socketWrapper
     */
    public void who(Socket socket, SocketWrapper socketWrapper) {
        PersonaEntity personaEntity = socketWrapper.getPersonaEntity();
        AccountEntity accountEntity = socketWrapper.getAccountEntity();

        Optional<PersonaStatsEntity> personaStatsEntityOpt = personaStatsRepository.findByPersonaId(personaEntity.getId());
        if (personaStatsEntityOpt.isPresent()) {
            PersonaStatsEntity personaStatsEntity = personaStatsEntityOpt.get();

            Long gameId = null != socketWrapper.getGameEntity() && null != socketWrapper.getGameEntity().getId() ?
                    socketWrapper.getGameEntity().getId() : 0L;
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
                    { "G", String.valueOf(gameId) },
                    { "AT", "" },
                    { "CL", "511" },
                    { "LV", "1049601" },
                    { "MD", "0" },
                    { "R", String.valueOf(personaStatsRepository.getRankByPersonaId(personaStatsEntity.getPersona().getId())) }, // Rank (in decimal)
                    { "US", "0" },
                    { "HW", "0" },
                    { "RP", String.valueOf(personaEntity.getRp()) }, // Reputation (0 to 5 stars)
                    { "LO", accountEntity.getLoc() }, // Locale (used to display country flag)
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
