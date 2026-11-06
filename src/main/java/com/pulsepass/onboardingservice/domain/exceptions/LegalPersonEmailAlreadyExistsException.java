package com.pulsepass.onboardingservice.domain.exceptions;

public class LegalPersonEmailAlreadyExistsException extends DomainException {

    public LegalPersonEmailAlreadyExistsException() {
        super("Legal person email already exists");
    }

    @Override
    public String getCode() {
        return "ONB00010";
    }

    @Override
    public String getMessageKey() {
        return "error.legal_person.email_already_exists";
    }
}
