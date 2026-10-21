package com.pulsepass.onboardingservice.api.schemas;

import com.pulsepass.onboardingservice.application.dtos.input.RefreshSessionDTO;
import jakarta.validation.constraints.NotBlank;

public record RefreshSessionSchema(
        @NotBlank String refreshToken
) {

    public RefreshSessionDTO toDTO() {
        return new RefreshSessionDTO(refreshToken);
    }
}
