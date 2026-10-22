package com.pulsepass.onboardingservice.unit.application.dtos.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.application.dtos.output.SessionPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionPersonDtoTest {

    @Test
    void new_allFields_exposesValues() {
        var person = new Person();
        var legalPerson = new LegalPerson();

        var dto = new SessionPersonDTO(person, List.of("organizer"), legalPerson);

        assertThat(dto.person()).isSameAs(person);
        assertThat(dto.roles()).containsExactly("organizer");
        assertThat(dto.legalPerson()).isSameAs(legalPerson);
    }
}
