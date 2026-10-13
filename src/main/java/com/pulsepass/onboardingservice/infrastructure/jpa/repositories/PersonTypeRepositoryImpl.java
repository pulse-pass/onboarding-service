package com.pulsepass.onboardingservice.infrastructure.jpa.repositories;

import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.domain.repositories.PersonTypeRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonTypeEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PersonTypeRepositoryImpl implements PersonTypeRepository {

    private final JpaPersonTypeRepository jpaRepository;

    public PersonTypeRepositoryImpl(JpaPersonTypeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PersonType> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(PersonTypeEntity::toDomain);
    }
}
