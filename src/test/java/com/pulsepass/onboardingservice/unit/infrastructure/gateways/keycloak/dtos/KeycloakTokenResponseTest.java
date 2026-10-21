package com.pulsepass.onboardingservice.unit.infrastructure.gateways.keycloak.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakTokenResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class KeycloakTokenResponseTest {

    @Test
    void deserialize_tokenBody_mapsSnakeCaseFieldsAndIgnoresUnknown() {
        var response = JsonMapper.builder().build().readValue(
                """
                {"access_token":"a","expires_in":300,"refresh_token":"r","refresh_expires_in":1800,
                 "token_type":"Bearer","error":"invalid_grant","scope":"profile"}
                """,
                KeycloakTokenResponse.class);

        assertThat(response.accessToken()).isEqualTo("a");
        assertThat(response.expiresIn()).isEqualTo(300);
        assertThat(response.refreshToken()).isEqualTo("r");
        assertThat(response.refreshExpiresIn()).isEqualTo(1800);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.error()).isEqualTo("invalid_grant");
    }
}
