package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLegalPersonCollaboratorRepository extends JpaRepository<LegalPersonCollaboratorEntity, Long> {

    Optional<LegalPersonCollaboratorEntity> findByLegalPersonPersonPersonKeyAndCollaboratorRoleEnumerator(
            String legalPersonKey,
            String enumerator
    );

    boolean existsByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
            String legalPersonKey,
            String naturalPersonKey
    );
}
