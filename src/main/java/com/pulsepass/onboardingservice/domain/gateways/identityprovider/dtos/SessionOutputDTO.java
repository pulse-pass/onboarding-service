package com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos;

public record SessionOutputDTO(
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        String tokenType
) {
}
