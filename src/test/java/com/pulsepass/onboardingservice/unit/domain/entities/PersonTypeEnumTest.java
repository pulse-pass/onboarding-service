package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.PersonTypeEnum;
import org.junit.jupiter.api.Test;

class PersonTypeEnumTest {

    @Test
    void values_all_matchSeededEnumerators() {
        assertThat(PersonTypeEnum.values()).extracting(Enum::name)
                .containsExactly("NATURAL", "LEGAL");
    }
}
