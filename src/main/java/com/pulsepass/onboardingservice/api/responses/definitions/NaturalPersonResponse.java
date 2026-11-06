package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;

public record NaturalPersonResponse(
        String email
) {

    public static NaturalPersonResponse fromEntity(NaturalPerson naturalPerson) {
        return new NaturalPersonResponse(naturalPerson.getEmail());
    }
}
