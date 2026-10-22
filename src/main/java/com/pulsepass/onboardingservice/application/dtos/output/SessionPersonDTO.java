package com.pulsepass.onboardingservice.application.dtos.output;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import java.util.List;

public record SessionPersonDTO(
        Person person,
        List<String> roles,
        LegalPerson legalPerson
) {
}
