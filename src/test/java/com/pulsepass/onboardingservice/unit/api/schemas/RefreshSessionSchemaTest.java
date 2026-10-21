package com.pulsepass.onboardingservice.unit.api.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.schemas.RefreshSessionSchema;
import org.junit.jupiter.api.Test;

class RefreshSessionSchemaTest {

    @Test
    void toDTO_schema_copiesRefreshToken() {
        var dto = new RefreshSessionSchema("refresh-secret").toDTO();

        assertThat(dto.refreshToken()).isEqualTo("refresh-secret");
    }
}
