package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;

public record CreateSessionResponse(
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        String tokenType
) {

    public static CreateSessionResponse fromSessionOutput(SessionOutputDTO session) {
        return new CreateSessionResponse(
                session.accessToken(),
                session.expiresIn(),
                session.refreshToken(),
                session.refreshExpiresIn(),
                session.tokenType()
        );
    }
}
