package com.pulsepass.onboardingservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.GetInternalPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetInternalPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetInternalPersonResponseTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 9, 14, 12);

    @Test
    void fromDTO_legalPerson_mapsRootPhonesAndLegalPersonWithNullNaturalPerson() {
        var company = new Person(1L, "legal-key", new PersonType(1L, "LEGAL", null),
                "12345678000195", "Acme", CREATED_AT, CREATED_AT);
        var legalPerson = new LegalPerson(
                company,
                "acme@mail.com",
                new LegalPersonAddress("address-key", "90110220", "Rua A", "60", "Sala 2", "Centro"),
                new LegalPersonCompanyType(1L, "LIMITED", null),
                new BigDecimal("25000.00"));
        var phoneNumbers = List.of(
                new PersonPhoneNumber("phone-1", company, "55", "51", "999990000"),
                new PersonPhoneNumber("phone-2", company, "55", "11", "999999999")
        );

        var response = GetInternalPersonResponse.fromDTO(
                new GetInternalPersonDTO(company, phoneNumbers, legalPerson, null));

        assertThat(response.personKey()).isEqualTo("legal-key");
        assertThat(response.personType()).isEqualTo("LEGAL");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-10-09T14:12:00Z"));
        assertThat(response.phoneNumbers()).hasSize(2);
        assertThat(response.phoneNumbers().get(0).areaCode()).isEqualTo("51");
        assertThat(response.phoneNumbers().get(1).number()).isEqualTo("999999999");
        assertThat(response.legalPerson().email()).isEqualTo("acme@mail.com");
        assertThat(response.legalPerson().legalPersonCompanyType()).isEqualTo("LIMITED");
        assertThat(response.legalPerson().monthlyRevenueAmount()).isEqualByComparingTo("25000.00");
        assertThat(response.legalPerson().legalPersonAddress().legalPersonAddressKey()).isEqualTo("address-key");
        assertThat(response.naturalPerson()).isNull();
    }

    @Test
    void fromDTO_naturalPerson_mapsRootPhonesAndNaturalPersonWithNullLegalPerson() {
        var person = new Person(2L, "user-key", new PersonType(2L, "NATURAL", null),
                "52998224725", "John Doe", CREATED_AT, CREATED_AT);
        var phoneNumbers = List.of(new PersonPhoneNumber("phone-1", person, "55", "51", "999990000"));

        var response = GetInternalPersonResponse.fromDTO(new GetInternalPersonDTO(
                person, phoneNumbers, null, new NaturalPerson(person, "john@mail.com")));

        assertThat(response.personType()).isEqualTo("NATURAL");
        assertThat(response.phoneNumbers()).hasSize(1);
        assertThat(response.legalPerson()).isNull();
        assertThat(response.naturalPerson().email()).isEqualTo("john@mail.com");
    }

    @Test
    void fromDTO_noPhones_returnsEmptyList() {
        var person = new Person(2L, "user-key", new PersonType(2L, "NATURAL", null),
                "52998224725", "John Doe", CREATED_AT, CREATED_AT);

        var response = GetInternalPersonResponse.fromDTO(new GetInternalPersonDTO(
                person, List.of(), null, new NaturalPerson(person, "john@mail.com")));

        assertThat(response.phoneNumbers()).isEmpty();
    }
}
