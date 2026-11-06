package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPersonRepository extends JpaRepository<PersonEntity, Long> {

    Optional<PersonEntity> findByDocumentNumber(String documentNumber);

    Optional<PersonEntity> findByPersonKey(String personKey);
}
