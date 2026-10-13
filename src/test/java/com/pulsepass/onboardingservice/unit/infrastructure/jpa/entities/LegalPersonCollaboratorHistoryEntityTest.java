package com.pulsepass.onboardingservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorHistory;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonCollaboratorHistoryEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorHistoryEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private static LegalPersonCollaborator collaborator(LegalPersonCollaboratorStatus status) {
        var legal = new LegalPerson(3L, new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW), NOW, NOW);
        var natural = new NaturalPerson(2L, new Person(2L, "owner-key", new PersonType(2L, "NATURAL", NOW),
                "52998224725", "John Doe", "john@mail.com", NOW, NOW), NOW, NOW);
        return new LegalPersonCollaborator(4L, "collab-key", legal, natural,
                new CollaboratorRole(1L, "OWNER", NOW), status, NOW, NOW);
    }

    @Test
    void fromDomainToDomain_nullOldStatus_roundTrips() {
        var status = new LegalPersonCollaboratorStatus(1L, "ACTIVE", NOW);
        var domain = new LegalPersonCollaboratorHistory(
                9L, "hist", collaborator(status), null, status, "{\"a\":1}", NOW);

        var result = LegalPersonCollaboratorHistoryEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
        assertThat(result.getOldStatus()).isNull();
    }

    @Test
    void fromDomainToDomain_withOldStatus_roundTrips() {
        var oldStatus = new LegalPersonCollaboratorStatus(1L, "ACTIVE", NOW);
        var newStatus = new LegalPersonCollaboratorStatus(2L, "DELETED", NOW);
        var domain = new LegalPersonCollaboratorHistory(
                9L, "hist", collaborator(newStatus), oldStatus, newStatus, "{}", NOW);

        var result = LegalPersonCollaboratorHistoryEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
