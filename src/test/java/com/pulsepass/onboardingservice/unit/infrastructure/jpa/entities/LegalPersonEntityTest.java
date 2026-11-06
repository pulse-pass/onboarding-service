package com.pulsepass.onboardingservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.LegalPersonEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_allFields_roundTrips() {
        var domain = new LegalPerson(3L, new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW),
                new LegalPersonAddress(4L, "address-key", "90110220", "Rua Ismael Chaves Barcelos", "60",
                        "Sala 2", "Praia de Belas", NOW, NOW),
                new LegalPersonCompanyType(5L, "LIMITED", NOW), new BigDecimal("25000.00"), NOW, NOW);

        var result = LegalPersonEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
