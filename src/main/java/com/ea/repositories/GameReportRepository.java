package com.ea.repositories;

import com.ea.entities.GameReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameReportRepository extends JpaRepository<GameReportEntity, Long> {

    List<GameReportEntity> findByGameIdAndEndTimeIsNull(Long gameId);

}
