package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLegalPersonCollaboratorHistoryRepository
        extends JpaRepository<LegalPersonCollaboratorHistoryEntity, Long> {
}
