package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonPhoneNumberEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaPersonPhoneNumberRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.PersonPhoneNumberRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PersonPhoneNumberRepositoryImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private JpaPersonPhoneNumberRepository jpaRepository;
    private PersonPhoneNumberRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaPersonPhoneNumberRepository.class);
        repository = new PersonPhoneNumberRepositoryImpl(jpaRepository);
    }

    @Test
    void save_domain_savesEntityAndReturnsMappedDomain() {
        var person = new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", NOW, NOW);
        var domain = new PersonPhoneNumber(5L, "phone-key", person, "55", "11", "999999999", NOW, NOW);
        when(jpaRepository.save(any(PersonPhoneNumberEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        ArgumentCaptor<PersonPhoneNumberEntity> captor = ArgumentCaptor.forClass(PersonPhoneNumberEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void findAllByPersonKey_existingPhones_returnsMappedDomains() {
        var person = new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", NOW, NOW);
        var first = new PersonPhoneNumber(5L, "phone-1", person, "55", "11", "999999999", NOW, NOW);
        var second = new PersonPhoneNumber(6L, "phone-2", person, "55", "51", "30224180", NOW, NOW);
        when(jpaRepository.findAllByPersonPersonKeyOrderByIdAsc("person-key")).thenReturn(List.of(
                PersonPhoneNumberEntity.fromDomain(first),
                PersonPhoneNumberEntity.fromDomain(second)
        ));

        var result = repository.findAllByPersonKey("person-key");

        assertThat(result).usingRecursiveComparison().isEqualTo(List.of(first, second));
    }

    @Test
    void findAllByPersonKey_noPhones_returnsEmptyList() {
        when(jpaRepository.findAllByPersonPersonKeyOrderByIdAsc("person-key")).thenReturn(List.of());

        assertThat(repository.findAllByPersonKey("person-key")).isEmpty();
    }
}
