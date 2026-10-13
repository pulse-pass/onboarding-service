package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PersonTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void new_withoutId_exposesValuesAndNullId() {
        var type = new PersonType(1L, "LEGAL", NOW);

        var person = new Person("key", type, "12345678000195", "Acme", "acme@mail.com");

        assertThat(person.getId()).isNull();
        assertThat(person.getPersonKey()).isEqualTo("key");
        assertThat(person.getPersonType()).isSameAs(type);
        assertThat(person.getDocumentNumber()).isEqualTo("12345678000195");
        assertThat(person.getName()).isEqualTo("Acme");
        assertThat(person.getEmail()).isEqualTo("acme@mail.com");
        assertThat(person.getCreatedAt()).isNull();
        assertThat(person.getUpdatedAt()).isNull();
    }

    @Test
    void new_allArgs_exposesValues() {
        var person = new Person(1L, "person-key", new PersonType(1L, "LEGAL", NOW),
                "12345678000195", "Acme", "acme@mail.com", NOW, NOW);

        assertThat(person.getId()).isEqualTo(1L);
        assertThat(person.getPersonKey()).isEqualTo("person-key");
        assertThat(person.getCreatedAt()).isEqualTo(NOW);
        assertThat(person.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var person = new Person();
        var type = new PersonType(2L, "NATURAL", NOW);

        person.setId(5L);
        person.setPersonKey("other");
        person.setPersonType(type);
        person.setDocumentNumber("52998224725");
        person.setName("John");
        person.setEmail("john@mail.com");
        person.setCreatedAt(NOW);
        person.setUpdatedAt(NOW);

        assertThat(person.getId()).isEqualTo(5L);
        assertThat(person.getPersonKey()).isEqualTo("other");
        assertThat(person.getPersonType()).isSameAs(type);
        assertThat(person.getDocumentNumber()).isEqualTo("52998224725");
        assertThat(person.getName()).isEqualTo("John");
        assertThat(person.getEmail()).isEqualTo("john@mail.com");
        assertThat(person.getCreatedAt()).isEqualTo(NOW);
        assertThat(person.getUpdatedAt()).isEqualTo(NOW);
    }
}
