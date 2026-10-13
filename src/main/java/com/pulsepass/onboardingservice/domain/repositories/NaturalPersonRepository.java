package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;

public interface NaturalPersonRepository {

    NaturalPerson save(NaturalPerson naturalPerson);
}
