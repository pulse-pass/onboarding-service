package com.pulsepass.onboardingservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonAddressEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonAddressEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_allFields_roundTrips() {
        var domain = new LegalPersonAddress(
                5L, "address-key", "90110220", "Rua A", "60", "Sala 2", "Centro", NOW, NOW);

        var result = LegalPersonAddressEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void fromDomainToDomain_nullComplement_roundTrips() {
        var domain = new LegalPersonAddress(
                5L, "address-key", "90110220", "Rua A", "60", null, "Centro", NOW, NOW);

        var result = LegalPersonAddressEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
