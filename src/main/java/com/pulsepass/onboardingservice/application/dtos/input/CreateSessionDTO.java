package com.pulsepass.onboardingservice.application.dtos.input;

public record CreateSessionDTO(
        String documentNumber,
        String password
) {
}
