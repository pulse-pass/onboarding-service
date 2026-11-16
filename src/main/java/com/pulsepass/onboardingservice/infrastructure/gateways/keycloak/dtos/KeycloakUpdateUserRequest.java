package com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakUpdateUserRequest(
        @JsonProperty("enabled") boolean enabled
) {
}
