package com.pulsepass.onboardingservice.domain.exceptions;

public class OrganizationCreationException extends DomainException {

    public OrganizationCreationException() {
        super("Organization could not be created in the organization service");
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
