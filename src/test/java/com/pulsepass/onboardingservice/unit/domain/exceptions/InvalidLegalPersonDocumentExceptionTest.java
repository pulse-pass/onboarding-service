package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidLegalPersonDocumentException;
import org.junit.jupiter.api.Test;

class InvalidLegalPersonDocumentExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new InvalidLegalPersonDocumentException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00003");
        assertThat(exception.getMessageKey()).isEqualTo("error.legal_person.invalid_document");
        assertThat(exception.getMessage()).isEqualTo("Legal person document number is invalid");
    }
}
