package com.pulsepass.onboardingservice.domain.exceptions;

public class PersonNotFoundException extends DomainException {

    public PersonNotFoundException() {
        super("Person not found");
    }

    @Override
    public String getCode() {
        return "ONB00009";
    }

    @Override
    public String getMessageKey() {
        return "error.person.not_found";
    }
}
