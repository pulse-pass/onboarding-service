package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLegalPersonAddressRepository extends JpaRepository<LegalPersonAddressEntity, Long> {
}
