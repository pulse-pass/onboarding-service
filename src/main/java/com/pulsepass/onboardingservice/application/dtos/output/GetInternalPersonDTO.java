package com.pulsepass.onboardingservice.application.dtos.output;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import java.util.List;

public record GetInternalPersonDTO(
        Person person,
        List<PersonPhoneNumber> phoneNumbers,
        LegalPerson legalPerson,
        NaturalPerson naturalPerson
) {
}
