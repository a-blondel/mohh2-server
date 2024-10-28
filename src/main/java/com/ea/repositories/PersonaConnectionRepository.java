package com.ea.repositories;

import com.ea.entities.PersonaConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PersonaConnectionRepository extends JpaRepository<PersonaConnectionEntity, Long> {

    Optional<PersonaConnectionEntity> findByVersAndSlusAndPersonaPersAndIsHostFalseAndEndTimeIsNull(String vers, String slus, String pers);

    @Transactional
    @Modifying
    @Query("UPDATE PersonaConnectionEntity pc SET pc.endTime = :endTime WHERE pc.endTime IS NULL")
    void setEndTimeForAllUnfinishedPersonaConnections(@Param("endTime") LocalDateTime endTime);
}
