package com.ea.repositories;

import com.ea.entities.PersonaConnectionEntity;
import com.ea.entities.PersonaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaConnectionRepository extends JpaRepository<PersonaConnectionEntity, Long> {

    Optional<PersonaConnectionEntity> findByPersonaAndVersInAndEndTimeIsNull(PersonaEntity persona, List<String> vers);

    Optional<PersonaConnectionEntity> findByPersonaAndVersAndSlusAndEndTimeIsNull(PersonaEntity persona, String vers, String slus);

}
