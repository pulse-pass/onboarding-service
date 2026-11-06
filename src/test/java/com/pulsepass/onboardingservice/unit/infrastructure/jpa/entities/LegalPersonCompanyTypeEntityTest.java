package com.pulsepass.onboardingservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCompanyTypeEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonCompanyTypeEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_allFields_roundTrips() {
        var domain = new LegalPersonCompanyType(1L, "ENUM", NOW);

        var result = LegalPersonCompanyTypeEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
