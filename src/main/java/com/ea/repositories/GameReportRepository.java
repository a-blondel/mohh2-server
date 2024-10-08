package com.ea.repositories;

import com.ea.entities.GameReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameReportRepository extends JpaRepository<GameReportEntity, Long> {

    @Query("SELECT lr FROM GameReportEntity lr " +
            "JOIN  lr.persona.personaConnections pc " +
            "WHERE pc.ip = :ip AND pc.endTime IS NULL AND lr.endTime IS NULL")
    Optional<GameReportEntity> findCurrentGameReportByIP(String ip);

}
