package com.pulsepass.onboardingservice.api.controllers;

import com.pulsepass.onboardingservice.api.responses.CreateLegalPersonResponse;
import com.pulsepass.onboardingservice.api.schemas.CreateLegalPersonSchema;
import com.pulsepass.onboardingservice.application.services.LegalPersonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegalPersonController {

    private final LegalPersonService legalPersonService;

    public LegalPersonController(LegalPersonService legalPersonService) {
        this.legalPersonService = legalPersonService;
    }

    @PostMapping("/legal_persons")
    public ResponseEntity<CreateLegalPersonResponse> createLegalPerson(
            @Valid @RequestBody CreateLegalPersonSchema request
    ) {
        var legalPerson = legalPersonService.createLegalPerson(request.toDTO());
        return ResponseEntity.status(201).body(CreateLegalPersonResponse.fromEntity(legalPerson));
    }
}
