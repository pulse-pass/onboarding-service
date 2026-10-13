package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonTypeEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaPersonTypeRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.PersonTypeRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PersonTypeRepositoryImplTest {

    private JpaPersonTypeRepository jpaRepository;
    private PersonTypeRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaPersonTypeRepository.class);
        repository = new PersonTypeRepositoryImpl(jpaRepository);
    }

    @Test
    void findByEnumerator_existing_returnsMappedDomain() {
        when(jpaRepository.findByEnumerator("ENUM"))
                .thenReturn(Optional.of(PersonTypeEntity.fromDomain(new PersonType(1L, "ENUM", null))));

        assertThat(repository.findByEnumerator("ENUM")).get()
                .extracting(PersonType::getEnumerator).isEqualTo("ENUM");
    }

    @Test
    void findByEnumerator_missing_returnsEmpty() {
        when(jpaRepository.findByEnumerator("ENUM")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("ENUM")).isEmpty();
    }
}
