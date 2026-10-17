package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.OrganizationCreationException;
import org.junit.jupiter.api.Test;

class OrganizationCreationExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new OrganizationCreationException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00007");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.creation");
        assertThat(exception.getMessage()).isEqualTo("Organization could not be created in the organization service");
    }
}
