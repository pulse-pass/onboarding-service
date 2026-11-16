package com.pulsepass.onboardingservice.domain.exceptions;

public class UserDeactivationException extends DomainException {

    public UserDeactivationException() {
        super("User could not be deactivated in the identity provider");
    }

    @Override
    public String getCode() {
        return "ONB00011";
    }

    @Override
    public String getMessageKey() {
        return "error.user.deactivation";
    }
}
