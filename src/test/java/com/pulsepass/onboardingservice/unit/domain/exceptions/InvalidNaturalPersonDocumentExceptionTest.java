package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidNaturalPersonDocumentException;
import org.junit.jupiter.api.Test;

class InvalidNaturalPersonDocumentExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new InvalidNaturalPersonDocumentException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00004");
        assertThat(exception.getMessageKey()).isEqualTo("error.natural_person.invalid_document");
        assertThat(exception.getMessage()).isEqualTo("Natural person document number is invalid");
    }
}
