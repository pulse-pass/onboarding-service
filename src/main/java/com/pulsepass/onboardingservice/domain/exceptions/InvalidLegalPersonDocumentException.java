package com.pulsepass.onboardingservice.domain.exceptions;

public class InvalidLegalPersonDocumentException extends DomainException {

    public InvalidLegalPersonDocumentException() {
        super("Legal person document number is invalid");
    }

    @Override
    public String getCode() {
        return "ONB00003";
    }

    @Override
    public String getMessageKey() {
        return "error.legal_person.invalid_document";
    }
}
