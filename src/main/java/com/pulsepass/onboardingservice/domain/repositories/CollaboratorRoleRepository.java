package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import java.util.Optional;

public interface CollaboratorRoleRepository {

    Optional<CollaboratorRole> findByEnumerator(String enumerator);
}
