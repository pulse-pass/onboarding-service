package com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakUserResponse(
        @JsonProperty("id") String id
) {
}
