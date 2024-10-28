package com.ea.repositories;

import com.ea.entities.GameReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameReportRepository extends JpaRepository<GameReportEntity, Long> {

    @Query("SELECT pc.address FROM GameReportEntity gr JOIN gr.personaConnection pc WHERE gr.game.id = :gameId AND gr.isHost = true AND pc.isHost = true AND pc.endTime IS NULL")
    List<String> findHostAddressByGameId(@Param("gameId") Long gameId);

    Optional<GameReportEntity> findByPersonaConnectionIdAndEndTimeIsNull(Long personaConnectionId);

    List<GameReportEntity> findByGameIdAndEndTimeIsNull(Long gameId);

    List<GameReportEntity> findByPersonaConnectionPersonaPersAndGameStartTimeAndPlayTimeAndIsHostFalse(String pers, LocalDateTime startTime, int playTime);

    @Transactional
    @Modifying
    @Query("UPDATE GameReportEntity gr SET gr.endTime = :endTime WHERE gr.endTime IS NULL")
    int setEndTimeForAllUnfinishedGameReports(@Param("endTime") LocalDateTime endTime);
}
