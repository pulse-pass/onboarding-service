package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.repositories.PersonRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepositoryImpl implements PersonRepository {

    private final JpaPersonRepository jpaRepository;

    public PersonRepositoryImpl(JpaPersonRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Person save(Person person) {
        var saved = jpaRepository.save(PersonEntity.fromDomain(person));
        return saved.toDomain();
    }

    @Override
    public Optional<Person> findByDocumentNumber(String documentNumber) {
        return jpaRepository.findByDocumentNumber(documentNumber).map(PersonEntity::toDomain);
    }
}
