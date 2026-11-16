package com.pulsepass.onboardingservice.domain.repositories;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatusEnum;
import java.util.List;
import java.util.Optional;

public interface LegalPersonCollaboratorRepository {

    LegalPersonCollaborator save(LegalPersonCollaborator legalPersonCollaborator);

    List<LegalPersonCollaborator> findAll(
            String legalPersonKey, CollaboratorRoleEnum collaboratorRole, int page, int pageSize);

    List<LegalPersonCollaborator> findAllByLegalPersonKeyAndStatus(
            String legalPersonKey, LegalPersonCollaboratorStatusEnum status);

    Optional<LegalPersonCollaborator> findForUpdateByLegalPersonCollaboratorKey(String legalPersonCollaboratorKey);

    Optional<LegalPersonCollaborator> findFirstByLegalPersonKeyAndNaturalPersonKey(
            String legalPersonKey, String naturalPersonKey);
}
