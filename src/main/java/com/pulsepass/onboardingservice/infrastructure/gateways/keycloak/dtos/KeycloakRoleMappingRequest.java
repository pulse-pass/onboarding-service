package com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakRoleMappingRequest(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name
) {
}
