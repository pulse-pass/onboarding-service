package com.pulsepass.onboardingservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.infrastructure.jpa.entities.PersonPhoneNumberEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PersonPhoneNumberEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_allFields_roundTrips() {
        var person = new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW);
        var domain = new PersonPhoneNumber(5L, "phone-key", person, "55", "11", "999999999", NOW, NOW);

        var result = PersonPhoneNumberEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
