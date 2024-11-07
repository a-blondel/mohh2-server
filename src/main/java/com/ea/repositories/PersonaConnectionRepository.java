package com.ea.repositories;

import com.ea.entities.PersonaConnectionEntity;
import com.ea.frontend.DTO;
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
public interface PersonaConnectionRepository extends JpaRepository<PersonaConnectionEntity, Long> {

    Optional<PersonaConnectionEntity> findByVersAndSlusAndPersonaPersAndIsHostFalseAndEndTimeIsNull(
            String vers,
            String slus,
            String pers
    );

    @Transactional
    @Modifying
    @Query("UPDATE PersonaConnectionEntity pc SET pc.endTime = :endTime WHERE pc.endTime IS NULL")
    int setEndTimeForAllUnfinishedPersonaConnections(@Param("endTime") LocalDateTime endTime);

    @Query("""
        SELECT COUNT(pc) FROM PersonaConnectionEntity pc 
        WHERE pc.endTime IS NULL 
        AND pc.isHost = false 
        AND pc.id NOT IN (
            SELECT gr.personaConnection.id 
            FROM GameReportEntity gr 
            WHERE gr.endTime IS NULL
        )
    """)
    int countPlayersInLobby();

    @Query("SELECT COUNT(pc) FROM PersonaConnectionEntity pc " +
            "WHERE pc.endTime IS NULL AND pc.isHost = false")
    int countActiveNonHostConnections();

    @Query("SELECT pc FROM PersonaConnectionEntity pc " +
            "JOIN FETCH pc.persona " +
            "WHERE pc.endTime IS NULL " +
            "AND pc.isHost = false " +
            "ORDER BY pc.startTime DESC")
    List<PersonaConnectionEntity> findActiveNonHostConnectionsWithPersonas();

    @Query("""
        SELECT new com.ea.frontend.DTO$ConnectionStatsDTO(
            COUNT(DISTINCT pc.id),
            COUNT(DISTINCT CASE WHEN pc.isHost = false THEN pc.id END),
            COUNT(DISTINCT pc.persona.id)
        )
        FROM PersonaConnectionEntity pc
        WHERE pc.endTime IS NULL
    """)
    DTO.ConnectionStatsDTO getActiveConnectionStats();
}

