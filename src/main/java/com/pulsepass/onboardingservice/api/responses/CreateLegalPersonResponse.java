package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.PersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;

public record CreateLegalPersonResponse(
        String email,
        PersonResponse person
) {

    public static CreateLegalPersonResponse fromEntity(LegalPerson legalPerson) {
        return new CreateLegalPersonResponse(
                legalPerson.getEmail(),
                PersonResponse.fromEntity(legalPerson.getPerson())
        );
    }
}
