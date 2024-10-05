package com.ea.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.net.Socket;

@AllArgsConstructor
@Getter
@Setter
public class SocketWrapper {
    private Socket socket;
    private String identifier;
    private String pers;
    private Long gameId;

    public boolean isHost() {
        return !StringUtils.isEmpty(pers) && pers.contains("@");
    }

    public String getPers() {
        if (pers != null && pers.contains("@")) {
            return pers.split("@")[1];
        }
        return pers;
    }

}
