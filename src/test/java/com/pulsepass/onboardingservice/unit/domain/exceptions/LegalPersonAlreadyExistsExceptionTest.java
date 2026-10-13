package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.LegalPersonAlreadyExistsException;
import org.junit.jupiter.api.Test;

class LegalPersonAlreadyExistsExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new LegalPersonAlreadyExistsException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00001");
        assertThat(exception.getMessageKey()).isEqualTo("error.legal_person.already_exists");
        assertThat(exception.getMessage()).isEqualTo("Legal person already exists");
    }
}
