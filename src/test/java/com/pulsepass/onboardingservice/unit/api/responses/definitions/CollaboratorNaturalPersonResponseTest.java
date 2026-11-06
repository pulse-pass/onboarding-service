package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.CollaboratorNaturalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class CollaboratorNaturalPersonResponseTest {

    @Test
    void fromEntity_naturalPerson_mapsEmailAndNestedPerson() {
        var naturalPerson = new NaturalPerson(
                new Person("owner-key", null, "52998224725", "John Doe"), "john@mail.com");

        var response = CollaboratorNaturalPersonResponse.fromEntity(naturalPerson);

        assertThat(response.email()).isEqualTo("john@mail.com");
        assertThat(response.person().personKey()).isEqualTo("owner-key");
        assertThat(response.person().name()).isEqualTo("John Doe");
        assertThat(response.person().documentNumber()).isEqualTo("52998224725");
    }
}
