package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatusEnum;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.specifications.LegalPersonCollaboratorSpecification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public List<LegalPersonCollaborator> findAll(
            String legalPersonKey,
            CollaboratorRoleEnum collaboratorRole,
            int page,
            int pageSize
    ) {
        var spec = LegalPersonCollaboratorSpecification.withFilters(legalPersonKey, collaboratorRole);
        var pageable = PageRequest.of(
                page - 1,
                pageSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        return jpaRepository.findAll(spec, pageable).map(LegalPersonCollaboratorEntity::toDomain).getContent();
    }

    @Override
    public List<LegalPersonCollaborator> findAllByLegalPersonKeyAndStatus(
            String legalPersonKey,
            LegalPersonCollaboratorStatusEnum status
    ) {
        return jpaRepository
                .findAllByLegalPersonPersonPersonKeyAndLegalPersonCollaboratorStatusEnumerator(
                        legalPersonKey,
                        status.name()
                )
                .stream()
                .map(LegalPersonCollaboratorEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<LegalPersonCollaborator> findForUpdateByLegalPersonCollaboratorKey(
            String legalPersonCollaboratorKey
    ) {
        return jpaRepository.findForUpdateByLegalPersonCollaboratorKey(legalPersonCollaboratorKey)
                .map(LegalPersonCollaboratorEntity::toDomain);
    }

    @Override
    public Optional<LegalPersonCollaborator> findFirstByLegalPersonKeyAndNaturalPersonKey(
            String legalPersonKey,
            String naturalPersonKey
    ) {
        return jpaRepository.findFirstByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
                legalPersonKey,
                naturalPersonKey
        ).map(LegalPersonCollaboratorEntity::toDomain);
    }
}
