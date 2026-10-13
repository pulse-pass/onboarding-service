package com.pulsepass.onboardingservice.domain.gateways.organization.dtos;

public record CreateOrganizationInputDTO(
        String legalPersonKey,
        String documentNumber,
        String name
) {
}
