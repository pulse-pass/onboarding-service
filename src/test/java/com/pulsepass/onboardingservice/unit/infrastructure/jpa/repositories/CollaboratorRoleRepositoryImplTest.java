package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.CollaboratorRoleEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.CollaboratorRoleRepositoryImpl;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaCollaboratorRoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollaboratorRoleRepositoryImplTest {

    private JpaCollaboratorRoleRepository jpaRepository;
    private CollaboratorRoleRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaCollaboratorRoleRepository.class);
        repository = new CollaboratorRoleRepositoryImpl(jpaRepository);
    }

    @Test
    void findByEnumerator_existing_returnsMappedDomain() {
        when(jpaRepository.findByEnumerator("ENUM"))
                .thenReturn(Optional.of(CollaboratorRoleEntity.fromDomain(new CollaboratorRole(1L, "ENUM", null))));

        assertThat(repository.findByEnumerator("ENUM")).get()
                .extracting(CollaboratorRole::getEnumerator).isEqualTo("ENUM");
    }

    @Test
    void findByEnumerator_missing_returnsEmpty() {
        when(jpaRepository.findByEnumerator("ENUM")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("ENUM")).isEmpty();
    }
}
