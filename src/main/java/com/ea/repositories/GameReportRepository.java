package com.ea.repositories;

import com.ea.entities.GameReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameReportRepository extends JpaRepository<GameReportEntity, Long> {

    @Query("SELECT pc.address FROM GameReportEntity gr JOIN gr.persona p JOIN p.personaConnections pc WHERE gr.game.id = :gameId AND gr.isHost = true AND pc.endTime IS NULL")
    Optional<String> findHostAddressByGameId(@Param("gameId") Long gameId);

    Optional<GameReportEntity> findByPersonaIdAndEndTimeIsNull(Long personaId);

    List<GameReportEntity> findByGameIdAndEndTimeIsNull(Long gameId);

    List<GameReportEntity> findByPersonaPersAndGameStartTimeAndPlayTime(String pers, LocalDateTime startTime, int playTime);

}
