package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.Person;
import java.util.Optional;

public interface PersonRepository {

    Person save(Person person);

    Optional<Person> findByDocumentNumber(String documentNumber);
}
