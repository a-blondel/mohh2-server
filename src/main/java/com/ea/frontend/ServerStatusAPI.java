package com.ea.frontend;

import com.ea.entities.GameEntity;
import com.ea.entities.GameReportEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ServerStatusAPI {

    @Autowired
    private API api;

    @GetMapping("/games/api")
    public ResponseEntity<API.MonitorResponse> getGameMonitorJson() {
        // Get active games
        List<GameEntity> activeGames = api.getActiveGames();

        // Calculate statistics
        int playersInGame = api.getPlayersInGame();
        int playersInLobby = api.getPlayersInLobby();
        int totalPlayers = playersInGame + playersInLobby;

        // Create response object
        API.MonitorResponse response = new API.MonitorResponse(
                LocalDateTime.now(),
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

    private API.GameInfo convertToGameInfo(GameEntity game) {
        List<API.PlayerInfo> activePlayers = api.getActiveReports(game)
                .stream()
                .map(this::convertToPlayerInfo)
                .toList();

        return new API.GameInfo(
                game.getId(),
                game.getName(),
                game.getVers(),
                game.getStartTime(),
                game.getMaxsize(),
                activePlayers
        );
    }

    private API.PlayerInfo convertToPlayerInfo(GameReportEntity report) {
        return new API.PlayerInfo(
                report.getPersonaConnection().getPersona().getPers(),
                report.isHost(),
                report.getStartTime(),
                api.formatDuration(report.getStartTime())
        );
    }
}
