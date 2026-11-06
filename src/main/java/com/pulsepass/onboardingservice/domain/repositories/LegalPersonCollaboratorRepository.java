package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import java.util.List;

public interface LegalPersonCollaboratorRepository {

    LegalPersonCollaborator save(LegalPersonCollaborator legalPersonCollaborator);

    List<LegalPersonCollaborator> findAll(
            String legalPersonKey, CollaboratorRoleEnum collaboratorRole, int page, int pageSize);

    boolean existsByLegalPersonKeyAndNaturalPersonKey(String legalPersonKey, String naturalPersonKey);
}
