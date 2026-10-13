package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorHistory;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorHistoryRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorHistoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public class LegalPersonCollaboratorHistoryRepositoryImpl implements LegalPersonCollaboratorHistoryRepository {

    private final JpaLegalPersonCollaboratorHistoryRepository jpaRepository;

    public LegalPersonCollaboratorHistoryRepositoryImpl(JpaLegalPersonCollaboratorHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LegalPersonCollaboratorHistory save(LegalPersonCollaboratorHistory legalPersonCollaboratorHistory) {
        var saved = jpaRepository.save(LegalPersonCollaboratorHistoryEntity.fromDomain(legalPersonCollaboratorHistory));
        return saved.toDomain();
    }
}
