package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.NaturalPersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNaturalPersonRepository extends JpaRepository<NaturalPersonEntity, Long> {
}
