package com.ea.frontend;

import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
import com.ea.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class API {
    @Autowired
    private GameService gameService;

    public List<GameEntity> getActiveGames() {
        return gameService.gameRepository.findByEndTimeIsNull();
    }

    public int calculateTotalPlayers(List<GameEntity> activeGames) {
        return activeGames.stream()
                .mapToInt(game -> (int) game.getGameReports().stream()
                        .filter(report -> report.getEndTime() == null)
                        .count())
                .sum();
    }

    public double calculateAveragePlayersPerGame(int totalPlayers, List<GameEntity> activeGames) {
        return activeGames.isEmpty() ? 0 : (double) totalPlayers / activeGames.size();
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String formatDuration(LocalDateTime startTime) {
        long minutes = ChronoUnit.MINUTES.between(startTime, LocalDateTime.now());
        if (minutes < 60) {
            return minutes + " min";
        }
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }

    public List<GameReportEntity> getActiveReports(GameEntity game) {
        return game.getGameReports().stream()
                .filter(report -> report.getEndTime() == null)
                .toList();
    }

    record MonitorResponse(
            LocalDateTime timestamp,
            Statistics stats,
            List<GameInfo> activeGames
    ) {}

    record Statistics(
            int activeGames,
            int totalPlayers,
            double averagePlayersPerGame
    ) {}

    record GameInfo(
            Long id,
            String name,
            String version,
            LocalDateTime startTime,
            Integer maxPlayers,
            List<PlayerInfo> players
    ) {}

    record PlayerInfo(
            String name,
            boolean isHost,
            LocalDateTime joinTime,
            String playTime
    ) {}
}