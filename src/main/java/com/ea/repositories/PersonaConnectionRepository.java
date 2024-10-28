package com.ea.repositories;

import com.ea.entities.PersonaConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaConnectionRepository extends JpaRepository<PersonaConnectionEntity, Long> {

    Optional<PersonaConnectionEntity> findByVersAndSlusAndPersonaPersAndIsHostFalseAndEndTimeIsNull(String vers, String slus, String pers);

}
