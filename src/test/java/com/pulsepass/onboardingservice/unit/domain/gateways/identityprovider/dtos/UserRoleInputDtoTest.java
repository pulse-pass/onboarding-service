package com.pulsepass.onboardingservice.unit.domain.gateways.identityprovider.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.UserRoleInputDTO;
import org.junit.jupiter.api.Test;

class UserRoleInputDtoTest {

    @Test
    void values_all_matchSeededEnumerators() {
        assertThat(UserRoleInputDTO.values()).extracting(Enum::name)
                .containsExactly("BUYER", "ORGANIZER", "ADMIN");
    }
}
