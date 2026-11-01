package com.pulsepass.onboardingservice.api.responses.pagination;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> data,
        PaginationResponse pagination
) {
}
