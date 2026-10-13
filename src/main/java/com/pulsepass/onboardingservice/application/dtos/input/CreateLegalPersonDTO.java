package com.pulsepass.onboardingservice.application.dtos.input;

import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonCollaboratorDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.PersonPhoneNumberDTO;
import java.util.List;

public record CreateLegalPersonDTO(
        String documentNumber,
        String name,
        String email,
        PersonPhoneNumberDTO phoneNumber,
        List<LegalPersonCollaboratorDTO> legalPersonCollaborators
) {
}
