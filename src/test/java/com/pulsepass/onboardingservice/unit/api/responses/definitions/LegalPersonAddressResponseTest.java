package com.pulsepass.onboardingservice.unit.api.responses.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonAddressResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import org.junit.jupiter.api.Test;

class LegalPersonAddressResponseTest {

    @Test
    void fromEntity_address_mapsAllFields() {
        var address = new LegalPersonAddress("address-key", "90110220", "Rua A", "60", "Sala 2", "Centro");

        var response = LegalPersonAddressResponse.fromEntity(address);

        assertThat(response.legalPersonAddressKey()).isEqualTo("address-key");
        assertThat(response.postalCode()).isEqualTo("90110220");
        assertThat(response.street()).isEqualTo("Rua A");
        assertThat(response.number()).isEqualTo("60");
        assertThat(response.complement()).isEqualTo("Sala 2");
        assertThat(response.neighborhood()).isEqualTo("Centro");
    }

    @Test
    void fromEntity_nullComplement_keepsNull() {
        var address = new LegalPersonAddress("address-key", "90110220", "Rua A", "60", null, "Centro");

        assertThat(LegalPersonAddressResponse.fromEntity(address).complement()).isNull();
    }
}
