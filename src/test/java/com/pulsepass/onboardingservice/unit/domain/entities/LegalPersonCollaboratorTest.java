package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private static LegalPersonCollaborator collaborator() {
        var legal = new LegalPerson(new Person("legal-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com"));
        var natural = new NaturalPerson(new Person("owner-key", new PersonType(2L, "NATURAL", NOW),
                "52998224725", "John Doe", "john@mail.com"));
        return new LegalPersonCollaborator("collab-key", legal, natural, new CollaboratorRole(1L, "OWNER", NOW),
                new LegalPersonCollaboratorStatus(1L, "ACTIVE", NOW));
    }

    @Test
    void new_withoutId_exposesValuesAndNullId() {
        var collaborator = collaborator();

        assertThat(collaborator.getId()).isNull();
        assertThat(collaborator.getLegalPersonCollaboratorKey()).isEqualTo("collab-key");
        assertThat(collaborator.getLegalPerson().getPerson().getPersonKey()).isEqualTo("legal-key");
        assertThat(collaborator.getNaturalPerson().getPerson().getPersonKey()).isEqualTo("owner-key");
        assertThat(collaborator.getCollaboratorRole().getEnumerator()).isEqualTo("OWNER");
        assertThat(collaborator.getLegalPersonCollaboratorStatus().getEnumerator()).isEqualTo("ACTIVE");
        assertThat(collaborator.getCreatedAt()).isNull();
        assertThat(collaborator.getUpdatedAt()).isNull();
    }

    @Test
    void new_allArgs_exposesValues() {
        var base = collaborator();

        var collaborator = new LegalPersonCollaborator(9L, "k", base.getLegalPerson(), base.getNaturalPerson(),
                base.getCollaboratorRole(), base.getLegalPersonCollaboratorStatus(), NOW, NOW);

        assertThat(collaborator.getId()).isEqualTo(9L);
        assertThat(collaborator.getLegalPersonCollaboratorKey()).isEqualTo("k");
        assertThat(collaborator.getCreatedAt()).isEqualTo(NOW);
        assertThat(collaborator.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var base = collaborator();
        var collaborator = new LegalPersonCollaborator();

        collaborator.setId(1L);
        collaborator.setLegalPersonCollaboratorKey("other");
        collaborator.setLegalPerson(base.getLegalPerson());
        collaborator.setNaturalPerson(base.getNaturalPerson());
        collaborator.setCollaboratorRole(base.getCollaboratorRole());
        collaborator.setLegalPersonCollaboratorStatus(base.getLegalPersonCollaboratorStatus());
        collaborator.setCreatedAt(NOW);
        collaborator.setUpdatedAt(NOW);

        assertThat(collaborator.getId()).isEqualTo(1L);
        assertThat(collaborator.getLegalPersonCollaboratorKey()).isEqualTo("other");
        assertThat(collaborator.getLegalPerson()).isSameAs(base.getLegalPerson());
        assertThat(collaborator.getNaturalPerson()).isSameAs(base.getNaturalPerson());
        assertThat(collaborator.getCollaboratorRole()).isSameAs(base.getCollaboratorRole());
        assertThat(collaborator.getLegalPersonCollaboratorStatus())
                .isSameAs(base.getLegalPersonCollaboratorStatus());
        assertThat(collaborator.getCreatedAt()).isEqualTo(NOW);
        assertThat(collaborator.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void toHistoryData_collaborator_returnsOrderedSnapshot() {
        var data = collaborator().toHistoryData();

        assertThat(data.keySet()).containsExactlyElementsOf(List.of(
                "legal_person_collaborator_key", "legal_person_key", "natural_person_key",
                "collaborator_role", "legal_person_collaborator_status"));
        assertThat(data).containsEntry("legal_person_collaborator_key", "collab-key")
                .containsEntry("legal_person_key", "legal-key")
                .containsEntry("natural_person_key", "owner-key")
                .containsEntry("collaborator_role", "OWNER")
                .containsEntry("legal_person_collaborator_status", "ACTIVE");
    }
}
