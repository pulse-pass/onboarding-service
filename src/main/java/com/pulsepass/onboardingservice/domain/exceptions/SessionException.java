package com.pulsepass.onboardingservice.domain.exceptions;

public class SessionException extends DomainException {

    public SessionException() {
        super("Session could not be handled by the identity provider");
    }

    @Override
    public String getCode() {
        return "ONB00008";
    }

    @Override
    public String getMessageKey() {
        return "error.session";
    }
}
