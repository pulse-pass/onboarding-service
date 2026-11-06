package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.PersonResponse;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class PersonResponseTest {

    @Test
    void fromEntity_person_mapsAllFields() {
        var response = PersonResponse.fromEntity(new Person("person-key", null, "12345678000195", "Acme"));

        assertThat(response.personKey()).isEqualTo("person-key");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.name()).isEqualTo("Acme");
    }
}
