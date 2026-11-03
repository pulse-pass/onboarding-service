package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonPhoneNumberEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPersonPhoneNumberRepository extends JpaRepository<PersonPhoneNumberEntity, Long> {

    List<PersonPhoneNumberEntity> findAllByPersonPersonKeyOrderByIdAsc(String personKey);
}
