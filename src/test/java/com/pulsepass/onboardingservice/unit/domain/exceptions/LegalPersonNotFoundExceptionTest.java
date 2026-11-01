package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.LegalPersonNotFoundException;
import org.junit.jupiter.api.Test;

class LegalPersonNotFoundExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new LegalPersonNotFoundException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00009");
        assertThat(exception.getMessageKey()).isEqualTo("error.legal_person.not_found");
        assertThat(exception.getMessage()).isEqualTo("Legal person not found");
    }
}
