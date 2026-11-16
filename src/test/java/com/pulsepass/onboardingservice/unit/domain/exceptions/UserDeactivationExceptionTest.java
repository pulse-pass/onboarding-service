package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.UserDeactivationException;
import org.junit.jupiter.api.Test;

class UserDeactivationExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new UserDeactivationException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00011");
        assertThat(exception.getMessageKey()).isEqualTo("error.user.deactivation");
        assertThat(exception.getMessage()).isEqualTo("User could not be deactivated in the identity provider");
    }
}
