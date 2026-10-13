package com.pulsepass.onboardingservice.domain.exceptions;

public class NaturalPersonAlreadyExistsException extends DomainException {

    public NaturalPersonAlreadyExistsException() {
        super("Natural person already exists");
    }

    @Override
    public String getCode() {
        return "ONB00002";
    }

    @Override
    public String getMessageKey() {
        return "error.natural_person.already_exists";
    }
}
