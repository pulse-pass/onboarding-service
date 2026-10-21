package com.pulsepass.onboardingservice.application.dtos.input.definitions;

public record LegalPersonCollaboratorDTO(
        String collaboratorRole,
        String documentNumber,
        String name,
        String email,
        String password
) {
}
