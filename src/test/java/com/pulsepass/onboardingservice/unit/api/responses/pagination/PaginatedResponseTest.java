package com.pulsepass.onboardingservice.unit.api.responses.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.PersonResponse;
import com.pulsepass.onboardingservice.api.responses.pagination.PaginatedResponse;
import com.pulsepass.onboardingservice.api.responses.pagination.PaginationResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class PaginatedResponseTest {

    @Test
    void new_dataAndPagination_exposesValues() {
        var item = new PersonResponse("key", "12345678000195", "Acme");
        var pagination = new PaginationResponse(1, 30);

        var response = new PaginatedResponse<>(List.of(item), pagination);

        assertThat(response.data()).containsExactly(item);
        assertThat(response.pagination()).isSameAs(pagination);
    }
}
