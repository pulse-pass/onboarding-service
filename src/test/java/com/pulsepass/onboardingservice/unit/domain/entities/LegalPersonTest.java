package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private static Person person() {
        return new Person("key", new PersonType(1L, "LEGAL", NOW), "12345678000195", "Acme");
    }

    private static LegalPersonAddress address() {
        return new LegalPersonAddress("address-key", "90110220", "Rua Ismael Chaves Barcelos", "60", null,
                "Praia de Belas");
    }

    private static LegalPersonCompanyType companyType() {
        return new LegalPersonCompanyType(1L, "LIMITED", NOW);
    }

    @Test
    void new_withoutId_exposesValuesAndNullId() {
        var person = person();
        var address = address();
        var companyType = companyType();

        var entity = new LegalPerson(person, "acme@mail.com", address, companyType, new BigDecimal("25000.00"));

        assertThat(entity.getId()).isNull();
        assertThat(entity.getPerson()).isSameAs(person);
        assertThat(entity.getEmail()).isEqualTo("acme@mail.com");
        assertThat(entity.getLegalPersonAddress()).isSameAs(address);
        assertThat(entity.getLegalPersonCompanyType()).isSameAs(companyType);
        assertThat(entity.getMonthlyRevenueAmount()).isEqualByComparingTo("25000.00");
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void new_allArgs_exposesValues() {
        var person = person();
        var address = address();
        var companyType = companyType();

        var entity = new LegalPerson(
                7L, person, "acme@mail.com", address, companyType, new BigDecimal("25000.00"), NOW, NOW);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getPerson()).isSameAs(person);
        assertThat(entity.getEmail()).isEqualTo("acme@mail.com");
        assertThat(entity.getLegalPersonAddress()).isSameAs(address);
        assertThat(entity.getLegalPersonCompanyType()).isSameAs(companyType);
        assertThat(entity.getMonthlyRevenueAmount()).isEqualByComparingTo("25000.00");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var entity = new LegalPerson();
        var person = person();
        var address = address();
        var companyType = companyType();

        entity.setId(8L);
        entity.setPerson(person);
        entity.setEmail("other@mail.com");
        entity.setLegalPersonAddress(address);
        entity.setLegalPersonCompanyType(companyType);
        entity.setMonthlyRevenueAmount(new BigDecimal("100.50"));
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(8L);
        assertThat(entity.getPerson()).isSameAs(person);
        assertThat(entity.getEmail()).isEqualTo("other@mail.com");
        assertThat(entity.getLegalPersonAddress()).isSameAs(address);
        assertThat(entity.getLegalPersonCompanyType()).isSameAs(companyType);
        assertThat(entity.getMonthlyRevenueAmount()).isEqualByComparingTo("100.50");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }
}
