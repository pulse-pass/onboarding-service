package com.pulsepass.onboardingservice.domain.exceptions;

public class OrganizationCreationException extends DomainException {

    public OrganizationCreationException() {
        super("Failed to create organization");
    }

    @Override
    public String getCode() {
        return "ONB00007";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.creation";
    }
}
