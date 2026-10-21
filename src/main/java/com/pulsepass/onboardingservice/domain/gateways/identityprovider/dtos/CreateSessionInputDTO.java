package com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos;

public record CreateSessionInputDTO(
        String documentNumber,
        String password
) {
}
