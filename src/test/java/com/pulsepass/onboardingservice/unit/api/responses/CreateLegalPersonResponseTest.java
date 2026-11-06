package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.CreateLegalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import org.junit.jupiter.api.Test;

class CreateLegalPersonResponseTest {

    @Test
    void fromEntity_legalPerson_mapsEmailAndNestedPerson() {
        var person = new Person("person-key", new PersonType(1L, "LEGAL", null),
                "12345678000195", "Acme");

        var response = CreateLegalPersonResponse.fromEntity(new LegalPerson(person, "acme@mail.com", null, null, null));

        assertThat(response.email()).isEqualTo("acme@mail.com");
        assertThat(response.person().personKey()).isEqualTo("person-key");
        assertThat(response.person().documentNumber()).isEqualTo("12345678000195");
        assertThat(response.person().name()).isEqualTo("Acme");
    }
}
