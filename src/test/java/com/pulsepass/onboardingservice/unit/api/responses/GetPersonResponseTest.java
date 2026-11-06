package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.GetPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class GetPersonResponseTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 9, 14, 12);

    @Test
    void fromDTO_legalPerson_mapsRootAndLegalPersonWithNullNaturalPerson() {
        var company = new Person(1L, "legal-key", new PersonType(1L, "LEGAL", null),
                "12345678000195", "Acme", CREATED_AT, CREATED_AT);

        var response = GetPersonResponse.fromDTO(new GetPersonDTO(
                company, new LegalPerson(company, "acme@mail.com", null, null, null), null));

        assertThat(response.personKey()).isEqualTo("legal-key");
        assertThat(response.personType()).isEqualTo("LEGAL");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-10-09T14:12:00Z"));
        assertThat(response.legalPerson().email()).isEqualTo("acme@mail.com");
        assertThat(response.naturalPerson()).isNull();
    }

    @Test
    void fromDTO_naturalPerson_mapsRootAndNaturalPersonWithNullLegalPerson() {
        var person = new Person(2L, "user-key", new PersonType(2L, "NATURAL", null),
                "52998224725", "John Doe", CREATED_AT, CREATED_AT);

        var response = GetPersonResponse.fromDTO(new GetPersonDTO(
                person, null, new NaturalPerson(person, "john@mail.com")));

        assertThat(response.personKey()).isEqualTo("user-key");
        assertThat(response.personType()).isEqualTo("NATURAL");
        assertThat(response.legalPerson()).isNull();
        assertThat(response.naturalPerson().email()).isEqualTo("john@mail.com");
    }
}
