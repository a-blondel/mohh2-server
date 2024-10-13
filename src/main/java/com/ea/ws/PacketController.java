package com.ea.ws;

import com.ea.dto.SocketData;
import com.ea.services.SocketManager;
import com.ea.steps.SocketWriter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PacketController {

    @PostMapping("/packet")
    public void sendPacket(@RequestBody PacketDTO packet) {
        if(SocketManager.getHostSockets() != null && !SocketManager.getHostSockets().isEmpty()) {
            SocketData socketData = new SocketData(packet.getPacketId(), null, packet.getPacketData());
            SocketWriter.write(SocketManager.getHostSockets().get(0), socketData);
        }
    }

}
