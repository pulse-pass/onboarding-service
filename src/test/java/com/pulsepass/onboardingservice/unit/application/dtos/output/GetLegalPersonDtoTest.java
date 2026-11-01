package com.pulsepass.onboardingservice.unit.application.dtos.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.application.dtos.output.GetLegalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class GetLegalPersonDtoTest {

    @Test
    void new_allFields_exposesValues() {
        var legalPerson = new Person();
        var owner = new Person();

        var dto = new GetLegalPersonDTO(legalPerson, owner);

        assertThat(dto.legalPerson()).isSameAs(legalPerson);
        assertThat(dto.owner()).isSameAs(owner);
    }
}
