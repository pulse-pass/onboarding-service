package com.pulsepass.onboardingservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonAddressEntity;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.JpaLegalPersonAddressRepository;
import com.pulsepass.onboardingservice.infrastructure.jpa.repositories.LegalPersonAddressRepositoryImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LegalPersonAddressRepositoryImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private JpaLegalPersonAddressRepository jpaRepository;
    private LegalPersonAddressRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaLegalPersonAddressRepository.class);
        repository = new LegalPersonAddressRepositoryImpl(jpaRepository);
    }

    @Test
    void save_domain_savesEntityAndReturnsMappedDomain() {
        var domain = new LegalPersonAddress(
                5L, "address-key", "90110220", "Rua A", "60", "Sala 2", "Centro", NOW, NOW);
        when(jpaRepository.save(any(LegalPersonAddressEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = repository.save(domain);

        ArgumentCaptor<LegalPersonAddressEntity> captor = ArgumentCaptor.forClass(LegalPersonAddressEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain()).usingRecursiveComparison().isEqualTo(domain);
        assertThat(saved).usingRecursiveComparison().isEqualTo(domain);
    }
}
