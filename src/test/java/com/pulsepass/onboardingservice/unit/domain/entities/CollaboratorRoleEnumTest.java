package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import org.junit.jupiter.api.Test;

class CollaboratorRoleEnumTest {

    @Test
    void values_all_matchSeededEnumerators() {
        assertThat(CollaboratorRoleEnum.values()).extracting(Enum::name)
                .containsExactly("OWNER", "MEMBER");
    }
}
