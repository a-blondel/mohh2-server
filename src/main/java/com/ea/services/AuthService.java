package com.ea.services;

import com.ea.dto.SessionData;
import com.ea.dto.SocketData;
import com.ea.steps.SocketWriter;
import com.ea.utils.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ea.utils.SocketUtils.getValueFromSocket;

@Component
public class AuthService {

    @Autowired
    Props props;

    @Autowired
    private PersonaService personaService;

    public void dir(Socket socket, SocketData socketData) {
        Map<String, String> content = Stream.of(new String[][] {
                // { "DIRECT", "0" }, // 0x8001FC04
                // if DIRECT == 0 then read ADDR and PORT
                { "ADDR", props.getTcpHost() }, // 0x8001FC18
                { "PORT", String.valueOf(props.getTcpPort()) }, // 0x8001fc30
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
                { "NEWS_URL", "http://gos.ea.com/easo/editorial/common/2008/news/news.jsp?lang=us&from=us&game=Burnout&platform=pc" },
                { "LIVE_NEWS_URL", "http://gos.ea.com/easo/editorial/Burnout/2008/livedata/main.jsp?lang=us&from=us&game=Burnout&platform=pc&env=live&nToken=1" },
                { "LIVE_NEWS2_URL", "http://gos.ea.com/easo/editorial/Burnout/2008/livedata/main.jsp?lang=us&from=us&game=Burnout&platform=pc&env=live&nToken=1" },
                { "STORE_DLC_URL", "http://pctrial.burnoutweb.ea.com/pcstore/store_dlc.php?lang=us&from=us&game=Burnout&platform=pc&env=live&nToken=%s&prodid=1" },
                { "STORE_URL", "http://pctrial.burnoutweb.ea.com/t2b/page/index.php?lang=us&from=us&game=Burnout&platform=pc&env=live&nToken=1" },
                { "TOSAC_URL", "http://gos.ea.com/easo/editorial/common/2008/tos/tos.jsp?style=accept&lang=us&platform=pc&from=us" },
                { "TOSA_URL", "http://gos.ea.com/easo/editorial/common/2008/tos/tos.jsp?style=view&lang=us&platform=pc&from=us" },
                { "TOS_URL", "http://gos.ea.com/easo/editorial/common/2008/tos/tos.jsp?lang=us&platform=pc&from=us" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        socketData.setOutputData(content);
        SocketWriter.write(socket, socketData);
    }

    public void sele(Socket socket, SessionData sessionData, SocketData socketData) {
        String stats = getValueFromSocket(socketData.getInputMessage(), "STATS");
        String inGame = getValueFromSocket(socketData.getInputMessage(), "INGAME");

        Map<String, String> content;
        // Request separates attributes either by 0x20 or 0x0a...
        if(null == stats && null == inGame) { // If both NULL, then the separator is 0x20, so we know which request was sent
            content = Stream.of(new String[][] {
                    { "MORE", "0" },
                    { "SLOTS", "4" },
                    { "STATS", "0" },
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
        SocketWriter.write(socket, socketData);

        if(null != stats || null != inGame) {
            personaService.who(socket, sessionData);
        }
    }

}
