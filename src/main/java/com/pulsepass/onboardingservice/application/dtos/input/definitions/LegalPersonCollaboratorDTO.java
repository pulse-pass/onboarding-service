package com.pulsepass.onboardingservice.application.dtos.input.definitions;

public record LegalPersonCollaboratorDTO(
        String collaboratorRole,
        String documentNumber,
        String name,
        String email,
        String password
) {

    @Override
    public String toString() {
        return "LegalPersonCollaboratorDTO[collaboratorRole=" + collaboratorRole
                + ", documentNumber=" + documentNumber
                + ", name=" + name
                + ", email=" + email
                + ", password=***"
                + "]";
    }
}
