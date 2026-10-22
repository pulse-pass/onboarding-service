package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLegalPersonCollaboratorRepository extends JpaRepository<LegalPersonCollaboratorEntity, Long> {

    boolean existsByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
            String legalPersonKey,
            String naturalPersonKey
    );
}
