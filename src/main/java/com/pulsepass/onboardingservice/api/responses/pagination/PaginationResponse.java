package com.pulsepass.onboardingservice.api.responses.pagination;

public record PaginationResponse(
        Integer page,
        Integer pageSize
) {
}
