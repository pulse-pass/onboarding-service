package com.pulsepass.onboardingservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorStatusEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorStatusEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_allFields_roundTrips() {
        var domain = new LegalPersonCollaboratorStatus(1L, "ENUM", NOW);

        var result = LegalPersonCollaboratorStatusEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
