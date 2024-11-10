package com.ea.dto;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.ea.entities.AccountEntity;
import com.ea.entities.PersonaConnectionEntity;
import com.ea.entities.PersonaEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SocketWrapper {
    private Socket socket;
    private String identifier;
    private final AtomicBoolean isHost = new AtomicBoolean(false);
    private final AtomicBoolean isGps = new AtomicBoolean(false);
    private final AtomicBoolean isHosting = new AtomicBoolean(false);
    private final AtomicInteger pingSendCounter = new AtomicInteger(0);
    private final AtomicInteger pingReceiveCounter = new AtomicInteger(0);
    private volatile AccountEntity accountEntity;
    private volatile PersonaEntity personaEntity;
    private volatile PersonaConnectionEntity personaConnectionEntity;
}
