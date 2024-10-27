package com.ea.repositories;

import com.ea.entities.PersonaConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaConnectionRepository extends JpaRepository<PersonaConnectionEntity, Long> {

    List<PersonaConnectionEntity> findByVersInAndPersonaIdAndIsHostAndEndTimeIsNull(List<String> vers, long personaId, boolean isHost);

    Optional<PersonaConnectionEntity> findByVersAndSlusAndPersonaPersAndEndTimeIsNull(String vers, String slus, String pers);

}
