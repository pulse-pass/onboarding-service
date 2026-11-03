package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.UnauthorizedInternalRequestException;
import org.junit.jupiter.api.Test;

class UnauthorizedInternalRequestExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new UnauthorizedInternalRequestException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("GEN00401");
        assertThat(exception.getMessageKey()).isEqualTo("error.unauthorized");
        assertThat(exception.getMessage()).isEqualTo("Missing or invalid internal token");
    }
}
