package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;

public interface PersonPhoneNumberRepository {

    PersonPhoneNumber save(PersonPhoneNumber personPhoneNumber);
}
