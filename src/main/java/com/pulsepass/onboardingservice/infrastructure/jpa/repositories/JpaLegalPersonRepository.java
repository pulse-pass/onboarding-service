package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLegalPersonRepository extends JpaRepository<LegalPersonEntity, Long> {

    Optional<LegalPersonEntity> findByPersonPersonKey(String personKey);
}
