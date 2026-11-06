package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.GetInternalLegalPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetInternalLegalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import java.math.BigDecimal;
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
                new GetInternalLegalPersonDTO(
                        company,
                        owner,
                        phoneNumbers,
                        new LegalPersonCompanyType(1L, "LIMITED", null),
                        new BigDecimal("25000.00"),
                        new LegalPersonAddress("address-key", "90110220", "Rua A", "60", "Sala 2", "Centro")));

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
        assertThat(response.legalPersonCompanyType()).isEqualTo("LIMITED");
        assertThat(response.monthlyRevenueAmount()).isEqualByComparingTo("25000.00");
        assertThat(response.legalPersonAddress().legalPersonAddressKey()).isEqualTo("address-key");
        assertThat(response.legalPersonAddress().postalCode()).isEqualTo("90110220");
        assertThat(response.legalPersonAddress().street()).isEqualTo("Rua A");
        assertThat(response.legalPersonAddress().number()).isEqualTo("60");
        assertThat(response.legalPersonAddress().complement()).isEqualTo("Sala 2");
        assertThat(response.legalPersonAddress().neighborhood()).isEqualTo("Centro");
    }

    @Test
    void fromDTO_noPhones_returnsEmptyList() {
        var company = new Person("legal-key", null, "12345678000195", "Acme", "acme@mail.com");
        company.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        var owner = new Person("owner-key", null, "52998224725", "John Doe", "john@mail.com");

        var response = GetInternalLegalPersonResponse.fromDTO(
                new GetInternalLegalPersonDTO(
                        company,
                        owner,
                        List.of(),
                        new LegalPersonCompanyType(1L, "MEI", null),
                        new BigDecimal("100.00"),
                        new LegalPersonAddress("address-key", "90110220", "Rua A", "60", null, "Centro")));

        assertThat(response.phoneNumbers()).isEmpty();
    }
}
