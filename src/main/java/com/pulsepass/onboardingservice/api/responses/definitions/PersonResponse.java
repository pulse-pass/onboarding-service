package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.Person;

public record PersonResponse(
        String personKey,
        String documentNumber,
        String name
) {

    public static PersonResponse fromEntity(Person person) {
        return new PersonResponse(
                person.getPersonKey(),
                person.getDocumentNumber(),
                person.getName()
        );
    }
}
