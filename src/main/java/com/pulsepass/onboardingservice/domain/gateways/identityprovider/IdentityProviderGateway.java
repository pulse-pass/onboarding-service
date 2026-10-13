package com.pulsepass.onboardingservice.domain.gateways.identityprovider;

import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;

public interface IdentityProviderGateway {

    void createUser(CreateUserInputDTO input);
}
