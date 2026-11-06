package com.pulsepass.onboardingservice.api.schemas.definitions;

import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonAddressDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LegalPersonAddressSchema(
        @NotBlank @Pattern(regexp = "^\\d{8}$") String postalCode,
        @NotBlank @Size(max = 255) String street,
        @NotBlank @Size(max = 20) String number,
        @Size(max = 255) @Pattern(regexp = "^(?!\\s*$).+$") String complement,
        @NotBlank @Size(max = 255) String neighborhood
) {

    public LegalPersonAddressDTO toDTO() {
        return new LegalPersonAddressDTO(postalCode, street, number, complement, neighborhood);
    }
}
