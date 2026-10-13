package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorStatusEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaLegalPersonCollaboratorStatusRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.LegalPersonCollaboratorStatusRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorStatusRepositoryImplTest {

    private JpaLegalPersonCollaboratorStatusRepository jpaRepository;
    private LegalPersonCollaboratorStatusRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaLegalPersonCollaboratorStatusRepository.class);
        repository = new LegalPersonCollaboratorStatusRepositoryImpl(jpaRepository);
    }

    @Test
    void findByEnumerator_existing_returnsMappedDomain() {
        var status = new LegalPersonCollaboratorStatus(1L, "ENUM", null);
        when(jpaRepository.findByEnumerator("ENUM"))
                .thenReturn(Optional.of(LegalPersonCollaboratorStatusEntity.fromDomain(status)));

        assertThat(repository.findByEnumerator("ENUM")).get()
                .extracting(LegalPersonCollaboratorStatus::getEnumerator).isEqualTo("ENUM");
    }

    @Test
    void findByEnumerator_missing_returnsEmpty() {
        when(jpaRepository.findByEnumerator("ENUM")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("ENUM")).isEmpty();
    }
}
