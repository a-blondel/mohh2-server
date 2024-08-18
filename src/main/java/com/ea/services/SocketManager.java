package com.ea.services;

import com.ea.dto.SocketWrapper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SocketManager {
    private final ConcurrentHashMap<String, SocketWrapper> sockets = new ConcurrentHashMap<>();

    public void addSocket(String identifier, Socket socket) {
        sockets.put(identifier, new SocketWrapper(socket, identifier, false));
    }

    public void removeSocket(String identifier) {
        sockets.remove(identifier);
    }

    public void setHost(String identifier, boolean isHost) {
        SocketWrapper wrapper = sockets.get(identifier);
        if (wrapper != null) {
            wrapper.setHost(isHost);
        }
    }

    public List<Socket> getHostSockets() {
        return sockets.values().stream()
                .filter(SocketWrapper::isHost)
                .map(SocketWrapper::getSocket)
                .collect(Collectors.toList());
    }

    public void broadcastMessage(String message) {
        sockets.values().forEach(wrapper -> {
            try {
                writeMessageToSocket(wrapper.getSocket(), message);
            } catch (IOException e) {
                // Handle exception, possibly remove the socket from the collection
            }
        });
    }

    private void writeMessageToSocket(Socket socket, String message) throws IOException {
        // If necessary, implement the logic to write a message to a socket
    }
}