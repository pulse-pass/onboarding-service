package com.pulsepass.onboardingservice.api.controllers;

import com.pulsepass.onboardingservice.api.responses.CreateLegalPersonResponse;
import com.pulsepass.onboardingservice.api.responses.GetInternalLegalPersonResponse;
import com.pulsepass.onboardingservice.api.responses.GetLegalPersonResponse;
import com.pulsepass.onboardingservice.api.schemas.CreateLegalPersonSchema;
import com.pulsepass.onboardingservice.api.validators.InternalRequestValidator;
import com.pulsepass.onboardingservice.application.services.LegalPersonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegalPersonController {

    private final LegalPersonService legalPersonService;
    private final InternalRequestValidator internalRequestValidator;

    public LegalPersonController(
            LegalPersonService legalPersonService,
            InternalRequestValidator internalRequestValidator
    ) {
        this.legalPersonService = legalPersonService;
        this.internalRequestValidator = internalRequestValidator;
    }

    @PostMapping("/onboarding/legal_persons")
    public ResponseEntity<CreateLegalPersonResponse> createLegalPerson(
            @Valid @RequestBody CreateLegalPersonSchema request
    ) {
        var legalPerson = legalPersonService.createLegalPerson(request.toDTO());
        return ResponseEntity.status(201).body(CreateLegalPersonResponse.fromEntity(legalPerson));
    }

    @GetMapping("/onboarding/legal_persons/{legal_person_key}")
    public ResponseEntity<GetLegalPersonResponse> getLegalPerson(
            @RequestHeader(value = "selected-agent-key", required = false) String selectedAgentKey,
            @PathVariable("legal_person_key") String legalPersonKey
    ) {
        internalRequestValidator.validateSelectedAgentKey(selectedAgentKey);
        var legalPerson = legalPersonService.getLegalPerson(legalPersonKey, selectedAgentKey);
        return ResponseEntity.ok(GetLegalPersonResponse.fromDTO(legalPerson));
    }

    @GetMapping("/onboarding/internal/legal_persons/{legal_person_key}")
    public ResponseEntity<GetInternalLegalPersonResponse> getInternalLegalPerson(
            @RequestHeader(value = "internal-token", required = false) String internalToken,
            @RequestHeader(value = "selected-user-roles", required = false) String selectedUserRoles,
            @PathVariable("legal_person_key") String legalPersonKey
    ) {
        internalRequestValidator.validateInternalTokenOrAdmin(internalToken, selectedUserRoles);
        var legalPerson = legalPersonService.getInternalLegalPerson(legalPersonKey);
        return ResponseEntity.ok(GetInternalLegalPersonResponse.fromDTO(legalPerson));
    }
}
