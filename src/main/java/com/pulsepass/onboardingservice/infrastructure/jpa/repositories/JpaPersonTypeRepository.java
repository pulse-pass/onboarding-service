package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPersonTypeRepository extends JpaRepository<PersonTypeEntity, Long> {

    Optional<PersonTypeEntity> findByEnumerator(String enumerator);
}
