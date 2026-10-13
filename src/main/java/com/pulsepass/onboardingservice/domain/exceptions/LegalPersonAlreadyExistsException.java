package com.pulsepass.onboardingservice.domain.exceptions;

public class LegalPersonAlreadyExistsException extends DomainException {

    public LegalPersonAlreadyExistsException() {
        super("Legal person already exists");
    }

    @Override
    public String getCode() {
        return "ONB00001";
    }

    @Override
    public String getMessageKey() {
        return "error.legal_person.already_exists";
    }
}
