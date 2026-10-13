package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonEntity;
import org.springframework.stereotype.Repository;

@Repository
public class LegalPersonRepositoryImpl implements LegalPersonRepository {

    private final JpaLegalPersonRepository jpaRepository;

    public LegalPersonRepositoryImpl(JpaLegalPersonRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LegalPerson save(LegalPerson legalPerson) {
        var saved = jpaRepository.save(LegalPersonEntity.fromDomain(legalPerson));
        return saved.toDomain();
    }
}
