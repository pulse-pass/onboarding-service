package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.NaturalPersonEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaNaturalPersonRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.NaturalPersonRepositoryImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NaturalPersonRepositoryImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private JpaNaturalPersonRepository jpaRepository;
    private NaturalPersonRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaNaturalPersonRepository.class);
        repository = new NaturalPersonRepositoryImpl(jpaRepository);
    }

    @Test
    void save_domain_savesEntityAndReturnsMappedDomain() {
        var domain = new NaturalPerson(2L, new Person(2L, "owner-key", new PersonType(2L, "NATURAL", NOW),
                "52998224725", "John Doe", NOW, NOW), "john@mail.com", NOW, NOW);
        when(jpaRepository.save(any(NaturalPersonEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        ArgumentCaptor<NaturalPersonEntity> captor = ArgumentCaptor.forClass(NaturalPersonEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void findByPersonKey_existingKey_returnsMappedDomain() {
        var domain = new NaturalPerson(2L, new Person(2L, "owner-key", new PersonType(2L, "NATURAL", NOW),
                "52998224725", "John Doe", NOW, NOW), "john@mail.com", NOW, NOW);
        when(jpaRepository.findByPersonPersonKey("owner-key"))
                .thenReturn(Optional.of(NaturalPersonEntity.fromDomain(domain)));

        var found = repository.findByPersonKey("owner-key");

        assertThat(found).isPresent();
        assertThat(found.get()).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void findByPersonKey_unknownKey_returnsEmpty() {
        when(jpaRepository.findByPersonPersonKey("unknown")).thenReturn(Optional.empty());

        assertThat(repository.findByPersonKey("unknown")).isEmpty();
    }
}
