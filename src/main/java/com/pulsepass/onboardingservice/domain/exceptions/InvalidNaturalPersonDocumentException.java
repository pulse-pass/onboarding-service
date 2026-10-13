package com.pulsepass.onboardingservice.domain.exceptions;

public class InvalidNaturalPersonDocumentException extends DomainException {

    public InvalidNaturalPersonDocumentException() {
        super("Natural person document number is invalid");
    }

    @Override
    public String getCode() {
        return "ONB00004";
    }

    @Override
    public String getMessageKey() {
        return "error.natural_person.invalid_document";
    }
}
