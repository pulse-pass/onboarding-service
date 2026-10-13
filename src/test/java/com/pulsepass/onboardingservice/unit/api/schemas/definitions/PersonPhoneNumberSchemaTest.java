package com.pulsepass.onboardingservice.unit.api.schemas.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.schemas.definitions.PersonPhoneNumberSchema;
import org.junit.jupiter.api.Test;

class PersonPhoneNumberSchemaTest {

    @Test
    void toDTO_schema_copiesAllFields() {
        var dto = new PersonPhoneNumberSchema("55", "11", "999999999").toDTO();

        assertThat(dto.countryCode()).isEqualTo("55");
        assertThat(dto.areaCode()).isEqualTo("11");
        assertThat(dto.number()).isEqualTo("999999999");
    }
}
