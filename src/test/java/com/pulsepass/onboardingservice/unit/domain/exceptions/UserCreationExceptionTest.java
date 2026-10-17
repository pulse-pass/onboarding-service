package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.UserCreationException;
import org.junit.jupiter.api.Test;

class UserCreationExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new UserCreationException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00006");
        assertThat(exception.getMessageKey()).isEqualTo("error.user.creation");
        assertThat(exception.getMessage()).isEqualTo("User could not be created in the identity provider");
    }
}
