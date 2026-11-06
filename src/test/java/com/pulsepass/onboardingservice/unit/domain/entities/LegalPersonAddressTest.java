package com.pulsepass.onboardingservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LegalPersonAddressTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void new_withoutId_exposesValuesAndNullId() {
        var address = new LegalPersonAddress(
                "address-key", "90110220", "Rua Ismael Chaves Barcelos", "60", "Sala 2", "Praia de Belas");

        assertThat(address.getId()).isNull();
        assertThat(address.getLegalPersonAddressKey()).isEqualTo("address-key");
        assertThat(address.getPostalCode()).isEqualTo("90110220");
        assertThat(address.getStreet()).isEqualTo("Rua Ismael Chaves Barcelos");
        assertThat(address.getNumber()).isEqualTo("60");
        assertThat(address.getComplement()).isEqualTo("Sala 2");
        assertThat(address.getNeighborhood()).isEqualTo("Praia de Belas");
        assertThat(address.getCreatedAt()).isNull();
        assertThat(address.getUpdatedAt()).isNull();
    }

    @Test
    void new_allArgs_exposesValues() {
        var address = new LegalPersonAddress(
                3L, "address-key", "90110220", "Rua A", "60", null, "Centro", NOW, NOW);

        assertThat(address.getId()).isEqualTo(3L);
        assertThat(address.getComplement()).isNull();
        assertThat(address.getCreatedAt()).isEqualTo(NOW);
        assertThat(address.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void setters_newValues_updateFields() {
        var address = new LegalPersonAddress();

        address.setId(4L);
        address.setLegalPersonAddressKey("other");
        address.setPostalCode("01001000");
        address.setStreet("Praca da Se");
        address.setNumber("1");
        address.setComplement("Andar 3");
        address.setNeighborhood("Se");
        address.setCreatedAt(NOW);
        address.setUpdatedAt(NOW);

        assertThat(address.getId()).isEqualTo(4L);
        assertThat(address.getLegalPersonAddressKey()).isEqualTo("other");
        assertThat(address.getPostalCode()).isEqualTo("01001000");
        assertThat(address.getStreet()).isEqualTo("Praca da Se");
        assertThat(address.getNumber()).isEqualTo("1");
        assertThat(address.getComplement()).isEqualTo("Andar 3");
        assertThat(address.getNeighborhood()).isEqualTo("Se");
        assertThat(address.getCreatedAt()).isEqualTo(NOW);
        assertThat(address.getUpdatedAt()).isEqualTo(NOW);
    }
}
