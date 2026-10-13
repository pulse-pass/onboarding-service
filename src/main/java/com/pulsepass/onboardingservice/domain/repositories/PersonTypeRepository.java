package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.util.Optional;

public interface PersonTypeRepository {

    Optional<PersonType> findByEnumerator(String enumerator);
}
