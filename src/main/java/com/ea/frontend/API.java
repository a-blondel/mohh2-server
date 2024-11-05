package com.ea.frontend;

import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
import com.ea.entities.PersonaConnectionEntity;
import com.ea.services.GameService;
import com.ea.repositories.GameReportRepository;
import com.ea.repositories.PersonaConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        // Get all active persona connections (excluding hosts)
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
     * Converts LocalDateTime to UTC Instant considering system timezone
     */
    public Instant toUTCInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    /**
     * Formats duration since the given time until now in format "Xh Ym" or "Y min"
     * All calculations are done in UTC
     */
    public String formatDuration(Instant startTime) {
        if (startTime == null) {
            return "N/A";
        }

        long minutes = ChronoUnit.MINUTES.between(startTime, Instant.now());
        if (minutes < 0) {
            return "0 min";  // Handle case where time might be slightly in the future due to clock skew
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }

    record MonitorResponse(
            Instant timestamp,
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
            Instant startTime,
            Integer maxPlayers,
            List<PlayerInfo> activePlayers
    ) {}

    record PlayerInfo(
            String name,
            boolean isHost,
            Instant joinTime,
            String playTime
    ) {}
}