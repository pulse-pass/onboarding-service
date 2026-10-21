package com.pulsepass.onboardingservice.unit.api.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.schemas.DeleteSessionSchema;
import org.junit.jupiter.api.Test;

class DeleteSessionSchemaTest {

    @Test
    void toDTO_schema_copiesRefreshToken() {
        var dto = new DeleteSessionSchema("refresh-secret").toDTO();

        assertThat(dto.refreshToken()).isEqualTo("refresh-secret");
    }
}
