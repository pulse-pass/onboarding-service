package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.repositories.PersonPhoneNumberRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonPhoneNumberEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PersonPhoneNumberRepositoryImpl implements PersonPhoneNumberRepository {

    private final JpaPersonPhoneNumberRepository jpaRepository;

    public PersonPhoneNumberRepositoryImpl(JpaPersonPhoneNumberRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PersonPhoneNumber save(PersonPhoneNumber personPhoneNumber) {
        var saved = jpaRepository.save(PersonPhoneNumberEntity.fromDomain(personPhoneNumber));
        return saved.toDomain();
    }

    @Override
    public List<PersonPhoneNumber> findAllByPersonKey(String personKey) {
        return jpaRepository.findAllByPersonPersonKeyOrderByIdAsc(personKey).stream()
                .map(PersonPhoneNumberEntity::toDomain)
                .toList();
    }
}
