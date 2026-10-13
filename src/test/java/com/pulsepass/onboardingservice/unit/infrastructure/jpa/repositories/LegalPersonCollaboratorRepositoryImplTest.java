package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaLegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.LegalPersonCollaboratorRepositoryImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LegalPersonCollaboratorRepositoryImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private JpaLegalPersonCollaboratorRepository jpaRepository;
    private LegalPersonCollaboratorRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaLegalPersonCollaboratorRepository.class);
        repository = new LegalPersonCollaboratorRepositoryImpl(jpaRepository);
    }

    @Test
    void save_domain_savesEntityAndReturnsMappedDomain() {
        var legal = new LegalPerson(3L, new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW), NOW, NOW);
        var natural = new NaturalPerson(2L, new Person(2L, "owner-key", new PersonType(2L, "NATURAL", NOW),
                "52998224725", "John Doe", "john@mail.com", NOW, NOW), NOW, NOW);
        var collaborator = new LegalPersonCollaborator(4L, "collab-key", legal, natural,
                new CollaboratorRole(1L, "OWNER", NOW), new LegalPersonCollaboratorStatus(1L, "ACTIVE", NOW), NOW, NOW);
        var domain = collaborator;
        when(jpaRepository.save(any(LegalPersonCollaboratorEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        var captor = ArgumentCaptor.forClass(LegalPersonCollaboratorEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }
}
