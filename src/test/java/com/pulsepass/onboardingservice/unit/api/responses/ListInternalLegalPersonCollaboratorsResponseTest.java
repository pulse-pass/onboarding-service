package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.ListInternalLegalPersonCollaboratorsResponse;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class ListInternalLegalPersonCollaboratorsResponseTest {

    @Test
    void fromEntity_legalPersonCollaborator_mapsAllFieldsAndNestedNaturalPerson() {
        var legalPerson = new LegalPerson(new Person("legal-key", null, "12345678000195", "Acme"),
                "acme@mail.com", null, null, null);
        var naturalPerson = new NaturalPerson(
                new Person("owner-key", null, "52998224725", "John Doe"), "john@mail.com");
        var collaborator = new LegalPersonCollaborator("collaborator-key", legalPerson, naturalPerson,
                new CollaboratorRole(1L, "OWNER", null), new LegalPersonCollaboratorStatus(1L, "ACTIVE", null));

        var response = ListInternalLegalPersonCollaboratorsResponse.fromEntity(collaborator);

        assertThat(response.legalPersonCollaboratorKey()).isEqualTo("collaborator-key");
        assertThat(response.collaboratorRole()).isEqualTo("OWNER");
        assertThat(response.legalPersonCollaboratorStatus()).isEqualTo("ACTIVE");
        assertThat(response.naturalPerson().email()).isEqualTo("john@mail.com");
        assertThat(response.naturalPerson().person().personKey()).isEqualTo("owner-key");
        assertThat(response.naturalPerson().person().name()).isEqualTo("John Doe");
        assertThat(response.naturalPerson().person().documentNumber()).isEqualTo("52998224725");
    }
}
