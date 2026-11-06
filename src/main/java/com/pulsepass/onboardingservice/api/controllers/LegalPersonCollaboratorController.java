package com.pulsepass.onboardingservice.api.controllers;

import com.pulsepass.onboardingservice.api.responses.ListInternalLegalPersonCollaboratorsResponse;
import com.pulsepass.onboardingservice.api.responses.ListLegalPersonCollaboratorsResponse;
import com.pulsepass.onboardingservice.api.responses.pagination.PaginatedResponse;
import com.pulsepass.onboardingservice.api.responses.pagination.PaginationResponse;
import com.pulsepass.onboardingservice.api.validators.InternalRequestValidator;
import com.pulsepass.onboardingservice.application.services.LegalPersonCollaboratorService;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegalPersonCollaboratorController {

    private final LegalPersonCollaboratorService legalPersonCollaboratorService;
    private final InternalRequestValidator internalRequestValidator;

    public LegalPersonCollaboratorController(
            LegalPersonCollaboratorService legalPersonCollaboratorService,
            InternalRequestValidator internalRequestValidator
    ) {
        this.legalPersonCollaboratorService = legalPersonCollaboratorService;
        this.internalRequestValidator = internalRequestValidator;
    }

    @GetMapping("/onboarding/legal_persons/{legal_person_key}/collaborators")
    public ResponseEntity<PaginatedResponse<ListLegalPersonCollaboratorsResponse>> listLegalPersonCollaborators(
            @RequestHeader(value = "selected-agent-key", required = false) String selectedAgentKey,
            @PathVariable("legal_person_key") String legalPersonKey,
            @RequestParam(name = "collaborator_role", required = false) CollaboratorRoleEnum collaboratorRole,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(name = "page_size", defaultValue = "30") @Min(1) @Max(100) Integer pageSize
    ) {
        internalRequestValidator.validateSelectedAgentKey(selectedAgentKey);
        var legalPersonCollaborators = legalPersonCollaboratorService.listLegalPersonCollaborators(
                legalPersonKey, selectedAgentKey, collaboratorRole, page, pageSize);
        var data = legalPersonCollaborators.stream().map(ListLegalPersonCollaboratorsResponse::fromEntity).toList();
        return ResponseEntity.ok(new PaginatedResponse<>(data, new PaginationResponse(page, pageSize)));
    }

    @GetMapping("/onboarding/internal/legal_persons/{legal_person_key}/collaborators")
    public ResponseEntity<PaginatedResponse<ListInternalLegalPersonCollaboratorsResponse>>
            listInternalLegalPersonCollaborators(
            @RequestHeader(value = "internal-token", required = false) String internalToken,
            @RequestHeader(value = "selected-user-roles", required = false) String selectedUserRoles,
            @PathVariable("legal_person_key") String legalPersonKey,
            @RequestParam(name = "collaborator_role", required = false) CollaboratorRoleEnum collaboratorRole,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(name = "page_size", defaultValue = "30") @Min(1) @Max(100) Integer pageSize
    ) {
        internalRequestValidator.validateInternalTokenOrAdmin(internalToken, selectedUserRoles);
        var legalPersonCollaborators = legalPersonCollaboratorService.listInternalLegalPersonCollaborators(
                legalPersonKey, collaboratorRole, page, pageSize);
        var data = legalPersonCollaborators.stream()
                .map(ListInternalLegalPersonCollaboratorsResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(new PaginatedResponse<>(data, new PaginationResponse(page, pageSize)));
    }
}
