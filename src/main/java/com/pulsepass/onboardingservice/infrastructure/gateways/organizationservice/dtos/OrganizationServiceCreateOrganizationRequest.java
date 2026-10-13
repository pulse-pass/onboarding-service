package com.pulsepass.onboardingservice.infrastructure.gateways.organizationservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrganizationServiceCreateOrganizationRequest(
        @JsonProperty("legal_person_key") String legalPersonKey,
        @JsonProperty("document_number") String documentNumber,
        @JsonProperty("name") String name
) {
}
