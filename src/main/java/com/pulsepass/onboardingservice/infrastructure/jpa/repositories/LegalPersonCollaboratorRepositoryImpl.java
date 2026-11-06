package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.specifications.LegalPersonCollaboratorSpecification;
import java.util.List;
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
    public boolean existsByLegalPersonKeyAndNaturalPersonKey(String legalPersonKey, String naturalPersonKey) {
        return jpaRepository.existsByLegalPersonPersonPersonKeyAndNaturalPersonPersonPersonKey(
                legalPersonKey,
                naturalPersonKey
        );
    }
}
