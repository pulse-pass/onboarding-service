package com.pulsepass.onboardingservice.domain.exceptions;

public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired");
    }

    @Override
    public String getCode() {
        return "GEN00401";
    }

    @Override
    public String getMessageKey() {
        return "error.invalid_refresh_token";
    }
}
