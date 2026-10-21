package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.SessionException;
import org.junit.jupiter.api.Test;

class SessionExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new SessionException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00008");
        assertThat(exception.getMessageKey()).isEqualTo("error.session");
        assertThat(exception.getMessage()).isEqualTo("Session could not be handled by the identity provider");
    }
}
