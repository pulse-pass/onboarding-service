package com.pulsepass.onboardingservice.domain.exceptions;

public class InvalidCollaboratorRoleException extends DomainException {

    public InvalidCollaboratorRoleException() {
        super("Collaborator role must be OWNER");
    }

    @Override
    public String getCode() {
        return "ONB00005";
    }

    @Override
    public String getMessageKey() {
        return "error.legal_person_collaborator.invalid_role";
    }
}
