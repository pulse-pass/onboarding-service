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
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW);
        var domain = new PersonPhoneNumber(5L, "phone-key", person, "55", "11", "999999999", NOW, NOW);
        when(jpaRepository.save(any(PersonPhoneNumberEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        ArgumentCaptor<PersonPhoneNumberEntity> captor = ArgumentCaptor.forClass(PersonPhoneNumberEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }
}
