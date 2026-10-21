package com.pulsepass.onboardingservice.api.schemas;

import com.pulsepass.onboardingservice.application.dtos.input.DeleteSessionDTO;
import jakarta.validation.constraints.NotBlank;

public record DeleteSessionSchema(
        @NotBlank String refreshToken
) {

    public DeleteSessionDTO toDTO() {
        return new DeleteSessionDTO(refreshToken);
    }
}
