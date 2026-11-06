package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaLegalPersonCollaboratorRepository extends JpaRepository<LegalPersonCollaboratorEntity, Long>,
        JpaSpecificationExecutor<LegalPersonCollaboratorEntity> {

    boolean existsByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
            String legalPersonKey,
            String naturalPersonKey
    );
}
