package com.pulsepass.onboardingservice.unit.application.dtos.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.application.dtos.output.GetPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import org.junit.jupiter.api.Test;

class GetPersonDtoTest {

    @Test
    void new_allFields_exposesValues() {
        var person = new Person();
        var legalPerson = new LegalPerson();
        var naturalPerson = new NaturalPerson();

        var dto = new GetPersonDTO(person, legalPerson, naturalPerson);

        assertThat(dto.person()).isSameAs(person);
        assertThat(dto.legalPerson()).isSameAs(legalPerson);
        assertThat(dto.naturalPerson()).isSameAs(naturalPerson);
    }
}
