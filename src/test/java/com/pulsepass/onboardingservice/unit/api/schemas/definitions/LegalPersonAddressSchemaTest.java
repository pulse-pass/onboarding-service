package com.pulsepass.onboardingservice.unit.api.schemas.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.schemas.definitions.LegalPersonAddressSchema;
import org.junit.jupiter.api.Test;

class LegalPersonAddressSchemaTest {

    @Test
    void toDTO_schema_copiesAllFields() {
        var dto = new LegalPersonAddressSchema(
                "90110220", "Rua Ismael Chaves Barcelos", "60", "Sala 2", "Praia de Belas").toDTO();

        assertThat(dto.postalCode()).isEqualTo("90110220");
        assertThat(dto.street()).isEqualTo("Rua Ismael Chaves Barcelos");
        assertThat(dto.number()).isEqualTo("60");
        assertThat(dto.complement()).isEqualTo("Sala 2");
        assertThat(dto.neighborhood()).isEqualTo("Praia de Belas");
    }

    @Test
    void toDTO_nullComplement_keepsNull() {
        var dto = new LegalPersonAddressSchema("90110220", "Rua A", "60", null, "Centro").toDTO();

        assertThat(dto.complement()).isNull();
    }
}
