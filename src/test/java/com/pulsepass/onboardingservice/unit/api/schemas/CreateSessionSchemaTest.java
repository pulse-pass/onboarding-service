package com.pulsepass.onboardingservice.unit.api.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.schemas.CreateSessionSchema;
import org.junit.jupiter.api.Test;

class CreateSessionSchemaTest {

    @Test
    void toDTO_schema_copiesAllFields() {
        var dto = new CreateSessionSchema("52998224725", "Secret123").toDTO();

        assertThat(dto.documentNumber()).isEqualTo("52998224725");
        assertThat(dto.password()).isEqualTo("Secret123");
    }
}
