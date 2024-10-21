package com.ea.repositories;

import com.ea.entities.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {

    Optional<GameEntity> findById(Long id);

    @Query("FROM GameEntity g JOIN g.gameReports gr WHERE gr.persona.id = :personaId AND gr.endTime IS NULL")
    Optional<GameEntity> findCurrentGameOfPersona(Long personaId);

    List<GameEntity> findByEndTimeIsNull();

    GameEntity findByNameAndEndTimeIsNull(String name);

    List<GameEntity> findByVersInAndEndTimeIsNull(List<String> vers);

    boolean existsByNameAndVersInAndEndTimeIsNull(String name, List<String> vers);

}
