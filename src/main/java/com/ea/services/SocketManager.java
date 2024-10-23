package com.ea.services;

import com.ea.dto.SocketWrapper;
import com.ea.repositories.GameReportRepository;
import com.ea.utils.BeanUtil;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SocketManager {

    private static GameReportRepository gameReportRepository = BeanUtil.getBean(GameReportRepository.class);
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

    public static SocketWrapper getSocketWrapper(Socket socket) {
        return getSocketWrapper(socket.getRemoteSocketAddress().toString());
    }

    private static SocketWrapper getSocketWrapper(String identifier) {
        return sockets.get(identifier);
    }

    public static SocketWrapper getHostSocketWrapperOfGame(Long gameId) {
        return gameReportRepository.findHostAddressByGameId(gameId)
                .map(SocketManager::getSocketWrapper)
                .orElse(null);
    }

    public static SocketWrapper getAvailableGps() {
        return sockets.values().stream()
                .filter(wrapper -> wrapper.isGps() && !wrapper.isHosting())
                .findFirst()
                .orElse(null);
    }

    public static List<Socket> getHostSockets() {
        return sockets.values().stream()
                .filter(wrapper -> wrapper.isHost())
                .map(SocketWrapper::getSocket)
                .collect(Collectors.toList());
    }
}