package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

public interface JpaLegalPersonCollaboratorRepository extends JpaRepository<LegalPersonCollaboratorEntity, Long>,
        JpaSpecificationExecutor<LegalPersonCollaboratorEntity> {

    List<LegalPersonCollaboratorEntity>
            findAllByLegalPersonPersonPersonKeyAndLegalPersonCollaboratorStatusEnumerator(
            String legalPersonKey,
            String enumerator
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LegalPersonCollaboratorEntity> findForUpdateByLegalPersonCollaboratorKey(
            String legalPersonCollaboratorKey
    );

    Optional<LegalPersonCollaboratorEntity> findFirstByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
            String legalPersonKey,
            String naturalPersonKey
    );
}
