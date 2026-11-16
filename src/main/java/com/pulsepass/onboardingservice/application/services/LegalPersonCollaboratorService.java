package com.pulsepass.onboardingservice.application.services;

import com.pulsepass.onboardingservice.domain.UnitOfWork;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorHistory;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatusEnum;
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import com.pulsepass.onboardingservice.domain.exceptions.UserDeactivationException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeactivateUserInputDTO;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorHistoryRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorStatusRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LegalPersonCollaboratorService {

    private static final Logger logger = LoggerFactory.getLogger(LegalPersonCollaboratorService.class);

    private final UnitOfWork unitOfWork;
    private final LegalPersonRepository legalPersonRepository;
    private final LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;
    private final LegalPersonCollaboratorStatusRepository legalPersonCollaboratorStatusRepository;
    private final LegalPersonCollaboratorHistoryRepository legalPersonCollaboratorHistoryRepository;
    private final IdentityProviderGateway identityProviderGateway;

    public LegalPersonCollaboratorService(
            UnitOfWork unitOfWork,
            LegalPersonRepository legalPersonRepository,
            LegalPersonCollaboratorRepository legalPersonCollaboratorRepository,
            LegalPersonCollaboratorStatusRepository legalPersonCollaboratorStatusRepository,
            LegalPersonCollaboratorHistoryRepository legalPersonCollaboratorHistoryRepository,
            IdentityProviderGateway identityProviderGateway
    ) {
        this.unitOfWork = unitOfWork;
        this.legalPersonRepository = legalPersonRepository;
        this.legalPersonCollaboratorRepository = legalPersonCollaboratorRepository;
        this.legalPersonCollaboratorStatusRepository = legalPersonCollaboratorStatusRepository;
        this.legalPersonCollaboratorHistoryRepository = legalPersonCollaboratorHistoryRepository;
        this.identityProviderGateway = identityProviderGateway;
    }

    public void deleteInternalLegalPersonCollaborators(String legalPersonKey) {
        logger.info("LegalPersonCollaboratorService.deleteInternalLegalPersonCollaborators - Starting");

        LegalPersonCollaboratorStatus deletedStatus = legalPersonCollaboratorStatusRepository
                .findByEnumerator(LegalPersonCollaboratorStatusEnum.DELETED.name())
                .orElseThrow(() -> new IllegalStateException("Legal person collaborator status DELETED not found"));
        LegalPerson legalPerson = legalPersonRepository
                .findByPersonKey(legalPersonKey)
                .orElseThrow(PersonNotFoundException::new);

        List<LegalPersonCollaborator> collaborators = legalPersonCollaboratorRepository
                .findAllByLegalPersonKeyAndStatus(legalPersonKey, LegalPersonCollaboratorStatusEnum.ACTIVE);
        for (LegalPersonCollaborator listedCollaborator : collaborators) {
            unitOfWork.begin();
            LegalPersonCollaborator collaborator = legalPersonCollaboratorRepository
                    .findForUpdateByLegalPersonCollaboratorKey(listedCollaborator.getLegalPersonCollaboratorKey())
                    .orElseThrow(() -> new IllegalStateException("Legal person collaborator not found"));
            if (!LegalPersonCollaboratorStatusEnum.ACTIVE.name()
                    .equals(collaborator.getLegalPersonCollaboratorStatus().getEnumerator())) {
                logger.info(
                        "LegalPersonCollaboratorService.deleteInternalLegalPersonCollaborators - "
                                + "Collaborator is no longer active {}",
                        Map.of("legal_person_collaborator_key", collaborator.getLegalPersonCollaboratorKey())
                );
                continue;
            }
            LegalPersonCollaboratorStatus oldStatus = collaborator.getLegalPersonCollaboratorStatus();
            collaborator.setLegalPersonCollaboratorStatus(deletedStatus);
            legalPersonCollaboratorRepository.save(collaborator);
            legalPersonCollaboratorHistoryRepository.save(
                    LegalPersonCollaboratorHistory.fromLegalPersonCollaborator(collaborator, oldStatus)
            );
            DeactivateUserInputDTO deactivateUserInput = new DeactivateUserInputDTO(
                    collaborator.getNaturalPerson().getPerson().getPersonKey(),
                    collaborator.getNaturalPerson().getPerson().getDocumentNumber()
            );
            try {
                identityProviderGateway.deactivateUser(deactivateUserInput);
            } catch (Exception ex) {
                logger.error(
                        "LegalPersonCollaboratorService.deleteInternalLegalPersonCollaborators - "
                                + "Failed to deactivate the user {}",
                        Map.of(
                                "legal_person_key", legalPerson.getPerson().getPersonKey(),
                                "legal_person_collaborator_key", collaborator.getLegalPersonCollaboratorKey()
                        ),
                        ex
                );
                throw new UserDeactivationException();
            }
            unitOfWork.commit();
        }

        logger.info("LegalPersonCollaboratorService.deleteInternalLegalPersonCollaborators - Successfully finished");
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
