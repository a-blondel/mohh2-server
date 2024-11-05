package com.ea.frontend;

import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;



@RestController
public class ServerStatusAPI {
    @Autowired
    private API api;

    @GetMapping("/games/api")
    public ResponseEntity<API.MonitorResponse> getGameMonitorJson() {
        List<GameEntity> activeGames = api.getActiveGames();

        int playersInGame = api.getPlayersInGame();
        int playersInLobby = api.getPlayersInLobby();
        int totalPlayers = playersInGame + playersInLobby;

        API.MonitorResponse response = new API.MonitorResponse(
                Instant.now(),
                new API.Statistics(
                        activeGames.size(),
                        playersInGame,
                        playersInLobby,
                        totalPlayers
                ),
                activeGames.stream()
                        .map(this::convertToGameInfo)
                        .toList()
        );

        return ResponseEntity.ok(response);
    }

    private int getMaxPlayerSize(GameEntity game)
    {
        int maxSize = game.getMaxsize() -1;
        if (maxSize < 0)
        {
            maxSize = 0;
        }
        return maxSize;
    }

    private API.GameInfo convertToGameInfo(GameEntity game) {
        List<API.PlayerInfo> activePlayers = api.getActiveReports(game)
                .stream()
                .map(this::convertToPlayerInfo)
                .toList();

        return new API.GameInfo(
                game.getId(),
                game.getName(),
                game.getVers(),
                api.toUTCInstant(game.getStartTime()),
                getMaxPlayerSize(game),
                activePlayers
        );
    }

    private API.PlayerInfo convertToPlayerInfo(GameReportEntity report) {
        return new API.PlayerInfo(
                report.getPersonaConnection().getPersona().getPers(),
                report.isHost(),
                api.toUTCInstant(report.getStartTime()),
                api.formatDuration(api.toUTCInstant(report.getStartTime()))
        );
    }
}