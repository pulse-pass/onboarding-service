package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import java.util.Optional;

public interface LegalPersonCollaboratorStatusRepository {

    Optional<LegalPersonCollaboratorStatus> findByEnumerator(String enumerator);
}
