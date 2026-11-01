package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.GetLegalPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetLegalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class GetLegalPersonResponseTest {

    @Test
    void fromDTO_legalPersonAndOwner_mapsAllFields() {
        var createdAt = LocalDateTime.of(2026, 10, 9, 14, 12);
        var company = new Person(
                1L, "legal-key", null, "12345678000195", "Acme", "acme@mail.com", createdAt, createdAt
        );
        var owner = new Person("owner-key", null, "52998224725", "John Doe", "john@mail.com");

        var response = GetLegalPersonResponse.fromDTO(
                new GetLegalPersonDTO(company, owner));

        assertThat(response.legalPersonKey()).isEqualTo("legal-key");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.email()).isEqualTo("acme@mail.com");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.owner().personKey()).isEqualTo("owner-key");
        assertThat(response.owner().name()).isEqualTo("John Doe");
    }
}
