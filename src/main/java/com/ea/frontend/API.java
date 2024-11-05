package com.ea.frontend;

import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
import com.ea.entities.PersonaConnectionEntity;
import com.ea.services.GameService;
import com.ea.repositories.GameReportRepository;
import com.ea.repositories.PersonaConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class API {
    @Autowired
    private GameService gameService;

    @Autowired
    private GameReportRepository gameReportRepository;

    @Autowired
    private PersonaConnectionRepository personaConnectionRepository;

    /**
     * Retrieves all active games (games with no end time)
     */
    public List<GameEntity> getActiveGames() {
        return gameService.gameRepository.findByEndTimeIsNull();
    }

    /**
     * Gets all active players (excluding host) for a specific game
     */
    public List<GameReportEntity> getActiveReports(GameEntity game) {
        return gameReportRepository.findByGameIdAndEndTimeIsNull(game.getId()).stream()
                .filter(report -> !report.isHost())
                .toList();
    }

    /**
     * Gets count of players currently in active games (excluding hosts)
     */
    public int getPlayersInGame() {
        // Count unique players that have active game reports and aren't hosts
        return (int) gameReportRepository.findAll().stream()
                .filter(report -> report.getEndTime() == null)
                .filter(report -> !report.isHost())
                .map(report -> report.getPersonaConnection().getId())
                .distinct()
                .count();
    }

    /**
     * Gets count of players in lobby (connected but not in game)
     */
    public int getPlayersInLobby() {
        // Get all active persona connections
        List<PersonaConnectionEntity> activeConnections = personaConnectionRepository.findAll().stream()
                .filter(pc -> pc.getEndTime() == null && !pc.isHost())
                .collect(Collectors.toList());

        // Get IDs of players in games
        List<Long> playerIdsInGames = gameReportRepository.findAll().stream()
                .filter(report -> report.getEndTime() == null)
                .map(report -> report.getPersonaConnection().getId())
                .distinct()
                .toList();

        // Count connections that aren't in games
        return (int) activeConnections.stream()
                .filter(pc -> !playerIdsInGames.contains(pc.getId()))
                .count();
    }

    /**
     * Formats a LocalDateTime to a string in format "yyyy-MM-dd HH:mm:ss"
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Formats duration since the given time until now in format "Xh Ym" or "Y min"
     */
    public String formatDuration(LocalDateTime startTime) {
        long minutes = ChronoUnit.MINUTES.between(startTime, LocalDateTime.now());
        if (minutes < 60) {
            return minutes + " min";
        }
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }

    record MonitorResponse(
            LocalDateTime timestamp,
            Statistics stats,
            List<GameInfo> activeGames
    ) {}

    record Statistics(
            int activeGames,
            int playersInGame,
            int playersInLobby,
            int totalPlayers
    ) {}

    record GameInfo(
            Long id,
            String name,
            String version,
            LocalDateTime startTime,
            Integer maxPlayers,
            List<PlayerInfo> activePlayers
    ) {}

    record PlayerInfo(
            String name,
            boolean isHost,
            LocalDateTime joinTime,
            String playTime
    ) {}
}

