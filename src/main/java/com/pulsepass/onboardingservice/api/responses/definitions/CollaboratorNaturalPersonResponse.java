package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;

public record CollaboratorNaturalPersonResponse(
        String email,
        PersonResponse person
) {

    public static CollaboratorNaturalPersonResponse fromEntity(NaturalPerson naturalPerson) {
        return new CollaboratorNaturalPersonResponse(
                naturalPerson.getEmail(),
                PersonResponse.fromEntity(naturalPerson.getPerson())
        );
    }
}
