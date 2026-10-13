package com.pulsepass.onboardingservice.unit.domain.gateways.identityprovider.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.UserRoleInputDTO;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateUserInputDtoTest {

    @Test
    void toString_dto_masksPassword() {
        var dto = new CreateUserInputDTO(
                "52998224725", "John Doe", "john@mail.com", "Secret123", "person-key",
                List.of(UserRoleInputDTO.ORGANIZER));

        assertThat(dto.toString()).contains("password=***").doesNotContain("Secret123");
    }
}
