package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.PersonPhoneNumberResponse;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import org.junit.jupiter.api.Test;

class PersonPhoneNumberResponseTest {

    @Test
    void fromEntity_phoneNumber_mapsAllFields() {
        var phoneNumber = new PersonPhoneNumber("phone-key", null, "55", "51", "999990000");

        var response = PersonPhoneNumberResponse.fromEntity(phoneNumber);

        assertThat(response.countryCode()).isEqualTo("55");
        assertThat(response.areaCode()).isEqualTo("51");
        assertThat(response.number()).isEqualTo("999990000");
    }
}
