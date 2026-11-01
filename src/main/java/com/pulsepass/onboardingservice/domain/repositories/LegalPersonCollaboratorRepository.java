package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import java.util.Optional;

public interface LegalPersonCollaboratorRepository {

    LegalPersonCollaborator save(LegalPersonCollaborator legalPersonCollaborator);

    Optional<LegalPersonCollaborator> findByLegalPersonKeyAndRole(String legalPersonKey, CollaboratorRoleEnum role);

    boolean existsByLegalPersonKeyAndNaturalPersonKey(String legalPersonKey, String naturalPersonKey);
}
