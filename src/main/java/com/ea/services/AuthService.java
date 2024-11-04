package com.ea.services;

import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.repositories.GameReportRepository;
import com.ea.steps.SocketWriter;
import com.ea.utils.GameVersUtils;
import com.ea.utils.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ea.utils.SocketUtils.SPACE_CHAR;
import static com.ea.utils.SocketUtils.getValueFromSocket;

@Component
public class AuthService {

    @Autowired
    Props props;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameReportRepository gameReportRepository;

    public void dir(Socket socket, SocketData socketData) {
        String slus = getValueFromSocket(socketData.getInputMessage(), "SLUS");

        Map<String, String> content = Stream.of(new String[][] {
                // { "DIRECT", "0" }, // 0x8001FC04
                // if DIRECT == 0 then read ADDR and PORT
                { "ADDR", props.getTcpHost() }, // 0x8001FC18
                { "PORT", String.valueOf(GameVersUtils.getTcpPort(slus)) }, // 0x8001fc30
                // { "SESS", "0" }, // 0x8001fc48 %s-%s-%08x 0--498ea96f
                // { "MASK", "0" }, // 0x8001fc60
                // if ADDR == 0 then read DOWN
                // { "DOWN", "0" }, // 0x8001FC90
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);
    }

    public void addr(Socket socket, SocketData socketData) {
        SocketWriter.write(socket, socketData);
    }

    public void skey(Socket socket, SocketData socketData) {
        Map<String, String> content = Stream.of(new String[][] {
                { "SKEY", "$51ba8aee64ddfacae5baefa6bf61e009" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);
    }

    public void news(Socket socket, SocketData socketData) {
        Map<String, String> content = Stream.of(new String[][] {
                { "BUDDY_SERVER", props.getTcpHost() },
                { "BUDDY_PORT", String.valueOf(11192) },
                { "TOSAC_URL", "https://tos.ea.com/legalapp/webterms/us/fr/pc/" },
                { "TOSA_URL", "https://tos.ea.com/legalapp/webterms/us/fr/pc/" },
                { "TOS_URL", "https://tos.ea.com/legalapp/webterms/us/fr/pc/" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);
    }

    public void sele(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        String stats = getValueFromSocket(socketData.getInputMessage(), "STATS");
        String inGame = getValueFromSocket(socketData.getInputMessage(), "INGAME");

        Map<String, String> content;
        // Request separates attributes either by 0x20 or 0x0a...
        if(null == stats && null == inGame) { // If both NULL, then the separator is 0x20, so we know which request was sent
            content = Stream.of(new String[][] {
                    { "MORE", "0" },
                    { "SLOTS", "4" },
                    { "STATS", "0" },
//                    { "GAMES", "1" },
//                    { "ROOMS", "1" },
//                    { "USERS", "1" },
//                    { "MESGS", "1" },
//                    { "MYGAME", "1" },
//                    { "ASYNC", "1" },
            }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        } else {
            String myGame = getValueFromSocket(socketData.getInputMessage(), "MYGAME");
            String async = getValueFromSocket(socketData.getInputMessage(), "ASYNC");

            if ("1".equals(inGame)) {
                String games = getValueFromSocket(socketData.getInputMessage(), "GAMES");
                String rooms = getValueFromSocket(socketData.getInputMessage(), "ROOMS");
                String mesgs = getValueFromSocket(socketData.getInputMessage(), "MESGS");
                String mesgTypes = getValueFromSocket(socketData.getInputMessage(), "MESGTYPES");
                String users = getValueFromSocket(socketData.getInputMessage(), "USERS");
                String userSets = getValueFromSocket(socketData.getInputMessage(), "USERSETS");
                content = Stream.of(new String[][] {
                        { "INGAME", inGame },
                        { "MESGS", mesgs },
                        { "MESGTYPES", mesgTypes },
                        { "USERS", users },
                        { "GAMES", games },
                        { "MYGAME", myGame },
                        { "ROOMS", rooms },
                        { "ASYNC", async },
                        { "USERSETS", userSets },
                        { "STATS", stats },
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
            } else {
                content = Stream.of(new String[][] {
                        { "INGAME", inGame },
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
            }
        }

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData, SPACE_CHAR);

        if(null != stats || null != inGame) {
            personaService.who(socket, socketWrapper);
        }

        if(socketWrapper != null && socketWrapper.isHost()) {
            joinRoom(socket, socketData, socketWrapper);
        }

    }

    private void joinRoom(Socket socket, SocketData socketData, SocketWrapper socketWrapper) {
        personaService.who(socket, socketWrapper); // Used to set the room info
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        gameService.rom(socket, socketData);
    }

}
