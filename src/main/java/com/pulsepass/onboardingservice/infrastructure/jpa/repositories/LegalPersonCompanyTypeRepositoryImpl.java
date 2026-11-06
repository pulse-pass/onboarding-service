package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCompanyTypeRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCompanyTypeEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class LegalPersonCompanyTypeRepositoryImpl implements LegalPersonCompanyTypeRepository {

    private final JpaLegalPersonCompanyTypeRepository jpaRepository;

    public LegalPersonCompanyTypeRepositoryImpl(JpaLegalPersonCompanyTypeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LegalPersonCompanyType> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(LegalPersonCompanyTypeEntity::toDomain);
    }
}
