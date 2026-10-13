package com.pulsepass.onboardingservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.exceptions.DomainException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidCollaboratorRoleException;
import org.junit.jupiter.api.Test;

class InvalidCollaboratorRoleExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new InvalidCollaboratorRoleException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ONB00005");
        assertThat(exception.getMessageKey()).isEqualTo("error.legal_person_collaborator.invalid_role");
        assertThat(exception.getMessage()).isEqualTo("Collaborator role must be OWNER");
    }
}
