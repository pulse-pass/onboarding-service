package com.pulsepass.onboardingservice.domain.exceptions;

public class UserCreationException extends DomainException {

    public UserCreationException() {
        super("User could not be created in the identity provider");
    }

    @Override
    public String getCode() {
        return "ONB00006";
    }

    @Override
    public String getMessageKey() {
        return "error.user.creation";
    }
}
