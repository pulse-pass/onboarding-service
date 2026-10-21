package com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos;

import java.util.List;

public record CreateUserInputDTO(
        String documentNumber,
        String name,
        String email,
        String password,
        String personKey,
        String legalPersonKey,
        List<UserRoleInputDTO> roles
) {
}
