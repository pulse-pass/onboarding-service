package com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos;

public record DeactivateUserInputDTO(
        String personKey,
        String username
) {
}
