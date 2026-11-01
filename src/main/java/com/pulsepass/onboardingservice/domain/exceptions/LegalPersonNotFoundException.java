package com.pulsepass.onboardingservice.domain.exceptions;

public class LegalPersonNotFoundException extends DomainException {

    public LegalPersonNotFoundException() {
        super("Legal person not found");
    }

    @Override
    public String getCode() {
        return "ONB00009";
    }

    @Override
    public String getMessageKey() {
        return "error.legal_person.not_found";
    }
}
