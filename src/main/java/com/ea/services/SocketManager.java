package com.ea.services;

import com.ea.dto.SocketWrapper;
import com.ea.entities.GameEntity;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SocketManager {
    private static final ConcurrentHashMap<String, SocketWrapper> sockets = new ConcurrentHashMap<>();

    public static void addSocket(String identifier, Socket socket) {
        SocketWrapper wrapper = new SocketWrapper();
        wrapper.setSocket(socket);
        wrapper.setIdentifier(identifier);
        sockets.put(identifier, wrapper);
    }

    public static void removeSocket(String identifier) {
        sockets.remove(identifier);
    }

    public static void setHost(String identifier, boolean isHost) {
        SocketWrapper wrapper = sockets.get(identifier);
        if (wrapper != null) {
            wrapper.setHost(isHost);
        }
    }

    public static void setGameEntity(String identifier, GameEntity gameEntity) {
        SocketWrapper wrapper = sockets.get(identifier);
        if (wrapper != null) {
            wrapper.setGameEntity(gameEntity);
        }
    }

    public static SocketWrapper getSocketWrapper(Socket socket) {
        return getSocketWrapper(socket.getRemoteSocketAddress().toString());
    }

    private static SocketWrapper getSocketWrapper(String identifier) {
        return sockets.get(identifier);
    }

    public static SocketWrapper getHostSocketWrapperOfGame(Long gameId) {
        return sockets.values().stream()
                //.filter(wrapper -> wrapper.getGameEntity() != null && wrapper.getGameEntity().getId() == gameId && wrapper.isHost())
                .filter(wrapper -> wrapper.isHost())
                .findFirst()
                .orElse(null);
    }

    public static List<Socket> getHostSockets() {
        return sockets.values().stream()
                .filter(wrapper -> wrapper.isHost())
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