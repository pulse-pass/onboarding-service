package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaLegalPersonRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.LegalPersonRepositoryImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LegalPersonRepositoryImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private JpaLegalPersonRepository jpaRepository;
    private LegalPersonRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaLegalPersonRepository.class);
        repository = new LegalPersonRepositoryImpl(jpaRepository);
    }

    @Test
    void save_domain_savesEntityAndReturnsMappedDomain() {
        var domain = new LegalPerson(3L, new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW), NOW, NOW);
        when(jpaRepository.save(any(LegalPersonEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        ArgumentCaptor<LegalPersonEntity> captor = ArgumentCaptor.forClass(LegalPersonEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void findByPersonKey_existing_returnsMappedDomain() {
        var domain = new LegalPerson(3L, new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW), NOW, NOW);
        when(jpaRepository.findByPersonPersonKey("person-key"))
                .thenReturn(Optional.of(LegalPersonEntity.fromDomain(domain)));

        assertThat(repository.findByPersonKey("person-key")).get()
                .usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void findByPersonKey_missing_returnsEmpty() {
        when(jpaRepository.findByPersonPersonKey("person-key")).thenReturn(Optional.empty());

        assertThat(repository.findByPersonKey("person-key")).isEmpty();
    }
}
