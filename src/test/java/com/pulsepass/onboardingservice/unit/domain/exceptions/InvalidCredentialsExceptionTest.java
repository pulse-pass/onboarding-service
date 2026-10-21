package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.Test;

class InvalidCredentialsExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new InvalidCredentialsException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("GEN00401");
        assertThat(exception.getMessageKey()).isEqualTo("error.invalid_credentials");
        assertThat(exception.getMessage()).isEqualTo("Invalid CPF or password");
    }
}
