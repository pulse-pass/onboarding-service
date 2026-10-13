package com.pulsepass.onboardingservice.unit.api.schemas.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.schemas.definitions.LegalPersonCollaboratorSchema;
import org.junit.jupiter.api.Test;

class LegalPersonCollaboratorSchemaTest {

    @Test
    void toDTO_schema_copiesAllFields() {
        var schema = new LegalPersonCollaboratorSchema(
                "OWNER", "52998224725", "John Doe", "john@mail.com", "Secret123");

        var dto = schema.toDTO();

        assertThat(dto.collaboratorRole()).isEqualTo("OWNER");
        assertThat(dto.documentNumber()).isEqualTo("52998224725");
        assertThat(dto.name()).isEqualTo("John Doe");
        assertThat(dto.email()).isEqualTo("john@mail.com");
        assertThat(dto.password()).isEqualTo("Secret123");
    }

    @Test
    void toString_schema_masksPassword() {
        var schema = new LegalPersonCollaboratorSchema(
                "OWNER", "52998224725", "John Doe", "john@mail.com", "Secret123");

        assertThat(schema.toString()).contains("password=***").doesNotContain("Secret123");
    }
}
