package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCompanyTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLegalPersonCompanyTypeRepository extends JpaRepository<LegalPersonCompanyTypeEntity, Long> {

    Optional<LegalPersonCompanyTypeEntity> findByEnumerator(String enumerator);
}
