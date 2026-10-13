package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.CreateLegalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import org.junit.jupiter.api.Test;

class CreateLegalPersonResponseTest {

    @Test
    void fromEntity_legalPerson_mapsAllFields() {
        var person = new Person("person-key", new PersonType(1L, "LEGAL", null),
                "12345678000195", "Acme", "acme@mail.com");

        var response = CreateLegalPersonResponse.fromEntity(new LegalPerson(person));

        assertThat(response.personKey()).isEqualTo("person-key");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.email()).isEqualTo("acme@mail.com");
    }
}
