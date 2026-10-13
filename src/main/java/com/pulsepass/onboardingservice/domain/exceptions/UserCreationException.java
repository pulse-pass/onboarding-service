package com.pulsepass.onboardingservice.domain.exceptions;

public class UserCreationException extends DomainException {

    public UserCreationException() {
        super("Failed to create user");
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
