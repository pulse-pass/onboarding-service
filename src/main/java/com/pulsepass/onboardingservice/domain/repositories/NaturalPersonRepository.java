package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import java.util.Optional;

public interface NaturalPersonRepository {

    NaturalPerson save(NaturalPerson naturalPerson);

    Optional<NaturalPerson> findByPersonKey(String personKey);
}
