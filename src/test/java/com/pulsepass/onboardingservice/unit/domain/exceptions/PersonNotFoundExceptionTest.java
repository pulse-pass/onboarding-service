package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import org.junit.jupiter.api.Test;

class PersonNotFoundExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new PersonNotFoundException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00009");
        assertThat(exception.getMessageKey()).isEqualTo("error.person.not_found");
        assertThat(exception.getMessage()).isEqualTo("Person not found");
    }
}
