package com.pulsepass.onboardingservice.application.dtos.output;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;

public record GetPersonDTO(
        Person person,
        LegalPerson legalPerson,
        NaturalPerson naturalPerson
) {
}
