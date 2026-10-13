package com.pulsepass.onboardingservice.unit.api.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.schemas.CreateLegalPersonSchema;
import com.pulsepass.onboardingservice.api.schemas.definitions.LegalPersonCollaboratorSchema;
import com.pulsepass.onboardingservice.api.schemas.definitions.PersonPhoneNumberSchema;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateLegalPersonSchemaTest {

    @Test
    void toDTO_schema_copiesAllFieldsAndNestedObjects() {
        var schema = new CreateLegalPersonSchema(
                "12345678000195",
                "Acme",
                "acme@mail.com",
                new PersonPhoneNumberSchema("55", "11", "999999999"),
                List.of(new LegalPersonCollaboratorSchema(
                        "OWNER", "52998224725", "John Doe", "john@mail.com", "Secret123"))
        );

        var dto = schema.toDTO();

        assertThat(dto.documentNumber()).isEqualTo("12345678000195");
        assertThat(dto.name()).isEqualTo("Acme");
        assertThat(dto.email()).isEqualTo("acme@mail.com");
        assertThat(dto.phoneNumber().countryCode()).isEqualTo("55");
        assertThat(dto.phoneNumber().areaCode()).isEqualTo("11");
        assertThat(dto.phoneNumber().number()).isEqualTo("999999999");
        assertThat(dto.legalPersonCollaborators()).hasSize(1);
        assertThat(dto.legalPersonCollaborators().get(0).collaboratorRole()).isEqualTo("OWNER");
        assertThat(dto.legalPersonCollaborators().get(0).documentNumber()).isEqualTo("52998224725");
        assertThat(dto.legalPersonCollaborators().get(0).password()).isEqualTo("Secret123");
    }
}
