package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonOwnerResponse;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class LegalPersonOwnerResponseTest {

    @Test
    void fromEntity_person_mapsAllFields() {
        var person = new Person("owner-key", null, "52998224725", "John Doe", "john@mail.com");

        var response = LegalPersonOwnerResponse.fromEntity(person);

        assertThat(response.personKey()).isEqualTo("owner-key");
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.documentNumber()).isEqualTo("52998224725");
        assertThat(response.email()).isEqualTo("john@mail.com");
    }
}
