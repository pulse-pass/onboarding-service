package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorHistory;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorHistoryTest {

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
        var status = collaborator.getLegalPersonCollaboratorStatus();

        var history = new LegalPersonCollaboratorHistory("hist-key", collaborator, null, status, "{}");

        assertThat(history.getId()).isNull();
        assertThat(history.getLegalPersonCollaboratorHistoryKey()).isEqualTo("hist-key");
        assertThat(history.getLegalPersonCollaborator()).isSameAs(collaborator);
        assertThat(history.getOldStatus()).isNull();
        assertThat(history.getNewStatus()).isSameAs(status);
        assertThat(history.getHistoryData()).isEqualTo("{}");
        assertThat(history.getCreatedAt()).isNull();
    }

    @Test
    void new_allArgs_exposesValues() {
        var collaborator = collaborator();
        var status = collaborator.getLegalPersonCollaboratorStatus();

        var history = new LegalPersonCollaboratorHistory(5L, "hist-key", collaborator, status, status, "{}", NOW);

        assertThat(history.getId()).isEqualTo(5L);
        assertThat(history.getOldStatus()).isSameAs(status);
        assertThat(history.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var collaborator = collaborator();
        var status = collaborator.getLegalPersonCollaboratorStatus();
        var history = new LegalPersonCollaboratorHistory();

        history.setId(6L);
        history.setLegalPersonCollaboratorHistoryKey("other");
        history.setLegalPersonCollaborator(collaborator);
        history.setOldStatus(status);
        history.setNewStatus(status);
        history.setHistoryData("{\"a\":1}");
        history.setCreatedAt(NOW);

        assertThat(history.getId()).isEqualTo(6L);
        assertThat(history.getLegalPersonCollaboratorHistoryKey()).isEqualTo("other");
        assertThat(history.getLegalPersonCollaborator()).isSameAs(collaborator);
        assertThat(history.getOldStatus()).isSameAs(status);
        assertThat(history.getNewStatus()).isSameAs(status);
        assertThat(history.getHistoryData()).isEqualTo("{\"a\":1}");
        assertThat(history.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void fromLegalPersonCollaborator_nullOldStatus_buildsCreationSnapshot() {
        var collaborator = collaborator();

        var history = LegalPersonCollaboratorHistory.fromLegalPersonCollaborator(collaborator, null);

        assertThat(UUID.fromString(history.getLegalPersonCollaboratorHistoryKey())).isNotNull();
        assertThat(history.getLegalPersonCollaborator()).isSameAs(collaborator);
        assertThat(history.getOldStatus()).isNull();
        assertThat(history.getNewStatus()).isSameAs(collaborator.getLegalPersonCollaboratorStatus());
        assertThat(history.getHistoryData()).isEqualTo("{\"legal_person_collaborator_key\":\"collab-key\","
                + "\"legal_person_key\":\"legal-key\",\"natural_person_key\":\"owner-key\","
                + "\"collaborator_role\":\"OWNER\",\"legal_person_collaborator_status\":\"ACTIVE\"}");
    }

    @Test
    void fromLegalPersonCollaborator_withOldStatus_keepsOldStatus() {
        var collaborator = collaborator();
        var old = new LegalPersonCollaboratorStatus(2L, "DELETED", NOW);

        var history = LegalPersonCollaboratorHistory.fromLegalPersonCollaborator(collaborator, old);

        assertThat(history.getOldStatus()).isSameAs(old);
    }
}
