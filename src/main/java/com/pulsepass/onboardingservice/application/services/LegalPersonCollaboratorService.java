package com.pulsepass.onboardingservice.application.services;

import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LegalPersonCollaboratorService {

    private static final Logger logger = LoggerFactory.getLogger(LegalPersonCollaboratorService.class);

    private final LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;

    public LegalPersonCollaboratorService(LegalPersonCollaboratorRepository legalPersonCollaboratorRepository) {
        this.legalPersonCollaboratorRepository = legalPersonCollaboratorRepository;
    }

    public List<LegalPersonCollaborator> listLegalPersonCollaborators(
            String legalPersonKey,
            String selectedAgentKey,
            CollaboratorRoleEnum collaboratorRole,
            int page,
            int pageSize
    ) {
        logger.info("LegalPersonCollaboratorService.listLegalPersonCollaborators - Starting");

        if (!legalPersonKey.equals(selectedAgentKey)) {
            logger.warn("LegalPersonCollaboratorService.listLegalPersonCollaborators - "
                    + "Legal person key differs from the selected agent");
            throw new PersonNotFoundException();
        }

        var legalPersonCollaborators = legalPersonCollaboratorRepository.findAll(
                legalPersonKey, collaboratorRole, page, pageSize);

        logger.info("LegalPersonCollaboratorService.listLegalPersonCollaborators - Successfully finished");
        return legalPersonCollaborators;
    }

    public List<LegalPersonCollaborator> listInternalLegalPersonCollaborators(
            String legalPersonKey,
            CollaboratorRoleEnum collaboratorRole,
            int page,
            int pageSize
    ) {
        logger.info("LegalPersonCollaboratorService.listInternalLegalPersonCollaborators - Starting");
        var legalPersonCollaborators = legalPersonCollaboratorRepository.findAll(
                legalPersonKey, collaboratorRole, page, pageSize);
        logger.info("LegalPersonCollaboratorService.listInternalLegalPersonCollaborators - Successfully finished");
        return legalPersonCollaborators;
    }
}
