package com.pulsepass.onboardingservice.api.exceptions;

import java.util.List;

public record ErrorResponse(
        String title,
        String message,
        String code,
        List<ExtraFieldErrorResponse> extraFields
) {
}
