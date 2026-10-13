package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NaturalPersonTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private static Person person() {
        return new Person("key", new PersonType(1L, "NATURAL", NOW), "12345678000195", "Acme", "acme@mail.com");
    }

    @Test
    void new_personOnly_exposesPersonAndNullId() {
        var person = person();

        var entity = new NaturalPerson(person);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getPerson()).isSameAs(person);
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void new_allArgs_exposesValues() {
        var person = person();

        var entity = new NaturalPerson(7L, person, NOW, NOW);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getPerson()).isSameAs(person);
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var entity = new NaturalPerson();
        var person = person();

        entity.setId(8L);
        entity.setPerson(person);
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(8L);
        assertThat(entity.getPerson()).isSameAs(person);
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }
}
