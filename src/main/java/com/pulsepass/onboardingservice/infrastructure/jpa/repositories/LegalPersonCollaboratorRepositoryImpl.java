package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import org.springframework.stereotype.Repository;

@Repository
public class LegalPersonCollaboratorRepositoryImpl implements LegalPersonCollaboratorRepository {

    private final JpaLegalPersonCollaboratorRepository jpaRepository;

    public LegalPersonCollaboratorRepositoryImpl(JpaLegalPersonCollaboratorRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LegalPersonCollaborator save(LegalPersonCollaborator legalPersonCollaborator) {
        var saved = jpaRepository.save(LegalPersonCollaboratorEntity.fromDomain(legalPersonCollaborator));
        return saved.toDomain();
    }

    @Override
    public boolean existsByLegalPersonKeyAndNaturalPersonKey(String legalPersonKey, String naturalPersonKey) {
        return jpaRepository.existsByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
                legalPersonKey,
                naturalPersonKey
        );
    }
}
