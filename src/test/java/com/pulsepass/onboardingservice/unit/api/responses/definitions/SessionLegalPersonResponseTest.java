package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.SessionLegalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class SessionLegalPersonResponseTest {

    @Test
    void fromEntity_legalPerson_mapsKeyAndName() {
        var person = new Person("legal-person-key", null, "12345678000195", "Acme", "acme@mail.com");

        var response = SessionLegalPersonResponse.fromEntity(new LegalPerson(person));

        assertThat(response.legalPersonKey()).isEqualTo("legal-person-key");
        assertThat(response.name()).isEqualTo("Acme");
    }
}
