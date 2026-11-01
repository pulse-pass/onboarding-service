package com.pulsepass.onboardingservice.application.dtos.output;

import com.pulsepass.onboardingservice.domain.entities.Person;

public record GetLegalPersonDTO(
        Person legalPerson,
        Person owner
) {
}
