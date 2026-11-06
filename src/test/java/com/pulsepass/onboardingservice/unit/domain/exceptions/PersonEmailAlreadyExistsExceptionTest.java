package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.PersonEmailAlreadyExistsException;
import org.junit.jupiter.api.Test;

class PersonEmailAlreadyExistsExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new PersonEmailAlreadyExistsException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00010");
        assertThat(exception.getMessageKey()).isEqualTo("error.person.email_already_exists");
        assertThat(exception.getMessage()).isEqualTo("Person email already exists");
    }
}
