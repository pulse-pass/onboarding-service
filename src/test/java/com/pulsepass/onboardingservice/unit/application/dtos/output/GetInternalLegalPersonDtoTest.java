package com.pulsepass.onboardingservice.unit.application.dtos.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.application.dtos.output.GetInternalLegalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetInternalLegalPersonDtoTest {

    @Test
    void new_allFields_exposesValues() {
        var legalPerson = new Person();
        var owner = new Person();
        var phoneNumbers = List.of(new PersonPhoneNumber());

        var dto = new GetInternalLegalPersonDTO(legalPerson, owner, phoneNumbers);

        assertThat(dto.legalPerson()).isSameAs(legalPerson);
        assertThat(dto.owner()).isSameAs(owner);
        assertThat(dto.phoneNumbers()).isSameAs(phoneNumbers);
    }
}
