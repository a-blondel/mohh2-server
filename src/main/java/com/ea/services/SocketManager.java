package com.ea.services;

import com.ea.dto.SocketWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SocketManager {
    private final ConcurrentHashMap<String, SocketWrapper> sockets = new ConcurrentHashMap<>();

    // TODO merge SessionData and SocketWrapper

    public void addSocket(String identifier, Socket socket) {
        sockets.put(identifier, new SocketWrapper(socket, identifier, null, null));
    }

    public void removeSocket(String identifier) {
        sockets.remove(identifier);
    }

    public void setPers(String identifier, String pers) {
        SocketWrapper wrapper = sockets.get(identifier);
        if (wrapper != null) {
            wrapper.setPers(pers);
        }
    }

    public void setLobbyId(String identifier, Long lobbyId) {
        SocketWrapper wrapper = sockets.get(identifier);
        if (wrapper != null) {
            wrapper.setLobbyId(lobbyId);
        }
    }

    public SocketWrapper getSocketWrapper(String identifier) {
        return sockets.get(identifier);
    }

    public SocketWrapper getHostSocketWrapperOfLobby(Long lobbyId) {
        return sockets.values().stream()
                .filter(wrapper -> wrapper.getLobbyId() != null && wrapper.getLobbyId() == lobbyId && wrapper.isHost())
                .findFirst()
                .orElse(null);
    }

    public List<Socket> getHostSockets() {
        return sockets.values().stream()
                .filter(wrapper -> !StringUtils.isEmpty(wrapper.getPers()) && wrapper.getPers().contains("@"))
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