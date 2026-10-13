package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatusEnum;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorStatusEnumTest {

    @Test
    void values_all_matchSeededEnumerators() {
        assertThat(LegalPersonCollaboratorStatusEnum.values()).extracting(Enum::name)
                .containsExactly("ACTIVE", "DELETED");
    }
}
