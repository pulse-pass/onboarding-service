package com.pulsepass.onboardingservice.api.schemas.definitions;

import com.pulsepass.onboardingservice.application.dtos.input.definitions.PersonPhoneNumberDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PersonPhoneNumberSchema(
        @NotBlank @Pattern(regexp = "^\\d{1,3}$") String countryCode,
        @NotBlank @Pattern(regexp = "^\\d{2}$") String areaCode,
        @NotBlank @Pattern(regexp = "^9\\d{8}$") String number
) {

    public PersonPhoneNumberDTO toDTO() {
        return new PersonPhoneNumberDTO(countryCode, areaCode, number);
    }
}
