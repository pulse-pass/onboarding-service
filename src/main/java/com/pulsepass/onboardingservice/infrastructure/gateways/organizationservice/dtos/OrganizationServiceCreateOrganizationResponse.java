package com.pulsepass.onboardingservice.infrastructure.gateways.organizationservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrganizationServiceCreateOrganizationResponse(
        @JsonProperty("organization_key") String organizationKey
) {
}
