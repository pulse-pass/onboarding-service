package com.pulsepass.onboardingservice.domain.exceptions;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid CPF or password");
    }

    @Override
    public String getCode() {
        return "GEN00401";
    }

    @Override
    public String getMessageKey() {
        return "error.invalid_credentials";
    }
}
