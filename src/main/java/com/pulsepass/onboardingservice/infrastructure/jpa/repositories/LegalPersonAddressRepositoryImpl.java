package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonAddressRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonAddressEntity;
import org.springframework.stereotype.Repository;

@Repository
public class LegalPersonAddressRepositoryImpl implements LegalPersonAddressRepository {

    private final JpaLegalPersonAddressRepository jpaRepository;

    public LegalPersonAddressRepositoryImpl(JpaLegalPersonAddressRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LegalPersonAddress save(LegalPersonAddress legalPersonAddress) {
        var saved = jpaRepository.save(LegalPersonAddressEntity.fromDomain(legalPersonAddress));
        return saved.toDomain();
    }
}
