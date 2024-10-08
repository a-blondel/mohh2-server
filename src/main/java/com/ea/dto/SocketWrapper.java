package com.ea.dto;

import com.ea.entities.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.Socket;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SocketWrapper {
    private Socket socket;
    private String identifier;
    private boolean host;
    private AccountEntity accountEntity;
    private PersonaEntity personaEntity;
    private PersonaConnectionEntity personaConnectionEntity;
    private LobbyEntity lobbyEntity;
    private LobbyReportEntity lobbyReportEntity;

}
