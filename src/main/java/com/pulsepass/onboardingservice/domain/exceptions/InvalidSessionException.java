package com.pulsepass.onboardingservice.domain.exceptions;

public class InvalidSessionException extends DomainException {

    public InvalidSessionException() {
        super("Session does not match a registered person");
    }

    @Override
    public String getCode() {
        return "GEN00401";
    }

    @Override
    public String getMessageKey() {
        return "error.invalid_session";
    }
}
