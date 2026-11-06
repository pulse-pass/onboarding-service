package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;

public interface LegalPersonAddressRepository {

    LegalPersonAddress save(LegalPersonAddress legalPersonAddress);
}
