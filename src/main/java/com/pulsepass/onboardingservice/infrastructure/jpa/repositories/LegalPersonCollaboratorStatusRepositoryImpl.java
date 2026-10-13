package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorStatusRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorStatusEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class LegalPersonCollaboratorStatusRepositoryImpl implements LegalPersonCollaboratorStatusRepository {

    private final JpaLegalPersonCollaboratorStatusRepository jpaRepository;

    public LegalPersonCollaboratorStatusRepositoryImpl(JpaLegalPersonCollaboratorStatusRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LegalPersonCollaboratorStatus> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(LegalPersonCollaboratorStatusEntity::toDomain);
    }
}
