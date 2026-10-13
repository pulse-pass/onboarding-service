package com.pulsepass.onboardingservice.unit.infrastructure.gateways.keycloak.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakCreateUserRequest;
import com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos.KeycloakCredentialRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KeycloakCreateUserRequestTest {

    @Test
    void toString_requestWithCredentials_masksPassword() {
        var request = new KeycloakCreateUserRequest(
                "52998224725", "john@mail.com", "John", "Doe", true, true,
                Map.of("person_key", List.of("abc")),
                List.of(new KeycloakCredentialRequest("password", "Secret123", false)));

        assertThat(request.toString())
                .contains("username=52998224725", "value=***")
                .doesNotContain("Secret123");
    }
}
