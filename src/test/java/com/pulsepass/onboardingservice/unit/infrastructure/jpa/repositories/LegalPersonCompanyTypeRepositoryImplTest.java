package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCompanyTypeEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaLegalPersonCompanyTypeRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.LegalPersonCompanyTypeRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegalPersonCompanyTypeRepositoryImplTest {

    private JpaLegalPersonCompanyTypeRepository jpaRepository;
    private LegalPersonCompanyTypeRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaLegalPersonCompanyTypeRepository.class);
        repository = new LegalPersonCompanyTypeRepositoryImpl(jpaRepository);
    }

    @Test
    void findByEnumerator_existing_returnsMappedDomain() {
        when(jpaRepository.findByEnumerator("ENUM"))
                .thenReturn(Optional.of(
                        LegalPersonCompanyTypeEntity.fromDomain(new LegalPersonCompanyType(1L, "ENUM", null))));

        assertThat(repository.findByEnumerator("ENUM")).get()
                .extracting(LegalPersonCompanyType::getEnumerator).isEqualTo("ENUM");
    }

    @Test
    void findByEnumerator_missing_returnsEmpty() {
        when(jpaRepository.findByEnumerator("ENUM")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("ENUM")).isEmpty();
    }
}
