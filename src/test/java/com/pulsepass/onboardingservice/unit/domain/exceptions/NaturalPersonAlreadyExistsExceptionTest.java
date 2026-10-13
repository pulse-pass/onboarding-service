package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.NaturalPersonAlreadyExistsException;
import org.junit.jupiter.api.Test;

class NaturalPersonAlreadyExistsExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new NaturalPersonAlreadyExistsException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00002");
        assertThat(exception.getMessageKey()).isEqualTo("error.natural_person.already_exists");
        assertThat(exception.getMessage()).isEqualTo("Natural person already exists");
    }
}
