package com.ea.frontend;

import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
import com.ea.repositories.GameRepository;
import com.ea.repositories.GameReportRepository;
import com.ea.repositories.PersonaConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class API {
    private final GameRepository gameRepository;
    private final GameReportRepository gameReportRepository;
    private final PersonaConnectionRepository personaConnectionRepository;

    public List<GameEntity> getActiveGames() {
        return gameRepository.findByEndTimeIsNull();
    }

    public List<GameReportEntity> getActiveReports(GameEntity game) {
        return gameReportRepository.findActiveReportsWithPersonasByGameId(game.getId());
    }

    public int getPlayersInGame() {
        return gameReportRepository.countActiveNonHostPlayers();
    }

    public int getPlayersInLobby() {
        return personaConnectionRepository.countPlayersInLobby();
    }

    public Instant toUTCInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    public String formatDuration(Instant startTime) {
        if (startTime == null) {
            return "N/A";
        }

        long minutes = ChronoUnit.MINUTES.between(startTime, Instant.now());
        if (minutes < 0) {
            return "0 min";
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }
}