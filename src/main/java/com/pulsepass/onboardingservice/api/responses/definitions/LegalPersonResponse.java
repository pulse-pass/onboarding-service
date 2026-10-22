package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;

public record LegalPersonResponse(
        String legalPersonKey,
        String name
) {

    public static LegalPersonResponse fromEntity(LegalPerson legalPerson) {
        return new LegalPersonResponse(
                legalPerson.getPerson().getPersonKey(),
                legalPerson.getPerson().getName()
        );
    }
}
