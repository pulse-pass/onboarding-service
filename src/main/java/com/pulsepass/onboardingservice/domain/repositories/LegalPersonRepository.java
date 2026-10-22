package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import java.util.Optional;

public interface LegalPersonRepository {

    LegalPerson save(LegalPerson legalPerson);

    Optional<LegalPerson> findByPersonKey(String personKey);
}
