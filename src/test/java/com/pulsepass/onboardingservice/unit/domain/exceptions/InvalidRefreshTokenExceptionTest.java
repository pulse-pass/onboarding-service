package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidRefreshTokenException;
import org.junit.jupiter.api.Test;

class InvalidRefreshTokenExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new InvalidRefreshTokenException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("GEN00401");
        assertThat(exception.getMessageKey()).isEqualTo("error.invalid_refresh_token");
        assertThat(exception.getMessage()).isEqualTo("Refresh token is invalid or expired");
    }
}
