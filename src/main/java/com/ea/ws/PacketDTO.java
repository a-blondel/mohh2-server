package com.ea.ws;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PacketDTO {

    private String packetId;
    private Map<String, String> packetData;

}
