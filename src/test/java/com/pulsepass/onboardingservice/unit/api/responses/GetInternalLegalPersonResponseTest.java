package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.GetInternalLegalPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetInternalLegalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetInternalLegalPersonResponseTest {

    @Test
    void fromDTO_legalPersonOwnerAndPhones_mapsAllFields() {
        var createdAt = LocalDateTime.of(2026, 10, 9, 14, 12);
        var company = new Person(
                1L, "legal-key", null, "12345678000195", "Acme", "acme@mail.com", createdAt, createdAt
        );
        var owner = new Person("owner-key", null, "52998224725", "John Doe", "john@mail.com");
        var phoneNumbers = List.of(
                new PersonPhoneNumber("phone-1", company, "55", "51", "30224180"),
                new PersonPhoneNumber("phone-2", company, "55", "11", "999999999")
        );

        var response = GetInternalLegalPersonResponse.fromDTO(
                new GetInternalLegalPersonDTO(company, owner, phoneNumbers));

        assertThat(response.legalPersonKey()).isEqualTo("legal-key");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.email()).isEqualTo("acme@mail.com");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-10-09T14:12:00Z"));
        assertThat(response.phoneNumbers()).hasSize(2);
        assertThat(response.phoneNumbers().get(0).areaCode()).isEqualTo("51");
        assertThat(response.phoneNumbers().get(1).number()).isEqualTo("999999999");
        assertThat(response.owner().personKey()).isEqualTo("owner-key");
        assertThat(response.owner().name()).isEqualTo("John Doe");
    }

    @Test
    void fromDTO_noPhones_returnsEmptyList() {
        var company = new Person("legal-key", null, "12345678000195", "Acme", "acme@mail.com");
        company.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        var owner = new Person("owner-key", null, "52998224725", "John Doe", "john@mail.com");

        var response = GetInternalLegalPersonResponse.fromDTO(
                new GetInternalLegalPersonDTO(company, owner, List.of()));

        assertThat(response.phoneNumbers()).isEmpty();
    }
}
