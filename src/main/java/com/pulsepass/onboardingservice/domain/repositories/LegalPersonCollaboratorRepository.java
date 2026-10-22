package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;

public interface LegalPersonCollaboratorRepository {

    LegalPersonCollaborator save(LegalPersonCollaborator legalPersonCollaborator);

    boolean existsByLegalPersonKeyAndNaturalPersonKey(String legalPersonKey, String naturalPersonKey);
}
