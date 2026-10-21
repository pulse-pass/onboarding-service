package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;

public record SessionResponse(
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        String tokenType
) {

    public static SessionResponse fromSessionOutput(SessionOutputDTO session) {
        return new SessionResponse(
                session.accessToken(),
                session.expiresIn(),
                session.refreshToken(),
                session.refreshExpiresIn(),
                session.tokenType()
        );
    }
}
