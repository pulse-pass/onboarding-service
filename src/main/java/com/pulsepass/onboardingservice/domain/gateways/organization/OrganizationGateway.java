package com.pulsepass.onboardingservice.domain.gateways.organization;

import com.pulsepass.onboardingservice.domain.gateways.organization.dtos.CreateOrganizationInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.organization.dtos.CreateOrganizationOutputDTO;

public interface OrganizationGateway {

    CreateOrganizationOutputDTO createOrganization(CreateOrganizationInputDTO input);
}
