package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.CollaboratorNaturalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;

public record ListLegalPersonCollaboratorsResponse(
        String legalPersonCollaboratorKey,
        String collaboratorRole,
        String legalPersonCollaboratorStatus,
        CollaboratorNaturalPersonResponse naturalPerson
) {

    public static ListLegalPersonCollaboratorsResponse fromEntity(LegalPersonCollaborator legalPersonCollaborator) {
        return new ListLegalPersonCollaboratorsResponse(
                legalPersonCollaborator.getLegalPersonCollaboratorKey(),
                legalPersonCollaborator.getCollaboratorRole().getEnumerator(),
                legalPersonCollaborator.getLegalPersonCollaboratorStatus().getEnumerator(),
                CollaboratorNaturalPersonResponse.fromEntity(legalPersonCollaborator.getNaturalPerson())
        );
    }
}
