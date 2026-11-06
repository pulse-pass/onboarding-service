package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class LegalPersonResponseTest {

    @Test
    void fromEntity_legalPerson_mapsEmail() {
        var legalPerson = new LegalPerson(new Person("legal-key", null, "12345678000195", "Acme"),
                "acme@mail.com", null, null, null);

        var response = LegalPersonResponse.fromEntity(legalPerson);

        assertThat(response.email()).isEqualTo("acme@mail.com");
    }
}
