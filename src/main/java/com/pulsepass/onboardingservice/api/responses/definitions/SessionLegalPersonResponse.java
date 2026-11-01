package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;

public record SessionLegalPersonResponse(
        String legalPersonKey,
        String name
) {

    public static SessionLegalPersonResponse fromEntity(LegalPerson legalPerson) {
        return new SessionLegalPersonResponse(
                legalPerson.getPerson().getPersonKey(),
                legalPerson.getPerson().getName()
        );
    }
}
