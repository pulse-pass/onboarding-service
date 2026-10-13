package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.repositories.NaturalPersonRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.NaturalPersonEntity;
import org.springframework.stereotype.Repository;

@Repository
public class NaturalPersonRepositoryImpl implements NaturalPersonRepository {

    private final JpaNaturalPersonRepository jpaRepository;

    public NaturalPersonRepositoryImpl(JpaNaturalPersonRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NaturalPerson save(NaturalPerson naturalPerson) {
        var saved = jpaRepository.save(NaturalPersonEntity.fromDomain(naturalPerson));
        return saved.toDomain();
    }
}
