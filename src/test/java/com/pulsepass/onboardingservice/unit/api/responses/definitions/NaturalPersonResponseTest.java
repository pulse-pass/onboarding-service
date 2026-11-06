package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.NaturalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class NaturalPersonResponseTest {

    @Test
    void fromEntity_naturalPerson_mapsEmail() {
        var response = NaturalPersonResponse.fromEntity(new NaturalPerson(new Person(), "john@mail.com"));

        assertThat(response.email()).isEqualTo("john@mail.com");
    }
}
