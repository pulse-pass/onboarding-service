package com.pulsepass.onboardingservice.unit.application.dtos.input.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonCollaboratorDTO;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorDtoTest {

    @Test
    void toString_dto_masksPassword() {
        var dto = new LegalPersonCollaboratorDTO("OWNER", "52998224725", "John Doe", "john@mail.com", "Secret123");

        assertThat(dto.toString()).contains("password=***").doesNotContain("Secret123");
    }
}
