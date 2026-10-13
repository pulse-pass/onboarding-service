package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorStatusEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLegalPersonCollaboratorStatusRepository
        extends JpaRepository<LegalPersonCollaboratorStatusEntity, Long> {

    Optional<LegalPersonCollaboratorStatusEntity> findByEnumerator(String enumerator);
}
