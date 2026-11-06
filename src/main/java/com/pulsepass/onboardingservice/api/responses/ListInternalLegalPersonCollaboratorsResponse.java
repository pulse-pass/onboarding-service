package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.CollaboratorNaturalPersonResponse;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;

public record ListInternalLegalPersonCollaboratorsResponse(
        String legalPersonCollaboratorKey,
        String collaboratorRole,
        String legalPersonCollaboratorStatus,
        CollaboratorNaturalPersonResponse naturalPerson
) {

    public static ListInternalLegalPersonCollaboratorsResponse fromEntity(
            LegalPersonCollaborator legalPersonCollaborator) {
        return new ListInternalLegalPersonCollaboratorsResponse(
                legalPersonCollaborator.getLegalPersonCollaboratorKey(),
                legalPersonCollaborator.getCollaboratorRole().getEnumerator(),
                legalPersonCollaborator.getLegalPersonCollaboratorStatus().getEnumerator(),
                CollaboratorNaturalPersonResponse.fromEntity(legalPersonCollaborator.getNaturalPerson())
        );
    }
}
