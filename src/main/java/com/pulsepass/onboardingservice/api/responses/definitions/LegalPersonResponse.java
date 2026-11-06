package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;

public record LegalPersonResponse(
        String email
) {

    public static LegalPersonResponse fromEntity(LegalPerson legalPerson) {
        return new LegalPersonResponse(legalPerson.getEmail());
    }
}
