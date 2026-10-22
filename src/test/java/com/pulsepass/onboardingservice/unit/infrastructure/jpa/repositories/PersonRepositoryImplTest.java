package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaPersonRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.PersonRepositoryImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PersonRepositoryImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private JpaPersonRepository jpaRepository;
    private PersonRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaPersonRepository.class);
        repository = new PersonRepositoryImpl(jpaRepository);
    }

    @Test
    void save_domain_savesEntityAndReturnsMappedDomain() {
        var domain = new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW);
        when(jpaRepository.save(any(PersonEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        ArgumentCaptor<PersonEntity> captor = ArgumentCaptor.forClass(PersonEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void findByDocumentNumber_existing_returnsMappedDomain() {
        var person = new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW);
        when(jpaRepository.findByDocumentNumber("12345678000195"))
                .thenReturn(Optional.of(PersonEntity.fromDomain(person)));

        assertThat(repository.findByDocumentNumber("12345678000195")).get()
                .extracting(Person::getPersonKey).isEqualTo("person-key");
    }

    @Test
    void findByDocumentNumber_missing_returnsEmpty() {
        when(jpaRepository.findByDocumentNumber("12345678000195")).thenReturn(Optional.empty());

        assertThat(repository.findByDocumentNumber("12345678000195")).isEmpty();
    }

    @Test
    void findByPersonKey_existing_returnsMappedDomain() {
        var person = new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW);
        when(jpaRepository.findByPersonKey("person-key"))
                .thenReturn(Optional.of(PersonEntity.fromDomain(person)));

        assertThat(repository.findByPersonKey("person-key")).get()
                .extracting(Person::getDocumentNumber).isEqualTo("12345678000195");
    }

    @Test
    void findByPersonKey_missing_returnsEmpty() {
        when(jpaRepository.findByPersonKey("person-key")).thenReturn(Optional.empty());

        assertThat(repository.findByPersonKey("person-key")).isEmpty();
    }
}
