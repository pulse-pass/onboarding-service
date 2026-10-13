package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;

public record CreateLegalPersonResponse(
        String personKey,
        String documentNumber,
        String name,
        String email
) {

    public static CreateLegalPersonResponse fromEntity(LegalPerson legalPerson) {
        return new CreateLegalPersonResponse(
                legalPerson.getPerson().getPersonKey(),
                legalPerson.getPerson().getDocumentNumber(),
                legalPerson.getPerson().getName(),
                legalPerson.getPerson().getEmail()
        );
    }
}
