package com.pulsepass.onboardingservice.unit.infrastructure.gateways.keycloak.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakCredentialRequest;
import org.junit.jupiter.api.Test;

class KeycloakCredentialRequestTest {

    @Test
    void toString_credential_masksPassword() {
        var credential = new KeycloakCredentialRequest("password", "Secret123", false);

        assertThat(credential.toString())
                .contains("type=password", "value=***", "temporary=false")
                .doesNotContain("Secret123");
    }
}
