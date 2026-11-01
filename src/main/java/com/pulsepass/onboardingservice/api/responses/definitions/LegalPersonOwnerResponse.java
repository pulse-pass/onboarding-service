package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.Person;

public record LegalPersonOwnerResponse(
        String personKey,
        String name,
        String documentNumber,
        String email
) {

    public static LegalPersonOwnerResponse fromEntity(Person person) {
        return new LegalPersonOwnerResponse(
                person.getPersonKey(),
                person.getName(),
                person.getDocumentNumber(),
                person.getEmail()
        );
    }
}
