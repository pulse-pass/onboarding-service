package com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakCredentialRequest(
        @JsonProperty("type") String type,
        @JsonProperty("value") String value,
        @JsonProperty("temporary") boolean temporary
) {

    @Override
    public String toString() {
        return "KeycloakCredentialRequest[type=" + type
                + ", value=***"
                + ", temporary=" + temporary
                + "]";
    }
}
