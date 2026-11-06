package com.pulsepass.onboardingservice.api.controllers;

import com.pulsepass.onboardingservice.api.responses.GetInternalPersonResponse;
import com.pulsepass.onboardingservice.api.responses.GetPersonResponse;
import com.pulsepass.onboardingservice.api.validators.InternalRequestValidator;
import com.pulsepass.onboardingservice.application.services.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {

    private final PersonService personService;
    private final InternalRequestValidator internalRequestValidator;

    public PersonController(
            PersonService personService,
            InternalRequestValidator internalRequestValidator
    ) {
        this.personService = personService;
        this.internalRequestValidator = internalRequestValidator;
    }

    @GetMapping("/onboarding/persons/{person_key}")
    public ResponseEntity<GetPersonResponse> getPerson(
            @RequestHeader("selected-user-key") String selectedUserKey,
            @RequestHeader(value = "selected-agent-key", required = false) String selectedAgentKey,
            @PathVariable("person_key") String personKey
    ) {
        var person = personService.getPerson(personKey, selectedAgentKey, selectedUserKey);
        return ResponseEntity.ok(GetPersonResponse.fromDTO(person));
    }

    @GetMapping("/onboarding/internal/persons/{person_key}")
    public ResponseEntity<GetInternalPersonResponse> getInternalPerson(
            @RequestHeader(value = "internal-token", required = false) String internalToken,
            @RequestHeader(value = "selected-user-roles", required = false) String selectedUserRoles,
            @PathVariable("person_key") String personKey
    ) {
        internalRequestValidator.validateInternalTokenOrAdmin(internalToken, selectedUserRoles);
        var person = personService.getInternalPerson(personKey);
        return ResponseEntity.ok(GetInternalPersonResponse.fromDTO(person));
    }
}
