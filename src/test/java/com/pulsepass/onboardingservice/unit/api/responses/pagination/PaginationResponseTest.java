package com.pulsepass.onboardingservice.unit.api.responses.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.pagination.PaginationResponse;
import org.junit.jupiter.api.Test;

class PaginationResponseTest {

    @Test
    void new_pageAndPageSize_exposesValues() {
        var response = new PaginationResponse(2, 10);

        assertThat(response.page()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(10);
    }
}
