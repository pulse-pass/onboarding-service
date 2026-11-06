package com.pulsepass.onboardingservice.application.dtos.input.definitions;

public record LegalPersonAddressDTO(
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood
) {
}
