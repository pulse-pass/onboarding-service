package com.pulsepass.onboardingservice.unit.application.dtos.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.application.dtos.output.GetInternalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetInternalPersonDtoTest {

    @Test
    void new_allFields_exposesValues() {
        var person = new Person();
        var phoneNumbers = List.of(new PersonPhoneNumber());
        var legalPerson = new LegalPerson();
        var naturalPerson = new NaturalPerson();

        var dto = new GetInternalPersonDTO(person, phoneNumbers, legalPerson, naturalPerson);

        assertThat(dto.person()).isSameAs(person);
        assertThat(dto.phoneNumbers()).isSameAs(phoneNumbers);
        assertThat(dto.legalPerson()).isSameAs(legalPerson);
        assertThat(dto.naturalPerson()).isSameAs(naturalPerson);
    }
}
