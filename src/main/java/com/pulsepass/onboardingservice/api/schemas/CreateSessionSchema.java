package com.pulsepass.onboardingservice.api.schemas;

import com.pulsepass.onboardingservice.application.dtos.input.CreateSessionDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateSessionSchema(
        @NotBlank @Pattern(regexp = "^\\d{11}$") String documentNumber,
        @NotBlank String password
) {

    public CreateSessionDTO toDTO() {
        return new CreateSessionDTO(documentNumber, password);
    }
}
