package com.pulsepass.onboardingservice.domain.gateways.identityprovider;

import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeleteSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.RefreshSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;

public interface IdentityProviderGateway {

    void createUser(CreateUserInputDTO input);

    SessionOutputDTO createSession(CreateSessionInputDTO input);

    SessionOutputDTO refreshSession(RefreshSessionInputDTO input);

    void deleteSession(DeleteSessionInputDTO input);
}
