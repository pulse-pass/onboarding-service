package com.pulsepass.onboardingservice.api.schemas.definitions;

import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonCollaboratorDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LegalPersonCollaboratorSchema(
        @NotBlank String collaboratorRole,
        @NotBlank @Pattern(regexp = "^\\d{11}$") String documentNumber,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) @Email String email,
        @NotBlank @Size(min = 8) String password
) {

    public LegalPersonCollaboratorDTO toDTO() {
        return new LegalPersonCollaboratorDTO(collaboratorRole, documentNumber, name, email, password);
    }

    @Override
    public String toString() {
        return "LegalPersonCollaboratorSchema[collaboratorRole=" + collaboratorRole
                + ", documentNumber=" + documentNumber
                + ", name=" + name
                + ", email=" + email
                + ", password=***"
                + "]";
    }
}
