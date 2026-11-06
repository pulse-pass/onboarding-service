package com.pulsepass.onboardingservice.domain.exceptions;

public class PersonEmailAlreadyExistsException extends DomainException {

    public PersonEmailAlreadyExistsException() {
        super("Person email already exists");
    }

    @Override
    public String getCode() {
        return "ONB00010";
    }

    @Override
    public String getMessageKey() {
        return "error.person.email_already_exists";
    }
}
