package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.SessionPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.SessionPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionPersonResponseTest {

    private static final Person OWNER = new Person("person-key", null, "52998224725", "Bianca Torres", "b@mail.com");

    @Test
    void fromDTO_withLegalPerson_mapsAllFields() {
        var company = new Person("legal-person-key", null, "12345678000195", "Acme", "acme@mail.com");

        var response = SessionPersonResponse.fromDTO(
                new SessionPersonDTO(OWNER, List.of("organizer"), new LegalPerson(company)));

        assertThat(response.personKey()).isEqualTo("person-key");
        assertThat(response.name()).isEqualTo("Bianca Torres");
        assertThat(response.documentNumber()).isEqualTo("52998224725");
        assertThat(response.roles()).containsExactly("organizer");
        assertThat(response.legalPerson().legalPersonKey()).isEqualTo("legal-person-key");
        assertThat(response.legalPerson().name()).isEqualTo("Acme");
    }

    @Test
    void fromDTO_withoutLegalPerson_mapsNullLegalPerson() {
        var response = SessionPersonResponse.fromDTO(
                new SessionPersonDTO(OWNER, List.of("admin"), null));

        assertThat(response.personKey()).isEqualTo("person-key");
        assertThat(response.roles()).containsExactly("admin");
        assertThat(response.legalPerson()).isNull();
    }
}
