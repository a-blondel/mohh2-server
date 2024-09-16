package com.ea.steps;

import com.ea.dto.SessionData;
import com.ea.dto.SocketData;
import com.ea.services.*;
import com.ea.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;

@Slf4j
public class SocketProcessor {
    private static AuthService authService = BeanUtil.getBean(AuthService.class);
    private static AccountService accountService = BeanUtil.getBean(AccountService.class);
    private static PersonaService personaService = BeanUtil.getBean(PersonaService.class);
    private static StatsService statsService = BeanUtil.getBean(StatsService.class);
    private static LobbyService lobbyService = BeanUtil.getBean(LobbyService.class);

    /**
     * Dispatch to appropriate service based on request type
     * @param socket the socket to handle
     * @param socketData the object to process
     */
    public static void process(Socket socket, SessionData sessionData, SocketData socketData) {
        switch (socketData.getIdMessage()) {
            case ("@tic"), ("~png"):
                break;
            case ("@dir"):
                authService.dir(socket, socketData);
                break;
            case ("addr"):
                authService.addr(socket, socketData);
                break;
            case ("skey"):
                authService.skey(socket, socketData);
                break;
            case ("news"):
                authService.news(socket, socketData);
                break;
            case ("sele"):
                authService.sele(socket, sessionData, socketData);
                break;
            case ("acct"):
                accountService.acct(socket, socketData);
                break;
            case ("edit"):
                accountService.edit(socket, socketData);
                break;
            case ("auth"):
                accountService.auth(socket, sessionData, socketData);
                break;
            case ("cper"):
                personaService.cper(socket, sessionData, socketData);
                break;
            case ("pers"):
                personaService.pers(socket, sessionData, socketData);
                break;
            case ("dper"):
                personaService.dper(socket, socketData);
                break;
            case ("llvl"):
                personaService.llvl(socket, sessionData, socketData);
                break;
            case ("cate"):
                statsService.cate(socket, socketData);
                break;
            case ("snap"):
                statsService.snap(socket, sessionData, socketData);
                break;
            case ("gsea"):
                lobbyService.gsea(socket, socketData);
                break;
            case ("gget"):
                lobbyService.gget(socket, sessionData, socketData);
                break;
            case ("gjoi"):
                lobbyService.gjoi(socket, sessionData, socketData);
                break;
            case ("gpsc"):
                lobbyService.gpsc(socket, sessionData, socketData);
                break;
            case ("gcre"):
                lobbyService.gcre(socket, sessionData, socketData);
                break;
            case ("glea"):
                lobbyService.glea(socket, sessionData, socketData);
                break;
            case ("gpss"):
                lobbyService.gpss(socket, sessionData, socketData);
                break;
            default:
                log.info("Unsupported operation: {}", socketData.getIdMessage());
                SocketWriter.write(socket, socketData);
                break;
        }
    }

}
