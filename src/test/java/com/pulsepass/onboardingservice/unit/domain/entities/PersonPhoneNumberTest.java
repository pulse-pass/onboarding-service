package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PersonPhoneNumberTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private static Person person() {
        return new Person("key", new PersonType(1L, "LEGAL", NOW), "12345678000195", "Acme", "acme@mail.com");
    }

    @Test
    void new_withoutId_exposesValuesAndNullId() {
        var person = person();

        var phone = new PersonPhoneNumber("phone-key", person, "55", "11", "999999999");

        assertThat(phone.getId()).isNull();
        assertThat(phone.getPersonPhoneNumberKey()).isEqualTo("phone-key");
        assertThat(phone.getPerson()).isSameAs(person);
        assertThat(phone.getCountryCode()).isEqualTo("55");
        assertThat(phone.getAreaCode()).isEqualTo("11");
        assertThat(phone.getNumber()).isEqualTo("999999999");
        assertThat(phone.getCreatedAt()).isNull();
        assertThat(phone.getUpdatedAt()).isNull();
    }

    @Test
    void new_allArgs_exposesValues() {
        var phone = new PersonPhoneNumber(3L, "phone-key", person(), "55", "11", "999999999", NOW, NOW);

        assertThat(phone.getId()).isEqualTo(3L);
        assertThat(phone.getCreatedAt()).isEqualTo(NOW);
        assertThat(phone.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var phone = new PersonPhoneNumber();
        var person = person();

        phone.setId(4L);
        phone.setPersonPhoneNumberKey("other");
        phone.setPerson(person);
        phone.setCountryCode("1");
        phone.setAreaCode("21");
        phone.setNumber("88888888");
        phone.setCreatedAt(NOW);
        phone.setUpdatedAt(NOW);

        assertThat(phone.getId()).isEqualTo(4L);
        assertThat(phone.getPersonPhoneNumberKey()).isEqualTo("other");
        assertThat(phone.getPerson()).isSameAs(person);
        assertThat(phone.getCountryCode()).isEqualTo("1");
        assertThat(phone.getAreaCode()).isEqualTo("21");
        assertThat(phone.getNumber()).isEqualTo("88888888");
        assertThat(phone.getCreatedAt()).isEqualTo(NOW);
        assertThat(phone.getUpdatedAt()).isEqualTo(NOW);
    }
}
