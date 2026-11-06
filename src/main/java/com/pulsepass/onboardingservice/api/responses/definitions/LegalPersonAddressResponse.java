package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;

public record LegalPersonAddressResponse(
        String legalPersonAddressKey,
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood
) {

    public static LegalPersonAddressResponse fromEntity(LegalPersonAddress address) {
        return new LegalPersonAddressResponse(
                address.getLegalPersonAddressKey(),
                address.getPostalCode(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood()
        );
    }
}
