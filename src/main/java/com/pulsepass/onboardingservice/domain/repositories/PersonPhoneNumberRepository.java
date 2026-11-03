package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import java.util.List;

public interface PersonPhoneNumberRepository {

    PersonPhoneNumber save(PersonPhoneNumber personPhoneNumber);

    List<PersonPhoneNumber> findAllByPersonKey(String personKey);
}
