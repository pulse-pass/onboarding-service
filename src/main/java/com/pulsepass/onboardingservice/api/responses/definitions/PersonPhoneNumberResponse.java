package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;

public record PersonPhoneNumberResponse(
        String countryCode,
        String areaCode,
        String number
) {

    public static PersonPhoneNumberResponse fromEntity(PersonPhoneNumber phoneNumber) {
        return new PersonPhoneNumberResponse(
                phoneNumber.getCountryCode(),
                phoneNumber.getAreaCode(),
                phoneNumber.getNumber()
        );
    }
}
