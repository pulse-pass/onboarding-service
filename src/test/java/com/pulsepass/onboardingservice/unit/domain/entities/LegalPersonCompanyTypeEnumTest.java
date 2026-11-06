package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyTypeEnum;
import org.junit.jupiter.api.Test;

class LegalPersonCompanyTypeEnumTest {

    @Test
    void values_all_matchSeededEnumerators() {
        assertThat(LegalPersonCompanyTypeEnum.values()).extracting(Enum::name)
                .containsExactly("MEI", "LIMITED", "INDIVIDUAL", "ASSOCIATION");
    }
}
