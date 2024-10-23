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
    private boolean host = false;
    private boolean gps = false; // Game Persistent Spawn Service
    private boolean hosting = false;
    private AccountEntity accountEntity;
    private PersonaEntity personaEntity;
    private PersonaConnectionEntity personaConnectionEntity;

}
