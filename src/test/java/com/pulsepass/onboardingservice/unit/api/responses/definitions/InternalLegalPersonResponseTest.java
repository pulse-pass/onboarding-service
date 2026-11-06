package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.InternalLegalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.Person;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InternalLegalPersonResponseTest {

    @Test
    void fromEntity_legalPerson_mapsAllFields() {
        var legalPerson = new LegalPerson(
                new Person("legal-key", null, "12345678000195", "Acme"),
                "acme@mail.com",
                new LegalPersonAddress("address-key", "90110220", "Rua A", "60", "Sala 2", "Centro"),
                new LegalPersonCompanyType(1L, "LIMITED", null),
                new BigDecimal("25000.00"));

        var response = InternalLegalPersonResponse.fromEntity(legalPerson);

        assertThat(response.email()).isEqualTo("acme@mail.com");
        assertThat(response.legalPersonCompanyType()).isEqualTo("LIMITED");
        assertThat(response.monthlyRevenueAmount()).isEqualByComparingTo("25000.00");
        assertThat(response.legalPersonAddress().legalPersonAddressKey()).isEqualTo("address-key");
        assertThat(response.legalPersonAddress().postalCode()).isEqualTo("90110220");
        assertThat(response.legalPersonAddress().street()).isEqualTo("Rua A");
        assertThat(response.legalPersonAddress().number()).isEqualTo("60");
        assertThat(response.legalPersonAddress().complement()).isEqualTo("Sala 2");
        assertThat(response.legalPersonAddress().neighborhood()).isEqualTo("Centro");
    }
}
