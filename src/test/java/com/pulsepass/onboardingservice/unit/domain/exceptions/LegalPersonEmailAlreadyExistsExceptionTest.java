package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.LegalPersonEmailAlreadyExistsException;
import org.junit.jupiter.api.Test;

class LegalPersonEmailAlreadyExistsExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new LegalPersonEmailAlreadyExistsException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00010");
        assertThat(exception.getMessageKey()).isEqualTo("error.legal_person.email_already_exists");
        assertThat(exception.getMessage()).isEqualTo("Legal person email already exists");
    }
}
