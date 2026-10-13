package com.pulsepass.onboardingservice.infrastructure.gateways.keycloak.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record KeycloakCreateUserRequest(
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("emailVerified") boolean emailVerified,
        @JsonProperty("attributes") Map<String, List<String>> attributes,
        @JsonProperty("credentials") List<KeycloakCredentialRequest> credentials
) {

    @Override
    public String toString() {
        return "KeycloakCreateUserRequest[username=" + username
                + ", email=" + email
                + ", firstName=" + firstName
                + ", lastName=" + lastName
                + ", enabled=" + enabled
                + ", emailVerified=" + emailVerified
                + ", attributes=" + attributes
                + ", credentials=" + credentials
                + "]";
    }
}
