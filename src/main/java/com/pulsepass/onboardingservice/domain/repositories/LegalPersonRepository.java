package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;

public interface LegalPersonRepository {

    LegalPerson save(LegalPerson legalPerson);
}
